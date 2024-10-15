package com.github.shu

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * A KSP (Kotlin Symbol Processing) processor that processes class annotated with `@AutoRegister`.
 * This processor checks if the annotated classes have a no-argument constructor or if all their
 * constructor parameters hava default values. If neither condition is met, an error is logged.
 *
 * @param logger The KSP logger used for logging errors during processing.
 */
class AutoRegisterProcessor(private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val autoRegisterClasses = resolver.getSymbolsWithAnnotation("anticope.rejects.annotation.AutoRegister")
        autoRegisterClasses.forEach { classSymbol ->
            if (classSymbol is KSClassDeclaration) {
                validateClass(classSymbol)
            }
        }
        return emptyList()
    }

    private fun validateClass(classSymbol: KSClassDeclaration) {
        val constructors = classSymbol.getConstructors()
        val hasDefaultConstructor = constructors.any { constructor ->
            constructor.parameters.isEmpty() || constructor.parameters.all { it.hasDefault }
        }
        if (!hasDefaultConstructor) {
            val message =
                "${classSymbol.simpleName.asString()} must have a no-arg constructor or " +
                "all parameters must have default values"
            logger.error(message)
        }
    }
}

class AutoRegisterProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoRegisterProcessor(environment.logger)
    }
}