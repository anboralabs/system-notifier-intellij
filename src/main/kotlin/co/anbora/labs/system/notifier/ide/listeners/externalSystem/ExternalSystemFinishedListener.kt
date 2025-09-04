package co.anbora.labs.system.notifier.ide.listeners.externalSystem

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener

class ExternalSystemFinishedListener: ExternalSystemTaskNotificationListener {

    override fun onEnd(
        projectPath: String,
        id: ExternalSystemTaskId
    ) {
        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify("System Notifier Plugin", "System Notifier Plugin", "Task finished: $projectPath")
        }
    }
}