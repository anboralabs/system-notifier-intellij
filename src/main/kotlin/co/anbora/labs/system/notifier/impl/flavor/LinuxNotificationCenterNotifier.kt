package co.anbora.labs.system.notifier.impl.flavor

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import co.anbora.labs.system.notifier.impl.LinuxNotificationsImpl
import com.intellij.openapi.util.SystemInfo

class LinuxNotificationCenterNotifier: SystemNotifierFlavor() {
    override fun notify(name: String, title: String, description: String) {
        LinuxNotificationsImpl.notify(name, title, description)
    }

    override fun isApplicable() = SystemInfo.isLinux
}