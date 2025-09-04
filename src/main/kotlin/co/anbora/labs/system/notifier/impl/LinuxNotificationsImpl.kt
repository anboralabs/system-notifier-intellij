package co.anbora.labs.system.notifier.impl

import co.anbora.labs.system.notifier.SystemNotifier
import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.ui.findAppIcon
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

object LinuxNotificationsImpl: SystemNotifier {

    private var myLibNotify: LibNotify? = null
    private var myIcon: String? = null
    private val myLock = Any()
    private var myDisposed = false

    private interface LibNotify : Library {
        fun notify_init(appName: String?): Int
        fun notify_uninit()
        fun notify_notification_new(summary: String?, body: String?, icon: String?): Pointer?
        fun notify_notification_show(notification: Pointer?, error: Pointer?): Int
    }


    init {
        myLibNotify = Native.load("libnotify.so.4", LibNotify::class.java)

        val appName = ApplicationNamesInfo.getInstance().productName
        check(myLibNotify?.notify_init(appName) != 0) { "notify_init failed" }

        val icon: String? = findAppIcon()
        myIcon = icon ?: "dialog-information"

        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe<AppLifecycleListener>(AppLifecycleListener.TOPIC, object : AppLifecycleListener {
            override fun appClosing() {
                synchronized(myLock) {
                    myDisposed = true
                    myLibNotify?.notify_uninit()
                }
            }
        })
    }

    override fun notify(name: String, title: String, description: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(myLock) {
                val notification = myLibNotify?.notify_notification_new(title, description, myIcon)
                val result = myLibNotify?.notify_notification_show(notification, null)
                if (result != 0) {

                }
            }
        }
    }
}