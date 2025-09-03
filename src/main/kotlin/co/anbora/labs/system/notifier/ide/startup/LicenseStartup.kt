package co.anbora.labs.system.notifier.ide.startup

import co.anbora.labs.system.notifier.ide.IntelliJJNANotificationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.SystemNotifications
import com.intellij.ui.mac.foundation.Foundation
import java.util.UUID

class LicenseStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        val notifications = SystemNotifications.getInstance()

        val title = "System Notifications"
        val description = "This is a System Notifications"

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
        // Ensure each notification is treated as unique so macOS always presents it
        Foundation.invoke(
            notification,
            "setIdentifier:",
            *arrayOf<Any>(Foundation.nsString(UUID.randomUUID().toString()))
        )
        // Add default sound to draw user's attention
        Foundation.invoke(
            notification,
            "setSoundName:",
            *arrayOf<Any>(Foundation.nsString("NSUserNotificationDefaultSoundName"))
        )

        Foundation.allocateObjcClassPair()

        val center = Foundation.invoke(
            Foundation.getObjcClass("NSUserNotificationCenter"),
            "defaultUserNotificationCenter",
            *arrayOfNulls<Any>(0)
        )
        Foundation.invoke(center, "deliverNotification:", *arrayOf<Any>(notification))

        //IntelliJJNANotificationManager.notify(project, title, description, soundName = "Glass")
    }
}