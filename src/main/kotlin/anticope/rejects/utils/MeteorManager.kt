package anticope.rejects.utils

import anticope.rejects.annotation.AutoRegister
import meteordevelopment.meteorclient.commands.Command
import meteordevelopment.meteorclient.commands.Commands
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.systems.modules.Modules
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

private typealias Condition = (KClass<*>) -> Boolean

private inline fun <reified T> findKClasses(pkg: String, crossinline condition: Condition = { true }) =
    PackageScanner.findKClasses(pkg) { kClass ->
        kClass.isSubclassOf(T::class) && condition(kClass)
    }

private inline fun <reified T> registerClasses(packageName: String, addInstance: (T) -> Unit) {
    val result = findKClasses<T>(packageName) { kClass ->
        kClass.annotations.any { it is AutoRegister } && !kClass.isAbstract
    }
    result.forEach { clazz ->
        val instance = clazz.createInstance()
        addInstance(instance as T)
    }
}

/**
 * Registers all modules in the specified package that are annotated with `@AutoRegister`.
 *
 * This function scans the given package for classes that extend [Module] and have the `@AutoRegister` annotation,
 * creating instances of those classes and adding them to the [Modules] system.
 *
 * @param packageName The name of the package to search for modules, formatted as `com.example.module`.
 *
 * @throws ClassNotFoundException If the classes in the specified package cannot be found.
 *
 * Usage example:
 * ```
 * moduleRegister("com.github.shu.module")
 * ```
 * This will register all auto-registered modules from the `com.github.shu.module` package.
 */
fun moduleRegister(packageName: String) {
    registerClasses<Module>(packageName, Modules.get()::add)
}

/**
 * Registers all commands in the specified package that are annotated with `@AutoRegister`.
 *
 * This function scans the given package for classes that extend [Command] and have the `@AutoRegister` annotation,
 * creating instances of those classes and adding them to the [Commands] system.
 *
 * @param packageName The name of the package to search for commands, formatted as `com.example.command`.
 *
 * @throws ClassNotFoundException If the classes in the specified package cannot be found.
 *
 * Usage example:
 * ```
 * commandRegister("com.github.shu.command")
 * ```
 * This will register all auto-registered commands from the `com.github.shu.command` package.
 */
fun commandRegister(packageName: String) {
    registerClasses<Command>(packageName, Commands::add)
}