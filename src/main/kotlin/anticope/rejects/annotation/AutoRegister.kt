package anticope.rejects.annotation

/**
 * Annotation used to mark classes that should be automatically registered
 * into the Meteor Commands and Modules systems. Classes annotated with this will
 * be processed at runtime to ensure they are included in both command and
 * module management systems.
 *
 * Usage example:
 * ```
 * @AutoRegister
 * class ExampleModule: Module(MeteorAddon.CATEGORY, "example-module", "The description")
 * // This module will be automatically registered
 * // No need to manually add it to the Modules
 * // Modules.get().add(ExampleModule())
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AutoRegister
