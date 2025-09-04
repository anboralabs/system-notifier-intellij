package co.anbora.labs.system.notifier.ide.listeners.externalSystem

import co.anbora.labs.system.notifier.ide.listeners.ListenersManager
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener

class ExternalSystemFinishedListener: ExternalSystemTaskNotificationListener {

    override fun onEnd(
        projectPath: String,
        id: ExternalSystemTaskId
    ) {
        if (!notifierSettings.showExternalSystemAsSystemNotifications()) {
            return
        }
        ListenersManager.notify("System Notifier Plugin", "System Notifier Plugin", "Task finished: $projectPath")
    }
}