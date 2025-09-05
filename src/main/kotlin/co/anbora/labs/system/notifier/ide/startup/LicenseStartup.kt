package co.anbora.labs.system.notifier.ide.startup

import co.anbora.labs.system.notifier.license.CheckLicense
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit

class LicenseStartup: ProjectActivity {
    override suspend fun execute(project: Project) {
        // Schedule the license validation 5 minutes after startup to ensure LicensingFacade is initialized
        AppExecutorUtil.getAppScheduledExecutorService().schedule({
            val licensed = CheckLicense.isLicensed() ?: false
            /*
             Why check !project.isDisposed here?
             - The check is delayed by 5 minutes. In that window the user may close the project or the IDE may dispose it.
             - Showing a registration dialog for a disposed project can cause exceptions, UI glitches, or show it on the Welcome Screen
               after the project is gone (bad UX).
             - Guarding with !project.isDisposed ensures we only request the license dialog for a live project context and skip otherwise.
            */
            if (!licensed && !project.isDisposed) {
                CheckLicense.requestLicense("Support Plugin")
            }
        }, 5, TimeUnit.MINUTES)
    }
}