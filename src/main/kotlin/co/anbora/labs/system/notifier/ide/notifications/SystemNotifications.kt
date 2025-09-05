package co.anbora.labs.system.notifier.ide.notifications

import co.anbora.labs.system.notifier.NOTIFICATIONS_GROUP_ID
import co.anbora.labs.system.notifier.ide.icons.SystemIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project

object SystemNotifications {

    @JvmStatic
    fun createNotification(
        title: String,
        content: String,
        type: NotificationType,
        vararg actions: AnAction
    ): Notification {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATIONS_GROUP_ID)
            .createNotification(content, type)
            .setTitle(title)
            .setIcon(SystemIcons.ICON)

        for (action in actions) {
            notification.addAction(action)
        }

        return notification
    }

    @JvmStatic
    fun showNotification(notification: Notification, project: Project?) {
        try {
            notification.notify(project)
        } catch (e: Exception) {
            notification.notify(project)
        }
    }

    fun fallbackNotifications(title: String, content: String, project: Project?) {
        val notification = createNotification(title, content, NotificationType.INFORMATION)
        showNotification(notification, project)
    }
}