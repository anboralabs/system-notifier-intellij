package co.anbora.labs.system.notifier.ide.startup

import co.anbora.labs.system.notifier.ide.listeners.executions.RunConfigurationListener
import com.intellij.execution.ExecutionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class RunConfigurationStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        val connection = project.messageBus.connect()
        connection.subscribe(ExecutionManager.EXECUTION_TOPIC, RunConfigurationListener(project))
    }
}