package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import co.anbora.labs.system.notifier.ide.notifications.SystemNotifications
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.openapi.project.ProjectManager
import java.awt.Toolkit

object FallbackNotificationsImpl: SystemNotifier {
    override fun notify(name: String, title: String, description: String) {
        // No Project context available in the interface, so we pass null.
        SystemNotifications.fallbackNotifications(title, description, ProjectManager.getInstance().defaultProject)

        if (notifierSettings.addSoundToSystemNotifications()) {
            Toolkit.getDefaultToolkit().beep()
        }
    }
}