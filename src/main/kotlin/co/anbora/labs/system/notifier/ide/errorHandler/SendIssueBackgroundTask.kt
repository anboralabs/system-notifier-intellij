package co.anbora.labs.system.notifier.ide.errorHandler

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManager
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import io.sentry.Hub
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import java.util.function.Consumer

class SendIssueBackgroundTask(
    project: Project?,
    private val events: Array<out IdeaLoggingEvent>,
    private val consumer: Consumer<Unit>
): Task.Backgroundable(project, "Sending error Report") {
    override fun run(indicator: ProgressIndicator) {

        val options = SentryOptions()
        options.dsn = "https://293ab69618bc370af10760eb3922f2b7@o370368.ingest.us.sentry.io/4509967737356288"
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
        val hub = Hub(options)

        val plugin = PluginManager.getInstance().findEnabledPlugin(PluginId.getId("co.anbora.labs.system.notifier"))

        events.forEach {
            val event = SentryEvent()
            event.level = SentryLevel.ERROR

            event.release = plugin?.version
            // set server name to empty to avoid tracking personal data
            event.serverName = ""

            event.throwable = it.throwable

            event.setExtra("last_action", IdeaLogger.ourLastActionId)
            hub.captureEvent(event)
        }

        consumer.accept(Unit)
    }
}