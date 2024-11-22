package com.uooc.annotation

enum class AssembleType {
    Factory,
    Singleton,
    DisableAssemble
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleAssemble(
    val type: AssembleType = AssembleType.Factory
)
