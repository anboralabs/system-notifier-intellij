package co.anbora.labs.system.notifier.ide.listeners.notifications

import co.anbora.labs.system.notifier.NOTIFICATIONS_GROUP_ID
import co.anbora.labs.system.notifier.ide.listeners.ListenersManager
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import java.awt.Toolkit

class NotificationsListener: Notifications {

    override fun notify(notification: Notification) {
        if (NOTIFICATIONS_GROUP_ID == notification.groupId && notifierSettings.showAllIDENotificationsAsSystemNotification())
            return

        ListenersManager.notify(notification.groupId, notification.title, notification.content)
    }
}