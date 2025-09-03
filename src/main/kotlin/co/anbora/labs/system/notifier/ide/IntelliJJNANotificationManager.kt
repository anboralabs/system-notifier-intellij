package co.anbora.labs.system.notifier.ide

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.mac.foundation.Foundation
import com.intellij.ui.mac.foundation.ID
import com.sun.jna.Callback
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import com.intellij.openapi.diagnostic.Logger

/**
 * Notification manager backed by IntelliJ's mac Foundation bridge.
 * Always attempts native macOS delivery and falls back to IDE notifications when needed.
 */
object IntelliJJNANotificationManager {

    const val NOTIF_GROUP_ID: String = "IntelliJ JNA Notifier"

    enum class Type { SUCCESS, ERROR, WARNING, INFO }

    private val delegateInstalled = AtomicBoolean(false)
    @Volatile
    private var delegateRef: Any? = null
    @Volatile
    private var centerRef: ID? = null
    private val LOG = Logger.getInstance(IntelliJJNANotificationManager::class.java)

    // Cache frequently used Objective-C classes to avoid repeated lookups
    private object Cocoa {
        val NSUserNotification = Foundation.getObjcClass("NSUserNotification")
        val NSUserNotificationCenter = Foundation.getObjcClass("NSUserNotificationCenter")
        val NSObject = Foundation.getObjcClass("NSObject")
    }

    // Quick OS check for macOS.
    fun isMac(): Boolean = System.getProperty("os.name", "").lowercase().contains("mac")

    // Maps notification type to macOS sound default if none is specified.
    private fun defaultSound(type: Type): String? = when (type) {
        Type.SUCCESS -> "Glass"
        Type.ERROR -> "Basso"
        Type.WARNING -> "Ping"
        Type.INFO -> null
    }

    // Convenience APIs
    fun success(project: Project?, title: String, message: String): Unit =
        notify(project, title, message, Type.SUCCESS)

    fun error(project: Project?, title: String, message: String): Unit =
        notify(project, title, message, Type.ERROR)

    fun warning(project: Project?, title: String, message: String): Unit =
        notify(project, title, message, Type.WARNING)

    fun info(project: Project?, title: String, message: String): Unit =
        notify(project, title, message, Type.INFO)

    fun notify(
        project: Project?,
        title: String?,
        message: String?,
        type: Type = Type.INFO,
        subtitle: String? = null,
        actionButtonTitle: String? = null,
        soundName: String? = null
    ) {
        try {
            if (isMac()) {
                val ok = deliverMacOS(
                    title = title,
                    subtitle = subtitle,
                    message = message,
                    sound = soundName ?: defaultSound(type),
                    actionButtonTitle = actionButtonTitle
                )
                if (!ok) showIjFallback(project, type, title, message, subtitle)
            } else {
                showIjFallback(project, type, title, message, subtitle)
            }
        } catch (t: Throwable) {
            showIjFallback(project, type, title, message, subtitle)
        }
    }

    private fun showIjFallback(
        project: Project?,
        type: Type,
        title: String?,
        message: String?,
        subtitle: String?
    ) {
        val ijType = when (type) {
            Type.SUCCESS, Type.INFO -> NotificationType.INFORMATION
            Type.WARNING -> NotificationType.WARNING
            Type.ERROR -> NotificationType.ERROR
        }
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIF_GROUP_ID)
        val fullTitle = buildString {
            if (!title.isNullOrBlank()) append(title)
            if (!subtitle.isNullOrBlank()) {
                if (isNotEmpty()) append(" — ")
                append(subtitle)
            }
        }
        group.createNotification(fullTitle, message ?: "", ijType).notify(project)
    }

    /**
     * Native delivery using Objective-C runtime via Foundation bridge.
     * Uses NSUserNotification and NSUserNotificationCenter.
     * Returns true if we believe the call succeeded.
     */
    private fun deliverMacOS(
        title: String?,
        subtitle: String?,
        message: String?,
        sound: String?,
        actionButtonTitle: String?
    ): Boolean {
        return try {
            ensureDelegateInstalled()

            var success = false
            try {
                    val notif = Foundation.invoke(Cocoa.NSUserNotification, "new")
                    if (title != null) {
                        Foundation.invoke(
                            notif,
                            "setTitle:",
                            Foundation.nsString(StringUtil.stripHtml(title, true).replace("%", "%%"))
                        )
                    }
                    if (subtitle != null) {
                        Foundation.invoke(
                            notif,
                            "setSubtitle:",
                            Foundation.nsString(StringUtil.stripHtml(subtitle, true).replace("%", "%%"))
                        )
                    }
                    if (message != null) {
                        Foundation.invoke(
                            notif,
                            "setInformativeText:",
                            Foundation.nsString(StringUtil.stripHtml(message, true).replace("%", "%%"))
                        )
                    }
                    // Ensure each notification is treated as unique so macOS always presents it
                    Foundation.invoke(
                        notif,
                        "setIdentifier:",
                        Foundation.nsString(UUID.randomUUID().toString())
                    )
                    // Add sound if provided (use null to respect system preference)
                    if (sound != null) {
                        Foundation.invoke(
                            notif,
                            "setSoundName:",
                            Foundation.nsString(sound)
                        )
                    }
                    if (!actionButtonTitle.isNullOrBlank()) {
                        // BOOL in ObjC -> use 1 (YES)
                        Foundation.invoke(notif, "setHasActionButton:", 1)
                        Foundation.invoke(notif, "setActionButtonTitle:", Foundation.nsString(actionButtonTitle))
                    }

                    val centerLocal = (centerRef ?: Foundation.invoke(
                        Cocoa.NSUserNotificationCenter,
                        "defaultUserNotificationCenter"
                    ).also { centerRef = it })
                    Foundation.invoke(centerLocal, "deliverNotification:", notif)
                    success = true
                } catch (t: Throwable) {
                    LOG.warn("macOS notification delivery failed on main thread", t)
                }

            success
        } catch (t: Throwable) {
            LOG.warn("macOS notification delivery failed", t)
            false
        }
    }

    private fun ensureDelegateInstalled() {
        if (delegateInstalled.get()) return
        synchronized(delegateInstalled) {
            if (delegateInstalled.get()) return
            try {
                    try {
                        val center = centerRef ?: Foundation.invoke(
                            Cocoa.NSUserNotificationCenter,
                            "defaultUserNotificationCenter"
                        ).also { centerRef = it }

                        // Always-present delegate
                        val shouldPresentCallback = object : Callback {
                            @Suppress("unused")
                            fun callback(self: ID?, sel: ID?, center: ID?, notification: ID?): Byte {
                                return 1 // YES
                            }
                        }

                        val delegate: Any = try {
                            val ijnClass = Foundation.getObjcClass("IJNANotifierDelegate")
                            // Add method to existing IJNANotifierDelegate class (no-op if already present)
                            Foundation.addMethod(
                                ijnClass,
                                Foundation.createSelector("userNotificationCenter:shouldPresentNotification:"),
                                shouldPresentCallback,
                                "c@:@@"
                            )
                            val allocated = Foundation.invoke(ijnClass, "alloc")
                            Foundation.invoke(allocated, "init")
                        } catch (_: Throwable) {
                            // Fallback: dynamically create our own delegate class that always presents notifications
                            val nsObjectClass = Cocoa.NSObject
                            val className = "ANSystemNotifierDelegate"
                            val delegateClass = Foundation.allocateObjcClassPair(nsObjectClass, className)

                            Foundation.addMethod(
                                delegateClass,
                                Foundation.createSelector("userNotificationCenter:shouldPresentNotification:"),
                                shouldPresentCallback,
                                "c@:@@"
                            )
                            Foundation.registerObjcClassPair(delegateClass)
                            val allocated = Foundation.invoke(delegateClass, "alloc")
                            Foundation.invoke(allocated, "init")
                        }

                        Foundation.invoke(center, "setDelegate:", delegate)
                        delegateRef = delegate // Keep strong reference to avoid GC
                        delegateInstalled.set(true)
                    } catch (inner: Throwable) {
                        LOG.warn("Failed to install NSUserNotificationCenter delegate", inner)
                    }
            } catch (t: Throwable) {
                LOG.warn("Failed to install delegate (outer)", t)
                // Best-effort: if we fail to set a delegate, we'll still try to deliver notifications.
            }
        }
    }
}
