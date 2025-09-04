package co.anbora.labs.system.notifier.impl;

import com.sun.jna.*;

/**
 * Minimal JNA bridge to the Objective-C runtime on macOS.
 * Provides helpers to get classes/selectors and send messages required to interact with
 * NSUserNotification and NSUserNotificationCenter.
 *
 * Ensures JNA is loaded via IntelliJ's JnaLoader to avoid UnsatisfiedLinkError.
 */
public final class ObjectiveCRuntime {
    private ObjectiveCRuntime() {}

    private static final boolean JNA_READY;
    static {
        boolean ok = false;
        try {
            Class<?> loader = Class.forName("com.intellij.util.jna.JnaLoader");
            Object res = loader.getMethod("isLoaded").invoke(null);
            if (res instanceof Boolean b && b) {
                ok = true;
            } else {
                System.out.println("[IntelliJJNANotifier] JnaLoader.isLoaded() returned false");
            }
        } catch (Throwable t) {
            // JnaLoader may not be present or failed; try to ensure JNA classes are at least available
            try {
                Class.forName("com.sun.jna.Native");
                ok = true; // Let JNA attempt to load its native support later
            } catch (Throwable t2) {
                System.out.println("[IntelliJJNANotifier] JNA not available: " + t2);
            }
        }
        JNA_READY = ok;
    }

    public static boolean isJnaReady() { return JNA_READY; }

    /** Mapping of the libobjc dynamic library. */
    private interface ObjC extends Library {
        ObjC INSTANCE = Native.load("objc", ObjC.class);

        Pointer objc_getClass(String name);
        Pointer sel_registerName(String name);
        Pointer objc_allocateClassPair(Pointer superCls, String name, long extraBytes);
        void objc_registerClassPair(Pointer cls);
        boolean class_addMethod(Pointer cls, Pointer name, Callback imp, String types);
        // We expose variadic msgSend via Function for flexibility.
    }

    /** Get a class pointer by name (e.g., "NSUserNotification"). */
    public static Pointer getClass(String className) {
        if (!JNA_READY) throw new IllegalStateException("JNA not loaded");
        return ObjC.INSTANCE.objc_getClass(className);
    }

    /** Get a selector pointer by name (e.g., "alloc", "init"). */
    public static Pointer getSelector(String selectorName) {
        if (!JNA_READY) throw new IllegalStateException("JNA not loaded");
        return ObjC.INSTANCE.sel_registerName(selectorName);
    }

    private static final Function OBJC_MSG_SEND = Function.getFunction("objc", "objc_msgSend");

    /**
     * Send a message returning an Objective-C id (Pointer) with 0..N additional args.
     */
    public static Pointer msgSendPointer(Pointer receiver, Pointer selector, Object... args) {
        if (!JNA_READY) throw new IllegalStateException("JNA not loaded");
        Object[] argv = new Object[2 + (args == null ? 0 : args.length)];
        argv[0] = receiver;
        argv[1] = selector;
        if (args != null && args.length > 0) System.arraycopy(args, 0, argv, 2, args.length);
        return (Pointer) OBJC_MSG_SEND.invoke(Pointer.class, argv);
    }

    /**
     * Send a message returning void.
     */
    public static void msgSendVoid(Pointer receiver, Pointer selector, Object... args) {
        if (!JNA_READY) throw new IllegalStateException("JNA not loaded");
        Object[] argv = new Object[2 + (args == null ? 0 : args.length)];
        argv[0] = receiver;
        argv[1] = selector;
        if (args != null && args.length > 0) System.arraycopy(args, 0, argv, 2, args.length);
        OBJC_MSG_SEND.invoke(Void.class, argv);
    }

    /** Mapping to Foundation for CFString creation when needed. */
    private interface CoreFoundation extends Library {
        CoreFoundation INSTANCE = Native.load("CoreFoundation", CoreFoundation.class);
        Pointer CFStringCreateWithCString(Pointer alloc, String cStr, int encoding);
    }

    public static final int kCFStringEncodingUTF8 = 0x08000100;

    /**
     * Create an NSString* from a Java String using Objective-C NSString class.
     * Falls back to CFString if necessary.
     */
    public static Pointer toNSString(String javaString) {
        try {
            Pointer nsStringClass = getClass("NSString");
            Pointer selAlloc = getSelector("alloc");
            Pointer selInitWithUTF8 = getSelector("initWithUTF8String:");
            Pointer allocated = msgSendPointer(nsStringClass, selAlloc);
            return msgSendPointer(allocated, selInitWithUTF8, javaString);
        } catch (Throwable t) {
            // Fallback via CoreFoundation
            try {
                return CoreFoundation.INSTANCE.CFStringCreateWithCString(Pointer.NULL, javaString, kCFStringEncodingUTF8);
            } catch (Throwable t2) {
                System.out.println("[IntelliJJNANotifier] Failed to create NSString: " + t2);
                return Pointer.NULL;
            }
        }
    }

    // --- NSUserNotificationCenter delegate installation ---

    private static volatile boolean DELEGATE_INSTALLED = false;
    private static Pointer delegateInstance = null; // keep strong ref to avoid GC of Callback and ObjC object

    // Callback signature: BOOL impl(id self, SEL _cmd, id center, id notification)
    public interface ShouldPresentCallback extends Callback {
        byte callback(Pointer self, Pointer cmd, Pointer center, Pointer notification);
    }

    private static final ShouldPresentCallback SHOULD_PRESENT_IMPL = (self, cmd, center, notification) -> {
        // Always force presentation
        return (byte)1;
    };

    /**
     * Ensures the NSUserNotificationCenter delegate is set with shouldPresentNotification: returning YES.
     * Safe to call multiple times; installation occurs once per process.
     */
    public static synchronized boolean ensureUserNotificationCenterDelegateInstalled() {
        if (!JNA_READY) {
            return false;
        }
        if (DELEGATE_INSTALLED && delegateInstance != null) {
            return true;
        }
        try {
            Pointer nsObject = getClass("NSObject");
            String clsName = "IJNANotifierDelegate";
            Pointer newClass = ObjC.INSTANCE.objc_allocateClassPair(nsObject, clsName, 0);
            if (newClass == null) {
                // Class may already exist; fetch it
                newClass = getClass(clsName);
            }
            Pointer selShouldPresent = getSelector("userNotificationCenter:shouldPresentNotification:");
            boolean added = ObjC.INSTANCE.class_addMethod(newClass, selShouldPresent, SHOULD_PRESENT_IMPL, "c@:@@");
            // Register class (no-op if already registered)
            ObjC.INSTANCE.objc_registerClassPair(newClass);

            // Create instance and set as delegate
            Pointer allocSel = getSelector("alloc");
            Pointer initSel = getSelector("init");
            Pointer instance = msgSendPointer(newClass, allocSel);
            instance = msgSendPointer(instance, initSel);

            Pointer centerClass = getClass("NSUserNotificationCenter");
            Pointer defaultCenterSel = getSelector("defaultUserNotificationCenter");
            Pointer setDelegateSel = getSelector("setDelegate:");
            Pointer center = msgSendPointer(centerClass, defaultCenterSel);
            msgSendVoid(center, setDelegateSel, instance);

            delegateInstance = instance; // keep a strong ref
            DELEGATE_INSTALLED = true;
            System.out.println("[IntelliJJNANotifier] Installed NSUserNotificationCenter delegate (added=" + added + ")");
            return true;
        } catch (Throwable t) {
            System.out.println("[IntelliJJNANotifier] Failed to install NSUserNotificationCenter delegate: " + t);
            return false;
        }
    }
}
