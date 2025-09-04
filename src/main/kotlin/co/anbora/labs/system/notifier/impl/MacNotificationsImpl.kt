package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.mac.foundation.Foundation
import java.util.*

object MacNotificationsImpl: SystemNotifier {

    init {
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(AppLifecycleListener.TOPIC, object : AppLifecycleListener {
            override fun appClosing() {
                cleanupDeliveredNotifications()
            }
        })

        ObjectiveCRuntime.ensureUserNotificationCenterDelegateInstalled()
    }

    private fun cleanupDeliveredNotifications() {
        val center = Foundation.invoke(Foundation.getObjcClass("NSUserNotificationCenter"), "defaultUserNotificationCenter")
        Foundation.invoke(center, "removeAllDeliveredNotifications")
    }

    override fun notify(name: String, title: String, description: String) {
        val notification =
            Foundation.invoke(Foundation.getObjcClass("NSUserNotification"), "new", *arrayOfNulls<Any>(0))
        Foundation.invoke(
            notification,
            "setTitle:",
            *arrayOf<Any>(Foundation.nsString(StringUtil.stripHtml(title, true).replace("%", "%%")))
        )
        Foundation.invoke(
            notification,
            "setInformativeText:",
            *arrayOf<Any>(Foundation.nsString(StringUtil.stripHtml(description, true).replace("%", "%%")))
        )
        Foundation.invoke(
            notification,
            "setIdentifier:",
            *arrayOf<Any>(Foundation.nsString(UUID.randomUUID().toString()))
        )
        if (notifierSettings.addSoundToSystemNotifications()) {
            Foundation.invoke(
                notification,
                "setSoundName:",
                *arrayOf<Any>(Foundation.nsString("Glass"))
            )
        }
        val center = Foundation.invoke(
            Foundation.getObjcClass("NSUserNotificationCenter"),
            "defaultUserNotificationCenter"
        )
        Foundation.invoke(center, "deliverNotification:", notification)
    }
}