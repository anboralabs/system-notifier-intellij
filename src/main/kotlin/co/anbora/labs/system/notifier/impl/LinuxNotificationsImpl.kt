package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.ui.findAppIcon
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference


object LinuxNotificationsImpl: SystemNotifier {

    private var myLibNotify: LibNotify? = null
    private var myGlib: GLib? = null
    private var myIcon: String? = null
    private val myLock = Any()
    private var myDisposed = false

    private interface LibNotify : Library {
        fun notify_init(appName: String?): Int
        fun notify_uninit()
        fun notify_notification_new(summary: String?, body: String?, icon: String?): Pointer?
        fun notify_notification_show(notification: Pointer?, error: PointerByReference?): Int
    }

    private interface GLib : Library {
        fun g_error_free(error: Pointer?)
    }


    init {
        myLibNotify = Native.load("libnotify.so.4", LibNotify::class.java)
        try {
            myGlib = Native.load("libglib-2.0.so.0", GLib::class.java)
        } catch (_: Throwable) {
            // Best effort: if GLib binding fails, we can still deliver notifications without detailed error text.
        }

        val appName = ApplicationNamesInfo.getInstance().productName
        check(myLibNotify!!.notify_init(appName) != 0) { "notify_init failed" }

        val icon: String? = findAppIcon()
        myIcon = icon ?: "dialog-information"

        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe<AppLifecycleListener>(AppLifecycleListener.TOPIC, object : AppLifecycleListener {
            override fun appClosing() {
                synchronized(myLock) {
                    myDisposed = true
                    myLibNotify!!.notify_uninit()
                }
            }
        })
    }

    override fun notify(name: String, title: String, description: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(myLock) {
                val notification = myLibNotify?.notify_notification_new(title, description, myIcon)

                val errorRef = PointerByReference()
                val result = myLibNotify?.notify_notification_show(notification, errorRef) ?: 0
                if (result == 0) {
                    val errPtr = errorRef.value
                    val message = try {
                        if (errPtr != null) {
                            // GError layout: guint domain (4), gint code (4), gchar* message (pointer) -> message at offset 8
                            errPtr.getPointer(8L)?.getString(0)
                        } else null
                    } catch (_: Throwable) { null }

                    System.err.println("[LinuxNotificationsImpl] notify_notification_show failed" + (message?.let { ": $it" } ?: ""))

                    try {
                        if (errPtr != null) myGlib?.g_error_free(errPtr)
                    } catch (_: Throwable) {
                        // ignore
                    }
                } else {
                    println("[LinuxNotificationsImpl] notify_notification_show OK (result=$result)")
                }
            }
        }
    }
}