package co.anbora.labs.system.notifier.ide.startup

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class LicenseStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify("test", "test", "test")
        }
    }
}