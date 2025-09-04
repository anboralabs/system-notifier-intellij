package co.anbora.labs.system.notifier.ide.listeners.notifications

import co.anbora.labs.system.notifier.NOTIFICATIONS_GROUP_ID
import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.notification.Notification
import com.intellij.notification.Notifications

class NotificationsListener: Notifications {

    override fun notify(notification: Notification) {
        if (NOTIFICATIONS_GROUP_ID == notification.groupId)
            return

        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify(notification.groupId, notification.title, notification.content)
        }
    }
}