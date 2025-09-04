package co.anbora.labs.system.notifier.ide.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute

@State(
    name = "SystemNotifierSettings",
    storages = [Storage("d5fd7ba3-88a2-48b8-a814-370c159d3435_SystemNotifierSettings.xml")]
)
class NotifierSettingsService: PersistentStateComponent<NotifierSettingsService.ToolchainState?> {
    private var state = ToolchainState()

    fun addRunConfigurationsSystemNotifications(): Boolean = state.addRunConfigurationsSystemNotifications

    fun addSoundToSystemNotifications(): Boolean = state.addSoundToSystemNotifications

    fun showExternalSystemAsSystemNotifications(): Boolean = state.showExternalSystemAsSystemNotifications

    fun showAllIDENotificationsAsSystemNotification(): Boolean = state.showAllIDENotificationsAsSystemNotification

    fun setAddRunConfigurationsSystemNotifications(value: Boolean) {
        state.addRunConfigurationsSystemNotifications = value
    }

    fun setAddSoundToSystemNotifications(value: Boolean) {
        state.addSoundToSystemNotifications = value
    }

    fun setShowExternalSystemAsSystemNotifications(value: Boolean) {
        state.showExternalSystemAsSystemNotifications = value
    }

    fun setShowAllIDENotificationsAsSystemNotification(value: Boolean) {
        state.showAllIDENotificationsAsSystemNotification = value
    }

    override fun getState() = state

    override fun loadState(state: ToolchainState) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    class ToolchainState {
        @Attribute("addRunConfigurationsSystemNotifications")
        var addRunConfigurationsSystemNotifications: Boolean = true

        @Attribute("addSoundToSystemNotifications")
        var addSoundToSystemNotifications: Boolean = true

        @Attribute("showExternalSystemAsSystemNotifications")
        var showExternalSystemAsSystemNotifications: Boolean = true

        @Attribute("showAllIDENotificationsAsSystemNotification")
        var showAllIDENotificationsAsSystemNotification: Boolean = false
    }

    companion object {
        val notifierSettings
            get() = service<NotifierSettingsService>()
    }
}