package co.anbora.labs.system.notifier.ide.listeners

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.notification.Notification

object ListenersManager {

    fun notify(notification: Notification) {
        this.notify(notification.groupId, notification.title, notification.content)
    }

    fun notify(name: String, title: String, description: String) {
        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify(name, title, description)
        }
    }
}