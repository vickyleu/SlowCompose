@file:OptIn(DelicateCoroutinesApi::class)

package com.base

import com.diffplug.spotless.FormatExceptionPolicyStrict
import com.diffplug.spotless.Formatter
import com.diffplug.spotless.FormatterProperties
import com.diffplug.spotless.LineEnding
import com.diffplug.spotless.generic.EndWithNewlineStep
import com.diffplug.spotless.generic.IndentStep
import com.diffplug.spotless.generic.ReplaceStep
import com.diffplug.spotless.generic.TrimTrailingWhitespaceStep
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.gradle.api.Task
import org.gradle.api.internal.provider.DefaultProperty
import org.gradle.api.provider.Property
import java.io.File
import java.nio.charset.Charset
import kotlin.reflect.KFunction1
import kotlin.reflect.full.declaredMemberProperties

class CocoapodsAppender private constructor() {
    class Builder {
        private final val file: File
        private final val isExpanded: Boolean

        internal constructor(file: File, isExpanded: Boolean) {
            if (!file.exists()) {
                throw IllegalArgumentException("file not exist")
            }
            this.file = file
            this.isExpanded = isExpanded
            lines = file.readText().lines().toMutableList()
        }

        constructor(file: File) : this(file, false)

        private var lines: MutableList<String>


        fun build() {
            if (isExpanded) {
                throw RuntimeException("can't build in expanded mode")
            }
            val text = lines.joinToString("\n")
            file.writeText(text)
            formatFile(file)
        }

        fun expandedBuild() {
            if (isExpanded) {
                val text = lines.joinToString("\n")
                file.writeText(text)
                formatFile(file)
            }
        }


        private fun formatFile(file: File) {
            // 创建一个 Spotless formatter
            // FormatExceptionPolicy.failOnlyOnError()
            val exceptionPolicy = FormatExceptionPolicyStrict()
            val steps = listOf(
                IndentStep.Type.SPACE.create(),// 确保文件以换行符结尾
                EndWithNewlineStep.create(),// 确保文件以换行符结尾
                ReplaceStep.create("replace", "  ", " "),// 替换缩进
                TrimTrailingWhitespaceStep.create(),// 删除行尾空格
                ReplaceStep.create("replace", "\t", " "),// 替换缩进
                ReplaceStep.create("replace", "    ", " "),// 替换缩进
//                ReplaceStep.create("replace", "", " "),// 替换缩进
                TrimTrailingWhitespaceStep.create(),// 删除行尾空格
            )
            val f = Formatter.builder()
                .lineEndingsPolicy(LineEnding.UNIX.createPolicy())
                .encoding(Charset.defaultCharset())
                .rootDir(file.parentFile.toPath())
                .steps(steps)
                .exceptionPolicy(exceptionPolicy)
                .build()
            // 使用 Spotless 格式化文本
            f.applyTo(file)
        }

        @SuppressWarnings
        fun append(index: Int, appendText: String): Builder {
            lines.add(index, appendText)
            return this
        }

        @SuppressWarnings
        fun appendFirst(appendText: String): Builder {
            lines.add(0, appendText)
            return this
        }

        @SuppressWarnings
        fun appendLast(appendText: String): Builder {
            lines.add(appendText)
            return this
        }

        @SuppressWarnings
        fun append(searchBy: String, appendText: String): Builder {
            val index = lines.indexOfFirst { it.contains(searchBy) }
            lines.add(index + 1, appendText)
            lines = lines.joinToString("\n").lines().toMutableList()
            return this
        }

        @SuppressWarnings
        fun appendInWholePodspecFile(appendText: String): Builder {
            // 默认加在设置iOS部署目标的后面,不影响其他设置
            return append("spec.ios.deployment_target", appendText)
        }

        @SuppressWarnings
        fun appendInWholeSyntheticPodfile(appendText: String): Builder {
            // 默认加在设置iOS部署目标的后面,不影响其他设置
            return append("target.build_configurations.each do |config|", appendText)
        }

        @SuppressWarnings
        fun replace(searchBy: String, replaceText: String): Builder {
            val index = lines.indexOfFirst { it.contains(searchBy) }
            if (index >= 0) {
                lines[index] = replaceText
            }
            lines = lines.joinToString("\n").lines().toMutableList()
            return this
        }

        @SuppressWarnings
        fun replace(index: Int, replaceText: String): Builder {
            if (index >= 0) {
                lines[index] = replaceText
            }
            lines = lines.joinToString("\n").lines().toMutableList()
            return this
        }

        /**
         * 检查xcode有没有安装Kotlin插件的,虽然不是必须的,但是我还是改成强制要求了,不能调试的代码没有意义
         * @return String 返回的字符串为植入到podspec中的代码,已处理好缩进
         *
         * english: Check if xcode has installed the Kotlin plugin. Although it is not necessary,
         * I still changed it to a mandatory requirement.
         * There is no point in code that cannot be debugged
         * @return String The returned string is the code implanted in podspec, and the indentation has been processed
         */
        fun xcodeKotlinCheck(spec: Any): Builder {
            if (spec::class.simpleName != "PodspecTask_Decorated") {
                throw IllegalArgumentException("xcodeKotlinCheck must be called in PodspecTask")
            }
            return appendInWholePodspecFile(
                """
            # 检查是否安装了 CFPropertyList gem
            unless Gem::Specification::find_all_by_name('CFPropertyList').any?
              puts "Installing CFPropertyList gem..."
              `gem install CFPropertyList`
            end
            require 'cfpropertylist'
            # 获取Xcode配置文件路径
            xcode_plist_path = File.expand_path("~/Library/Preferences/com.apple.dt.Xcode.plist")
            # 以ASCII-8BIT编码读取配置文件内容
            xcode_settings_binary = IO.binread(xcode_plist_path)
            # 解析 plist 文件
            xcode_settings = CFPropertyList.native_types(CFPropertyList::List.new(data: xcode_settings_binary).value)
            # 获取DerivedData目录
            derived_data_directory = xcode_settings["IDECustomDerivedDataLocation"] || xcode_settings["DerivedDataLocation"] || ""
            # 如果derived_data_directory 没有获得值,说明使用了默认的路径
            if derived_data_directory.empty?
              derived_data_directory = File.expand_path("~/Library/Developer/Xcode/DerivedData")
            end
            
            
            # 查找Kotlin插件
            plug = File.expand_path(File.join(File.dirname(derived_data_directory), "/Plug-ins"))
            
            # 检查是否存在插件目录
            if plug.empty?
              raise "plug directory not found. Please check your Xcode settings. plug directory as #{plug}, founded by query DerivedData directory parent"
            end
            
            kotlin_plugin_found = Dir.glob("#{plug}/*.ideplugin").any? { |path| path.include?('Kotlin') }
            
            # 打印结果
            puts "Kotlin插件 #{kotlin_plugin_found ? '已安装.' : '未安装.'}"
            unless kotlin_plugin_found
              raise "XCode Kotlin Plugin is not installed. Please install it using
              ```
              brew install xcode-kotlin & \\
              xcode-kotlin install
              ```."
            end
                """.trimIndent()
                    .prependIndent("    ")
            )
        }

        fun deploymentTarget(
            podGen: Any,
            target: String? = null,
            swiftTarget: String? = null,
            excludePods: List<String> = emptyList()
        ): Builder {
            val deploymentTarget = if (target == null) {
                if (podGen::class.simpleName != "PodGenTask_Decorated") {
                    throw IllegalArgumentException("deploymentTarget must be called in PodGenTask or set target manually")
                }
                val platformSettingsVar: DefaultProperty<*>? =
                    podGen::class.declaredMemberProperties.find { it.name == "platformSettings" }?.getter?.call(
                        podGen
                    ) as? DefaultProperty<*>
                val platformSettings = platformSettingsVar?.get()
                    ?: throw IllegalArgumentException("PodGenTask platformSettings not found")

                platformSettings::class.declaredMemberProperties.find { it.name == "deploymentTarget" }
                    ?.let { return@let it.getter.call(platformSettings) }
                    ?: throw IllegalArgumentException("PodGenTask deploymentTarget not found")
            } else {
                target
            }
//            config.build_settings['ONLY_ACTIVE_ARCH'] = 'YES'
            // 只处理一次,避免重复修改文件, 需要更新就删除build目录下的cocoapods目录
            if (isExist("config.base_configuration_reference.real_path")) {
                return this
            }
            val indent = "\t\t"
            // config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
            val content = """
               
                xcconfig_path = config.base_configuration_reference.real_path
                xcconfig = File.read(xcconfig_path)
                xcconfig_mod = xcconfig.gsub(/DT_TOOLCHAIN_DIR/, "TOOLCHAIN_DIR")
                File.open(xcconfig_path, "w") { |file| file << xcconfig_mod }
                """.trimIndent()
                .replaceIndent(indent)
            return appendInWholeSyntheticPodfile(content)
                .appendOrCreate(
                    "if config.base_configuration_reference",
                    """
                 # @see https://stackoverflow.com/a/37289688/456536
                 config.build_settings.delete 'IPHONEOS_DEPLOYMENT_TARGET'
                 config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
                 config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = "$deploymentTarget"
                 ${
                        """
                        if config.name == 'Debug'
                           # Debug 环境 
                           config.build_settings.delete 'ONLY_ACTIVE_ARCH'
                           config.build_settings['ONLY_ACTIVE_ARCH'] = 'YES'
                        elsif config.name == 'Release'
                           # Release 环境
                           config.build_settings.delete 'ONLY_ACTIVE_ARCH'
                           config.build_settings['ONLY_ACTIVE_ARCH'] = 'NO'
                        end 
                        config.build_settings.delete 'VALID_ARCHS'
                        config.build_settings.delete "EXCLUDED_ARCHS[sdk=iphonesimulator*]"
                        # Arm64 的Mac 有很多框架没有适配,所以排除掉,只使用Rosetta模拟器运行X64
                        config.build_settings["EXCLUDED_ARCHS[sdk=iphonesimulator*]"] = "arm64"
                     """.trimIndent()
                    }
                 ${if (swiftTarget.isNullOrEmpty()) "" else "config.build_settings['SWIFT_VERSION'] = \"$swiftTarget\""}
                """.trimIndent().prependIndent("\t"),
                    indent = indent,
                    begin = "if config.base_configuration_reference",
                    end = "end",
                    func = ::appendInWholeSyntheticPodfile
                )

        }

        fun relinkGradle(projectDir: File, podSpecDir: File): Builder {
            val originDir =
                File("${projectDir.parentFile.absolutePath}/iosApp/Pods/../../${projectDir.name}/")
            val relativeTo = originDir.relativeTo(podSpecDir).path
            return replace(
                "REPO_ROOT=\"${"$"}PODS_TARGET_SRCROOT\"",
                """
                    REPO_ROOT="${"$"}PODS_TARGET_SRCROOT${if (relativeTo.isEmpty()) "" else "/$relativeTo"}"
                            echo "REPO_ROOT: ${'$'}REPO_ROOT"
                            echo "${
                    "Command: ${'$'}REPO_ROOT/../gradlew -p ${'$'}REPO_ROOT ${'$'}KOTLIN_PROJECT_PATH:syncFramework " +
                            "                    -Pkotlin.native.cocoapods.platform=${'$'}PLATFORM_NAME " +
                            "                    -Pkotlin.native.cocoapods.archs=\\\"${'$'}ARCHS\\\"" +
                            "                    -Pkotlin.native.cocoapods.configuration=\\\"${'$'}CONFIGURATION\\\""
                }"
                """.trimIndent().prependIndent("\t\t\t\t")
            )
        }

        fun sharedPodRelink(
            podSpecDir: File,
            rollback: Boolean = false,
            sharedModuleName: String = "shared"
        ): Builder {
            var searchBy = "  pod '$sharedModuleName', :path => '../$sharedModuleName'"
            val relative = "${podSpecDir.relativeTo(this.file.parentFile).path}/".replace("//", "/")
            var replaceBy = "  pod '$sharedModuleName', :path => '${relative}'"
            if (rollback) {
                val old = searchBy
                searchBy = replaceBy
                replaceBy = old
            }
            val index = lines.indexOfFirst { it.contains(searchBy) }

            if (index >= 0) {
                return replace(searchBy, replaceBy)
            } else {
                val index2 = lines.indexOfFirst { it.contains("pod '$sharedModuleName'") }
                if (index2 >= 0) {
                    return replace(index2, replaceBy)
                }
            }
            return this
        }

        /**
         * 自定义pod的构建目录,默认是在根目录build/ios下,如果修改了构建目录,则需要修改podspec文件中的SYMROOT
         *
         */
        @Suppress("unused")
        fun rewriteSymroot(buildDir: File, projectDir: File, rollback: Boolean = false): Builder {
            val shouldChangePodspecDir =
                buildDir.parentFile.absolutePath != projectDir.absolutePath
            val isBuildDirChanged = shouldChangePodspecDir

            if (isBuildDirChanged) {
                val relative =
                    buildDir.relativeTo(projectDir.parentFile.resolve("iosApp/Pods/")).path
                val path = "'${'$'}(SRCROOT)/$relative/cocoapods/iosApp'"
                val func = {
                    if (!isExist("config.build_settings['SYMROOT'] = $path")) {
                        append(
                            "platform ", """
                            ENV['PODS_BUILD_DIR'] = $path
                            ENV['SYMROOT'] = $path
                """.trimIndent().prependIndent("\t")
                        )
                            .appendOrCreate(
                                "if config.base_configuration_reference",
                                """
                  	        config.build_settings['PODS_BUILD_DIR'] = $path
                  	        config.build_settings['SYMROOT'] = $path""".trimIndent()
                                    .prependIndent("\t\t"),
                                indent = "\t\t",
                                begin = "if config.base_configuration_reference",
                                end = "end",
                                func = ::appendInWholeSyntheticPodfile
                            )
                    }
                }
                if (!isExist("ENV['PODS_BUILD_DIR']")) {
                    func()
                } else {
                    remove("ENV['PODS_BUILD_DIR']")
                    remove("ENV['SYMROOT']")
                    remove("config.build_settings['SYMROOT']")
                    remove("config.build_settings['PODS_BUILD_DIR']")
                    func()
                }
            } else {
                if (rollback) {
                    remove("ENV['PODS_BUILD_DIR']")
                    remove("ENV['SYMROOT']")
                    remove("config.build_settings['SYMROOT']")
                    remove("config.build_settings['PODS_BUILD_DIR']")
                }
            }
            return this
        }

        @SuppressWarnings
        fun remove(searchBy: String) {
            val index = lines.indexOfFirst {
                it.contains(searchBy)
            }
            if (index >= 0) {
                lines.removeAt(index)
                lines = lines.joinToString("\n").lines().toMutableList()
            }
        }

        @SuppressWarnings
        fun isExist(searchBy: String): Boolean {
            val index = lines.indexOfFirst {
                it.contains(searchBy)
            }
            return index >= 0
        }

        @SuppressWarnings
        fun appendOrCreate(
            searchBy: String,
            appendText: String,
            indent: String,
            begin: String? = null,
            end: String? = null,
            func: KFunction1<String, Builder>
        ): Builder {
            val index = lines.indexOfFirst {
                it.contains(searchBy)
            }
            if (index >= 0) {
                lines.add(index + 1, appendText.prependIndent(indent))
                lines = lines.joinToString("\n").lines().toMutableList()
            } else {
                val newLines = mutableListOf<String>()
                if (begin != null) {
                    newLines.add(begin)
                }
                newLines.add(appendText)
                if (end != null) {
                    newLines.add(end)
                }
                func.invoke(
                    newLines.joinToString("\n")
                        .prependIndent(indent)
                )
            }
            return this
        }

        @SuppressWarnings
        fun replaceOrCreate(
            searchBy: String,
            appendText: String,
            indent: String,
            begin: String? = null,
            end: String? = null,
            func: KFunction1<String, Builder>
        ): Builder {
            val index = lines.indexOfFirst {
                it.contains(searchBy)
            }
            if (index >= 0) {
                lines[index] = appendText.prependIndent(indent)
                lines = lines.joinToString("\n").lines().toMutableList()
            } else {
                val newLines = mutableListOf<String>()
                if (begin != null) {
                    newLines.add(begin)
                }
                newLines.add(appendText)
                if (end != null) {
                    newLines.add(end)
                }
                func.invoke(
                    newLines.joinToString("\n")
                        .prependIndent(indent)
                )
            }
            return this
        }

        fun openStaticLinkage(): Builder {
            return replaceOrCreate(
                "use_frameworks!",
                """
                 use_frameworks! #:linkage => :static
                """.trimIndent().prependIndent("\t"), indent = "\t\t",
                func = ::appendInWholeSyntheticPodfile
            )
        }

        override fun toString(): String {
            return lines.joinToString("\n")
        }

        fun inhibitAllWarnings(): Builder {
            if (!isExist("use_frameworks!")) {
                return appendOrCreate(
                    "use_frameworks!",
                    """inhibit_all_warnings!""", "",
                    func = ::appendInWholeSyntheticPodfile
                )
            }
            return this
        }

        fun excludeNonIphoneOS(excludeNonIphoneOS:Boolean): Builder {
            if(excludeNonIphoneOS.not())return this
            if (!isExist("config.build_settings['SUPPORTED_PLATFORMS']")) {
                return appendOrCreate(
                    "target.build_configurations.each",
                    """# 强制确保仅 iOS
                        config.build_settings['SUPPORTED_PLATFORMS'] = 'iphonesimulator iphoneos'""".trimMargin(), "",
                    func = ::appendInWholeSyntheticPodfile
                )
            }
            return this
        }

    }

    class TaskBuilder(
        private val task: Task,
        private val buildDir: File,
        private val projectDir: File,
        private val rootProjectDir: File,
    ) {
        private lateinit var podBuilder: Builder
        private lateinit var closure: (Builder) -> Unit
        private lateinit var buildExec: () -> Unit
        val isBuildDirChanged: Boolean
            get() {
                val shouldChangePodspecDir =
                    buildDir.parentFile.absolutePath != projectDir.absolutePath
                return shouldChangePodspecDir
            }

        val podSpecDir: File
            get() {
                return if (isBuildDirChanged) {
                    calculatePodspecDirectory(projectDir, buildDir, rootProjectDir)
                } else {
                    projectDir
                }
            }

        @Suppress("unused", "UNUSED_VARIABLE")
        fun relinkPodspec(outputFile: File): TaskBuilder {
            @Suppress("UNUSED", "UNUSED_VARIABLE", "UNCHECKED_CAST")
            val outputDir: Property<File>? =
                task::class.declaredMemberProperties.find { it.name == "outputDir" }?.getter?.call(
                    task
                ) as? Property<File>
            if (isBuildDirChanged) {
                /*var ignoreDistributionDir = false
                var oldOutputDir: File? = null
                if (outputDir != null) {
                    val old = outputDir.get()
                    if (
                        !(old.absolutePath.endsWith("/cocoapods/publish/release")
                                &&
                                old.absolutePath.endsWith("/cocoapods/publish/debug"))
                    ) {
                        oldOutputDir = old
                        outputDir.set(podSpecDir)
                    } else {
                        ignoreDistributionDir = true
                    }
                }
                if (!ignoreDistributionDir && oldOutputDir != null) {
                    // 查找old目录中的podspec文件
                    val podspecFile =
                        oldOutputDir.listFiles()?.find { it.name.endsWith(".podspec") }
                    podspecFile?.delete()
                }*/
//                val outputFileCaller = task::class.declaredMemberProperties.find { it.name.split("&").firstOrNull() == "outputFile" }!!
//                outputFileCaller.isAccessible = true
//                val outputFile = (outputFileCaller.getter.call(task) as File)
                outputFile.apply {
                    podBuilder = Builder(this, true)
                    if (isBuildDirChanged) {
                        podBuilder.appendFirst("# podspec can't be link relative for parent directory, so we change podspec file to parent directory 😂 ")
                    }
                    if (::closure.isInitialized) {
                        closure(podBuilder)
                    }
                    GlobalScope.launch {
                        withTimeout(5000) {
                            withContext(Dispatchers.IO) {
                                while (!::buildExec.isInitialized) {
                                    Thread.sleep(100)
                                }

                                withContext(Dispatchers.Default) {
                                    if (::buildExec.isInitialized) {
                                        buildExec()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
//                if (outputDir != null) {
//                    val old = outputDir.get().parentFile
//                    // 查找old目录中的podspec文件
//                    val podspecFile = old.listFiles()?.find { it.name.endsWith(".podspec") }
//                    podspecFile?.delete()
//                    val current =
//                        old.resolve("${this@TaskBuilder.projectDir.name}/${this@TaskBuilder.projectDir.name}.podspec")
//
//                }
                podBuilder = Builder(outputFile, true)
                if (::closure.isInitialized) {
                    closure(podBuilder)
                }
                GlobalScope.launch {
                    withTimeout(5000) {
                        withContext(Dispatchers.IO) {
                            while (!::buildExec.isInitialized) {
                                Thread.sleep(100)
                            }
                            withContext(Dispatchers.Default) {
                                if (::buildExec.isInitialized) {
                                    buildExec()
                                }
                            }
                        }
                    }
                }

            }
            return this
        }

        fun withClosure(closure: (Builder) -> Unit): TaskBuilder {
            this.closure = closure
            return this
        }

        fun build() {
            this.buildExec = {
                podBuilder.expandedBuild()
            }
        }
    }
}

fun calculatePodspecDirectory(
    projectDirFile: File,
    buildDirFile: File,
    rootProjectDir: File
): File {
    val absoluteProjectDir = projectDirFile.absoluteFile
    val absoluteBuildDir = buildDirFile.absoluteFile
    if (!absoluteBuildDir.absolutePath.startsWith(rootProjectDir.absolutePath)) {
        return projectDirFile
    }
    val projectComponents =
        absoluteProjectDir.toPath().toAbsolutePath().iterator().asSequence().toList()
    val buildComponents =
        absoluteBuildDir.toPath().toAbsolutePath().iterator().asSequence().toList()
    val commonComponents =
        projectComponents.zip(buildComponents).takeWhile { (p, b) -> p == b }.map { it.first }
    var str = commonComponents.joinToString(File.separator)
    if (!str.startsWith(File.separator)) {
        str = "${File.separator}$str"
    }
    val commonParentDir = File(str)
    return commonParentDir
}