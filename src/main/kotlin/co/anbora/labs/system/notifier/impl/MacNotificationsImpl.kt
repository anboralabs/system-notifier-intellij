package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.mac.foundation.Foundation
import java.util.UUID

object MacNotificationsImpl: SystemNotifier {

    init {
        ObjectiveCRuntime.ensureUserNotificationCenterDelegateInstalled()
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
        Foundation.invoke(
            notification,
            "setSoundName:",
            *arrayOf<Any>(Foundation.nsString("Glass"))
        )
        val center = Foundation.invoke(
            Foundation.getObjcClass("NSUserNotificationCenter"),
            "defaultUserNotificationCenter"
        )
        Foundation.invoke(center, "deliverNotification:", notification)
    }
}