package co.anbora.labs.system.notifier.impl.flavor

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import co.anbora.labs.system.notifier.impl.LinuxNotificationsImpl
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.ui.GraphicsUtil

class LinuxNotificationCenterNotifier: SystemNotifierFlavor() {
    override fun notify(name: String, title: String, description: String) {
        LinuxNotificationsImpl.notify(name, title, description)
    }

    override fun isApplicable(): Boolean = SystemInfo.isLinux && !GraphicsUtil.isRemoteEnvironment() && hasDbusSession()

    private fun hasDbusSession(): Boolean {
        // Heuristic: a usable Libnotify setup normally requires a session D-Bus
        // presence. We check the common environment variable.
        val addr = System.getenv("DBUS_SESSION_BUS_ADDRESS")
        return !addr.isNullOrBlank()
    }
}