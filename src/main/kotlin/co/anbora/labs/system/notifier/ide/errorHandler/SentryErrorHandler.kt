package co.anbora.labs.system.notifier.ide.errorHandler

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.Consumer
import java.awt.Component

class SentryErrorHandler: ErrorReportSubmitter() {

    override fun getReportActionText(): String = "Report to Anbora-Labs"

    override fun submit(
        events: Array<IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val context = DataManager.getInstance().getDataContext(parentComponent)
        val project: Project? = CommonDataKeys.PROJECT.getData(context)

        SendIssueBackgroundTask(project, pluginDescriptor, events) {
            ApplicationManager.getApplication().invokeLater {
                Messages.showInfoMessage(parentComponent, "Thank you for submitting your report!", "Error Report")
                consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
            }
        }.queue()
        return true
    }
}