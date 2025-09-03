package co.anbora.labs.system.notifier

import com.intellij.openapi.extensions.ExtensionPointName

abstract class SystemNotifierFlavor {

    /**
     * Flavor is added to result in [getApplicableNotifiers] if this method returns true.
     * @return whether this flavor is applicable.
     */
    protected open fun isApplicable(): Boolean = true

    abstract fun notify(name: String, title: String, description: String)

    companion object {
        private val EP_NAME: ExtensionPointName<SystemNotifierFlavor> =
            ExtensionPointName.create("co.anbora.labs.system.notifier.systemNotifier")

        fun getApplicableNotifiers(): List<SystemNotifierFlavor> =
            EP_NAME.extensionList.filter { it.isApplicable() }
    }
}