package co.anbora.labs.system.notifier.ide.listeners.executions

import co.anbora.labs.system.notifier.ide.listeners.ListenersManager
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project

class RunConfigurationListener(private val project: Project): ExecutionListener {

    override fun processStarted(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler
    ) {
        if (!notifierSettings.addRunConfigurationsSystemNotifications()) {
            return
        }
        ListenersManager.notify("System Notifier Plugin", "System Notifier Plugin", "Task started: ${env.runProfile.name}")
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        if (!notifierSettings.addRunConfigurationsSystemNotifications()) {
            return
        }
        ListenersManager.notify("System Notifier Plugin", "System Notifier Plugin", "Task finished: ${env.runProfile.name}")
    }
}