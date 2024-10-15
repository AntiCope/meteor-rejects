package anticope.rejects.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.net.JarURLConnection
import java.util.jar.JarFile
import kotlin.reflect.KClass

class PackageScanner {
    companion object {
        private val log = LoggerFactory.getLogger(PackageScanner::class.java)
        private val loader = Thread.currentThread().contextClassLoader

        /**
         * Finds all Kotlin classes in the specified package and filters them based on the provided criteria.
         *
         * @param packageName The name of the package to search, formatted as `com.example.package`.
         * @param filter An optional filtering function that takes a [KClass] parameter and returns a boolean value.
         *               Only classes for which this function returns `true` will be included in the results.
         *               Defaults to a filter that accepts all classes (returns `true`).
         * @return A list of [KClass] objects that match the specified criteria.
         *
         * @throws ClassNotFoundException If the classes in the specified package cannot be found.
         *
         * Usage example:
         * ```
         * val classes = findKClasses("com.example") { it.annotations.any { annotation -> annotation is MyAnnotation } }
         * ```
         * This will return all classes in the `com.example` package that are annotated with `@MyAnnotation`.
         */
        @JvmStatic
        fun findKClasses(packageName: String, filter: (KClass<*>) -> Boolean = { true }): List<KClass<*>> {
            return PackageScanner().findKClasses(packageName).filter(filter)
        }
    }

    private fun findKClasses(packageName: String): List<KClass<*>> {
        val result = mutableListOf<KClass<*>>()
        val path = packageName.replace('.', '/')
        loader.getResources(path).asSequence().forEach { url ->
            result.addAll(when (url.protocol) {
                "file" -> findKClassInFile(File(url.path), packageName)
                "jar" -> (url.openConnection() as JarURLConnection).jarFile.use { findKClassInJar(it, path) }
                else -> emptyList()
            })
        }
        return result
    }

    private fun findKClassInFile(file: File, packageName: String): List<KClass<*>> =
        file.walkTopDown()
            .filter { it.isFile && it.name.endsWith(".class") }
            .mapNotNull {
                it.name.toClassName(packageName).let { className -> loader.loadClass(className).kotlinOrNull() }
            }
            .toList()

    private fun findKClassInJar(jarFile: JarFile, path: String): List<KClass<*>> =
        jarFile.entries().asSequence()
            .filter { it.name.startsWith(path) && it.name.endsWith(".class") }
            .mapNotNull { loader.loadClass(it.name.toClassName()).kotlinOrNull() }
            .toList()

    private fun String.toClassName(prefix: String = "") =
        "$prefix.${substringBeforeLast(".class").replace('/', '.')}".removePrefix(".")

    private fun Class<*>.kotlinOrNull(): KClass<*>? =
        runCatching { kotlin }.onFailure { log.error(it.message) }.getOrNull()
}

