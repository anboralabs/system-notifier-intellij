package co.anbora.labs.system.notifier.impl.flavor

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import co.anbora.labs.system.notifier.impl.MacNotificationsImpl
import com.intellij.openapi.util.SystemInfo

class MacCenterMessageNotifier: SystemNotifierFlavor() {
    override fun notify(name: String, title: String, description: String) {
        MacNotificationsImpl.notify(name, title, description)
    }

    override fun isApplicable() = SystemInfo.isMac
}