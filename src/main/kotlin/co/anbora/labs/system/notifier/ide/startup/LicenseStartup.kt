package co.anbora.labs.system.notifier.ide.startup

import co.anbora.labs.system.notifier.SystemNotifierFlavor
import co.anbora.labs.system.notifier.ide.IntelliJJNANotificationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.SystemNotifications
import com.intellij.ui.mac.foundation.Foundation
import java.util.UUID

class LicenseStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        SystemNotifierFlavor.getApplicableNotifiers().forEach {
            it.notify("test", "test", "test")
        }
    }
}