package co.anbora.labs.system.notifier.ide.settings

import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import co.anbora.labs.system.notifier.ide.settings.NotifierSettingsService.Companion.notifierSettings
import com.intellij.openapi.util.SystemInfo
import java.util.Objects
import javax.swing.JComponent

class NotificationsSettingsView {

    private lateinit var addRunConfigurationsSystemNotifications: JBCheckBox
    private lateinit var addSoundToSystemNotifications: JBCheckBox
    private lateinit var addSoundMacToSystemNotifications: JBCheckBox
    private lateinit var showExternalSystemAsSystemNotifications: JBCheckBox
    private lateinit var showAllIDENotificationsAsSystemNotification: JBCheckBox

    init {
        createUI()
    }

    private fun createUI() {
        addRunConfigurationsSystemNotifications = JBCheckBox()
        addSoundToSystemNotifications = JBCheckBox()
        addSoundMacToSystemNotifications = JBCheckBox()
        showExternalSystemAsSystemNotifications = JBCheckBox()
        showAllIDENotificationsAsSystemNotification = JBCheckBox()

        addSoundMacToSystemNotifications.isEnabled = SystemInfo.isMac
    }

    fun createComponent(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()

        formBuilder.addLabeledComponent("Show notifications for run configurations execution (Run, Debug): ", addRunConfigurationsSystemNotifications)
            .addLabeledComponent("Add sound to System notification (Default): ", addSoundToSystemNotifications)
            .addLabeledComponent("Add sound to System notification (Mac OS): ", addSoundMacToSystemNotifications)
            .addLabeledComponent("Show external system task (Gradle, Maven, ...) as Notifications: ", showExternalSystemAsSystemNotifications)
            .addLabeledComponent("Show all IDE notifications as System Notifications: ", showAllIDENotificationsAsSystemNotification)

        val panel = formBuilder.panel

        panel.border = IdeBorderFactory.createTitledBorder("System Notifications Config")

        return panel
    }

    fun isModified(): Boolean {
        return !Objects.equals(addRunConfigurationsSystemNotifications.isSelected, notifierSettings.addRunConfigurationsSystemNotifications())
                || !Objects.equals(addSoundToSystemNotifications.isSelected, notifierSettings.addSoundToSystemNotifications())
                || !Objects.equals(addSoundMacToSystemNotifications.isSelected, notifierSettings.addSoundMacToSystemNotifications())
                || !Objects.equals(showExternalSystemAsSystemNotifications.isSelected, notifierSettings.showExternalSystemAsSystemNotifications())
                || !Objects.equals(showAllIDENotificationsAsSystemNotification.isSelected, notifierSettings.showAllIDENotificationsAsSystemNotification())
    }

    fun reset() {
        addRunConfigurationsSystemNotifications.isSelected = notifierSettings.addRunConfigurationsSystemNotifications()
        addSoundToSystemNotifications.isSelected = notifierSettings.addSoundToSystemNotifications()
        addSoundMacToSystemNotifications.isSelected = notifierSettings.addSoundMacToSystemNotifications()
        showExternalSystemAsSystemNotifications.isSelected = notifierSettings.showExternalSystemAsSystemNotifications()
        showAllIDENotificationsAsSystemNotification.isSelected = notifierSettings.showAllIDENotificationsAsSystemNotification()
    }

    fun apply() {
        notifierSettings.setAddRunConfigurationsSystemNotifications(addRunConfigurationsSystemNotifications.isSelected)
        notifierSettings.setAddSoundToSystemNotifications(addSoundToSystemNotifications.isSelected)
        notifierSettings.setAddSoundMacToSystemNotifications(addSoundMacToSystemNotifications.isSelected)
        notifierSettings.setShowExternalSystemAsSystemNotifications(showExternalSystemAsSystemNotifications.isSelected)
        notifierSettings.setShowAllIDENotificationsAsSystemNotification(showAllIDENotificationsAsSystemNotification.isSelected)
    }

}