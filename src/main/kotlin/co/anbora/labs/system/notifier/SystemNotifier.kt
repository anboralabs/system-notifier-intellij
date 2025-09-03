package co.anbora.labs.system.notifier

interface SystemNotifier {
    fun notify(name: String, title: String, description: String)
}