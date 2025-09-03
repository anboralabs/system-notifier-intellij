package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.loadSmallApplicationIcon
import com.intellij.ui.scale.ScaleContext
import com.intellij.util.IconUtil.toImage
import com.intellij.util.ui.ImageUtil
import com.intellij.util.ui.UIUtil
import java.awt.Image
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Window
import java.awt.event.ActionEvent
import javax.swing.Icon

object WindowNotificationsImpl: SystemNotifier {

    var tooltip: String = "System Notifier"

    private val myTrayIcon: TrayIcon = TrayIcon(createImage(), tooltip)
    private val myType: TrayIcon.MessageType = TrayIcon.MessageType.INFO

    init {
        myTrayIcon.setImageAutoSize(true)
        SystemTray.getSystemTray().add(myTrayIcon)

        myTrayIcon.addActionListener { _: ActionEvent? ->
            val frame = IdeFocusManager.getGlobalInstance().lastFocusedFrame
            if (frame is Window) {
                UIUtil.toFront(frame as Window?)
            }
        }
    }

    private fun createImage(): Image {
        val icon: Icon = loadSmallApplicationIcon(ScaleContext.create(), 16)
        return ImageUtil.toBufferedImage(toImage(icon))
    }

    override fun notify(name: String, title: String, description: String) {
        myTrayIcon.displayMessage(title, description, myType)
    }
}