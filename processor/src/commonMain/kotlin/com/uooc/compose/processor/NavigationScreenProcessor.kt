@file:Suppress("unused", "NonAsciiCharacters", "ComposableNaming")

package com.uooc.compose.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.PlatformInfo
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.Modifier
import com.uooc.annotation.AssembleType
import com.uooc.annotation.AssembleType.DisableAssemble
import com.uooc.annotation.AssembleType.Factory
import com.uooc.annotation.AssembleType.Singleton
import com.uooc.annotation.ModuleAssemble
import com.uooc.annotation.ProcessorExcludeByScreen
import java.io.File

class NavigationScreenProcessorProvider : SymbolProcessorProvider {
    private lateinit var processor: NavigationScreenProcessor
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        // 妈的,不执行ksp的代码
        if (!::processor.isInitialized) {
            processor = NavigationScreenProcessor(environment)
        }
        return processor
    }
}

class NavigationScreenProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator

    private val logger = environment.logger

    private var packageName: String =
        environment.options["packageName"] ?: "org.uooc.compose.generated"
    private var buildDir: String = environment.options["buildDir"] ?: ""
    private var platforms: List<Pair<String, PlatformInfo>> = environment.platforms.map {
        it.platformName.split(" ").firstOrNull() to it
    }.filterNot {
        it.first == null
    }.map {
        it.first!! to it.second
    }

    private var processOnlyOnceFlag = true

    init {
        logger.warn("environment.options:${environment.options}")
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!processOnlyOnceFlag) return emptyList()
        processOnlyOnceFlag = false
        // /Volumes/Extra/Github/KMMCompose/composeApp/src/commonMain/kotlin/org/uooc/compose/component/BasicScreen.kt
        // 获取所有继承BasicScreen的类,BasicScreen是一个普通的kotlin class
        // 获取BasicScreen类的声明

        val allFiles = resolver.getAllFiles()
        val allDeclaration = allFiles
            .flatMap { it.declarations }

        processScreen(resolver,allDeclaration)
        processModule(resolver,allDeclaration)

        processResource(resolver, allFiles)
        return emptyList()
    }

    private fun processResource(resolver: Resolver, allFile: Sequence<KSFile>) {
        val pairList = allFile.toList().flatMap {
            val file = it
            it.declarations.filterIsInstance<KSClassDeclaration>().map {
                it to file
            }
        }.toList().mapNotNull { pair ->
            try {
                val classDecl = pair.first
                if(classDecl.classKind == ClassKind.OBJECT && classDecl.simpleName.asString() == "Res"){
                    return@mapNotNull pair
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            return@mapNotNull null
        }.sortedBy { it.first.simpleName.asString() }

        if(pairList.isNotEmpty()){
            val existFileDir = File(
                "$buildDir/generated/ksp/metadata/commonMain/kotlin/${
                    packageName.replace(
                        ".",
                        "/"
                    )
                }/"
            )
            val existFile = File(existFileDir, "GeneratedResFunctions.kt")
            if (!existFileDir.exists()) {
                existFileDir.mkdirs()
            } else {
                if (existFile.exists()) {
                    if (existFile.isDirectory) {
                        existFile.delete()
                    }
                }
            }
            val fileStream =
                codeGenerator.createNewFile(
                    dependencies = Dependencies(
                        aggregating = false,
                        sources = resolver.getAllFiles().toList().toTypedArray()
                    ),
                    packageName = packageName,
                    fileName = existFile.nameWithoutExtension
                )
            fileStream.bufferedWriter().use {
                it.write("package org.uooc.compose.generated\n\n")
                pairList.forEach{ clazz->
                    it.write("import ${clazz.first.qualifiedName?.asString()}\n\n")
                }
                pairList.forEach{ clazz->
                    val file:KSFile = clazz.second
                    val path = file.filePath.let {
                        return@let File(it).readLines().mapNotNull { line->
                            if(line.contains("readResourceBytes(")){
                                val path = line.substringAfter("readResourceBytes(")
                                    .substringBefore("+")
                                    .replace("\"","")
                                    .replace(" ","")
                                return@mapNotNull path
                            }else null
                        }.firstOrNull()
                    }
                    it.write("fun ${clazz.first.simpleName.asString()}.resource(path:String):String{\n")
                    it.write("      return \"$path\$path\"\n")
                    it.write("}\n\n")
                }
                it.flush()
            }

        }
    }

    @OptIn(KspExperimental::class)
    private fun processModule(resolver: Resolver, allDeclaration: Sequence<KSDeclaration>) {
        val clazzList = allDeclaration.filterIsInstance<KSClassDeclaration>().toList().mapNotNull { classDecl ->
                try {
                    if(classDecl.isAnnotationPresent(ModuleAssemble::class) && !classDecl.modifiers.contains(Modifier.ABSTRACT)){
                       return@mapNotNull classDecl
                    }
                    else  if(!classDecl.modifiers.contains(Modifier.ABSTRACT)){
                        // 判断类的父类是否有ModuleAssemble注解
                        var superType = classDecl.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration ?:return@mapNotNull null
                        val qualifiedName = superType.qualifiedName?.asString()?:return@mapNotNull null
                        while (!qualifiedName.equals("Any")
                            && !qualifiedName.equals("kotlin.Any")
                        ) {
                            if (superType.isAnnotationPresent(ModuleAssemble::class)) {
                                return@mapNotNull classDecl
                            }
                            val cla = superType.superTypes.firstOrNull()?.resolve()?.declaration as? KSClassDeclaration
                            if(cla!=null){
                                superType = cla
                            }else{
                                break
                            }
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
                return@mapNotNull null
            }
            .sortedBy { it.simpleName.asString() }

        val existFileDir = File(
            "$buildDir/generated/ksp/metadata/commonMain/kotlin/${
                packageName.replace(
                    ".",
                    "/"
                )
            }/"
        )
        val existFile = File(existFileDir, "GeneratedModuleFunctions.kt")
        if (!existFileDir.exists()) {
            existFileDir.mkdirs()
        } else {
            if (existFile.exists()) {
                if (existFile.isDirectory) {
                    existFile.delete()
                }
            }
        }

        val fileStream =
            codeGenerator.createNewFile(
                dependencies = Dependencies(
                    aggregating = false,
                    sources = resolver.getAllFiles().toList().toTypedArray()
                ),
                packageName = packageName,
                fileName = existFile.nameWithoutExtension
            )
        fileStream.bufferedWriter().use {
            it.write("package org.uooc.compose.generated\n\n")
            it.write("import org.koin.core.module.Module\n\n")
            it.write("import org.koin.dsl.module\n\n")
            clazzList.forEach { classDecl ->
                it.write("import ${classDecl.qualifiedName?.asString()}\n")
            }
            it.write("import org.koin.dsl.module\n\n")
            it.write("fun Module.generatedModules() {\n")
            it.write("    \n")
            clazzList.forEach { classDecl ->
                try {
                    // 递归查找最终的超类
                    fun getFinalSuperAssembleType(
                        classDecl: KSClassDeclaration,
                    ): AssembleType {
                        var currentClass = classDecl

                        val asm = currentClass.getAnnotationsByType(ModuleAssemble::class).firstOrNull()
                        if (asm!=null) {
                            return asm.type
                        }
                        while (true) {
                            val superType = currentClass.superTypes.firstOrNull()?.resolve() ?: break
                            val qualifiedName = superType.declaration.qualifiedName?.asString() ?: break
                            if (
                                qualifiedName != "Any"
                                && qualifiedName != "kotlin.Any"
                            ) {
                                val superClass = superType.declaration as? KSClassDeclaration ?: break
                                currentClass = superClass

                                val asmSuper = currentClass.getAnnotationsByType(ModuleAssemble::class).firstOrNull()
                                if (asmSuper!=null) {
                                    return asmSuper.type
                                }
                            } else break
                        }
                        return DisableAssemble
                    }
                    val moduleAssemble = getFinalSuperAssembleType(classDecl)
                    when(moduleAssemble){
                        Factory ->   it.write("  factory { ${classDecl.simpleName.asString()}() }\n\n")
                        Singleton -> it.write("  single { ${classDecl.simpleName.asString()}() }\n\n")
                        DisableAssemble -> Unit
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            it.write("    \n")
            it.write("}\n")
            it.flush()
        }
    }

    @OptIn(KspExperimental::class)
    private fun processScreen(resolver: Resolver, allDeclaration: Sequence<KSDeclaration>) {
        val basicScreenClass =
            resolver.getClassDeclarationByName(resolver.getKSNameFromString("org.uooc.compose.component.BasicScreen"))
        // 确保BasicScreen类存在
        if (basicScreenClass != null) {
            // 获取所有的类声明
            val classDeclarations = allDeclaration
                .filterIsInstance<KSClassDeclaration>().toList()

            // 递归查找最终的超类
            fun getFinalSuperType(
                classDecl: KSClassDeclaration,
                superClazzName: String
            ): KSClassDeclaration {
                var currentClass = classDecl
                if (currentClass.qualifiedName?.asString() == superClazzName) {
                    return currentClass
                }
                while (true) {
                    val superType = currentClass.superTypes.firstOrNull()?.resolve() ?: break
                    val qualifiedName = superType.declaration.qualifiedName?.asString() ?: break
                    if (
                        !qualifiedName.equals(superClazzName)
                        && !qualifiedName.equals("kotlin.Any")
                        && superType.declaration.modifiers.contains(Modifier.ABSTRACT)
                    ) {
                        val superClass = superType.declaration as? KSClassDeclaration ?: break
                        currentClass = superClass
                    } else break
                }

                return currentClass
            }
            // 过滤出继承自BasicScreen且不是抽象类的类

            val subClasses = classDeclarations.filter { classDecl ->
                classDecl.superTypes.any { superType ->
                    try {
                        val resolvedSuperType =
                            getFinalSuperType(
                                superType.resolve().declaration as KSClassDeclaration,
                                "org.uooc.compose.component.BasicScreen"
                            )
                        // superType 需要判断是否还有上一层的superType
                        // 获取最终的superType
                        // 判断是否是BasicScreen的子类
                        if (resolvedSuperType == basicScreenClass) return@any true
                        if (resolvedSuperType.superTypes.any { it.resolve().declaration == basicScreenClass }) return@any true
                        false
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                } && !classDecl.modifiers.contains(Modifier.ABSTRACT)
            }.toList().filter {
                //还需要过滤掉构造函数需要传参的类
                it.primaryConstructor?.parameters?.isEmpty() ?: false
            }.filter {
                // 获取泛型的类型参数,需要从subClasses中拿到当前类的泛型参数
                val typeArguments = it.superTypes.firstOrNull()?.resolve()?.arguments
                val typeArgumentDecl = typeArguments?.firstNotNullOfOrNull { arg ->
                    arg.type?.resolve()?.declaration as? KSClassDeclaration
                } ?: return@filter false
                var currentClass: KSClassDeclaration? = typeArgumentDecl
                while (currentClass != null) {
                    val curr = currentClass!!
                    if (curr.superTypes.any { superType ->
                            superType.resolve().declaration.qualifiedName?.asString() == "org.uooc.compose.component.BasicViewModel"
                        }) {
                        val hasAnnotation =
                            curr.getAnnotationsByType(ProcessorExcludeByScreen::class)
                                .firstOrNull() != null
                        if (hasAnnotation) {
                            return@filter false
                        }
                    }
                    currentClass = currentClass!!.superTypes.firstOrNull()
                        ?.resolve()?.declaration as? KSClassDeclaration
                }
                return@filter true
            }

            val existFileDir = File(
                "$buildDir/generated/ksp/metadata/commonMain/kotlin/${
                    packageName.replace(
                        ".",
                        "/"
                    )
                }/"
            )
            val existFile = File(existFileDir, "GeneratedScreenComposeFunctions.kt")
            if (!existFileDir.exists()) {
                existFileDir.mkdirs()
    //                existFile.createNewFile()
            } else {
                if (existFile.exists()) {
                    if (existFile.isDirectory) {
                        existFile.delete()
    //                        existFile.createNewFile()
                    }
                }
            }
            val fileStream =
                codeGenerator.createNewFile(
                    dependencies = Dependencies(
                        aggregating = false,
                        sources = resolver.getAllFiles().toList().toTypedArray()
                    ),
                    packageName = packageName,
                    fileName = "GeneratedScreenComposeFunctions"
                )


            try {
                if (subClasses.isEmpty()) {
                    if (existFile.useLines {
                            it.filter {
                                it.contains("fun NavGraphBuilder.generatedScreensNavigation()")
                            }.count() == 0 // 防止重复写入,如果已经写入了e就不再写入
                        }) {
                        fileStream.bufferedWriter().use {
                            it.write("package org.uooc.compose.generated\n\n")
                            it.write("import androidx.navigation.NavGraphBuilder\n\n")
                            it.write("import androidx.compose.runtime.Composable\n\n")
                            it.write("import androidx.navigation.compose.composable\n\n")
                            it.write("import androidx.compose.runtime.remember\n\n")
                            it.write("fun NavGraphBuilder.generatedScreensNavigation() {\n")
                            it.write("}\n")
                            it.flush()
                        }
                    }
                } else {
                    logger.warn("找到的Screen: ${subClasses.joinToString(",")}")
                    // 写入临时文件
                    fileStream.bufferedWriter().use {
                        it.write("package org.uooc.compose.generated\n\n")
                        subClasses.forEach { classDecl ->
                            it.write("import ${classDecl.qualifiedName?.asString()}\n")
                        }
                        it.write("import androidx.navigation.NavGraphBuilder\n\n")
                        it.write("import androidx.compose.runtime.Composable\n\n")
                        it.write("import androidx.navigation.compose.composable\n\n")
                        it.write("import androidx.compose.runtime.remember\n\n")
                        it.write("fun NavGraphBuilder.generatedScreensNavigation() {\n")
                        subClasses.forEach { classDecl ->
                            it.write("    composable(route = \"${classDecl.simpleName.asString()}\") {\n")
                            it.write("         val screen = remember { ${classDecl.simpleName.asString()}() }\n")
                            it.write("         screen.apply{\n")
                            it.write("            this.Content()\n")
                            it.write("         }\n")
                            it.write("    }\n")
                            it.write("\n")
                        }
                        it.write("}\n")
                        it.flush()
                    }
                }
                fileStream.flush()
                fileStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isPrimitiveType(declaration: KSClassDeclaration): Boolean {
        return declaration.qualifiedName?.asString()?.let { isPrimitiveTypeName(it) } ?: false
    }


    private fun isPrimitiveTypeName(qualifiedName: String): Boolean {
        return when (qualifiedName) {
            "kotlin.String",
            "kotlin.Int",
            "kotlin.Long",
            "kotlin.Double",
            "kotlin.Float",
            "kotlin.Boolean" -> true

            else -> false
        }
    }


}




