package co.anbora.labs.system.notifier.ide.settings

import co.anbora.labs.system.notifier.PLUGIN_NAME
import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.SwingHelper
import com.intellij.util.ui.UIUtil
import javax.swing.BorderFactory
import javax.swing.JComponent

class NotificationsConfigurable: Configurable {

    val view = NotificationsSettingsView()

    override fun getDisplayName() = PLUGIN_NAME

    override fun createComponent(): JComponent {
        val formBuilder = FormBuilder.createFormBuilder()
            .setHorizontalGap(UIUtil.DEFAULT_HGAP)
            .setVerticalGap(UIUtil.DEFAULT_VGAP)

        val panel = formBuilder
            .addComponent(view.createComponent())
            .addSeparator(4)
            .addVerticalGap(4)
            .panel

        val centerPanel = SwingHelper.wrapWithHorizontalStretch(panel)
        centerPanel.border = BorderFactory.createEmptyBorder(5, 0, 0, 0)

        return centerPanel
    }

    override fun isModified() = view.isModified()

    override fun apply() = view.apply()

    override fun reset() = view.reset()
}