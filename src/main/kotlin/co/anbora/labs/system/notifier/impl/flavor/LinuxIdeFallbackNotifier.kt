package co.anbora.labs.system.notifier.impl.flavor

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo

/**
 * Fallback notifier for Linux when a D-Bus session is not available.
 * Shows an IDE notification instead of attempting libnotify.
 */
class LinuxIdeFallbackNotifier : SystemNotifierFlavor() {

    override fun notify(name: String, title: String, description: String) {
        // No Project context available in the interface, so we pass null.
        val group = NotificationGroupManager.getInstance()
            .getNotificationGroup("IntelliJ JNA Notifier")
        group.createNotification(title, description, NotificationType.INFORMATION)
            .notify(null as Project?)
    }

    override fun isApplicable(): Boolean = SystemInfo.isLinux && !hasDbusSession()

    private fun hasDbusSession(): Boolean {
        val addr = System.getenv("DBUS_SESSION_BUS_ADDRESS")
        return !addr.isNullOrBlank()
    }
}
