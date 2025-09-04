package co.anbora.labs.system.notifier.ide.listeners

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.notification.Notification
import com.intellij.notification.Notifications

class NotificationsListener: Notifications {

    override fun notify(notification: Notification) {
        if (notification.groupId == "5f86eafc-1166-4714-b0d7-8045430751f1_systemNotifier")
            return

        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify(notification.groupId, notification.title, notification.content)
        }
    }
}