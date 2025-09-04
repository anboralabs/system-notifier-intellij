package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

object FallbackNotificationsImpl: SystemNotifier {
    override fun notify(name: String, title: String, description: String) {
        // No Project context available in the interface, so we pass null.
        val group = NotificationGroupManager.getInstance()
            .getNotificationGroup("5f86eafc-1166-4714-b0d7-8045430751f1_systemNotifier")
        group.createNotification(title, description, NotificationType.INFORMATION)
            .notify(null)
    }
}