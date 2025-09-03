package co.anbora.labs.system.notifier.impl.flavor

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import co.anbora.labs.system.notifier.impl.WindowNotificationsImpl
import com.intellij.openapi.util.SystemInfo

class WindowActionCenterNotifier: SystemNotifierFlavor() {
    override fun notify(name: String, title: String, description: String) {
        WindowNotificationsImpl.notify(name, title, description)
    }

    override fun isApplicable() = SystemInfo.isWindows
}