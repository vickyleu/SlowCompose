import app.cash.sqldelight.core.capitalize
import com.base.findTopLevelParentDirectories
import com.base.getGitCommitHash
import com.base.getXcodeSelectPath
import com.base.syntheticPodfileGen
import com.base.syntheticXCodeprojsTarget
import com.base.updatePodspecFile
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.google.devtools.ksp.gradle.KspTaskMetadata
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.types.error.ErrorModuleDescriptor.platform
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodBuildTask
import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.PodspecTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

val xcodePath = "/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/"

plugins {
    // kotlin多平台插件
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    // android应用插件
    id(libs.plugins.android.application.get().pluginId)
    // jetbrains compose插件
    id(libs.plugins.jetbrains.compose.get().pluginId)
    // kotlin 原子操作插件
    alias(libs.plugins.kotlinx.atomicfu)
    // kotlin parcelize插件
    id(libs.plugins.kotlin.parcelize.get().pluginId)

    // google ksp插件
    alias(libs.plugins.ksp)
    // sqldelight插件
    alias(libs.plugins.sqldelight)
    // kotlin serialization插件
    alias(libs.plugins.kotlin.serialization)
    // kotlin cocoapods插件
    id(libs.plugins.kotlin.cocoapods.get().pluginId)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.compose.compiler)
}

val configProperties = Properties().apply {
    load(project.file("../config.properties").reader())
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.value(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        }
    }
    listOf(
//        iosX64(),
        iosArm64(),
//        iosSimulatorArm64() //Bugly 不支持arm模拟器
    ).forEach {
        @Suppress("unused")
        val platform = when (it.name) {
            "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
            "iosArm64" -> "iphoneos"
            else -> error("Unsupported target ${it.name}")
        }
        it.binaries {
            framework {
                this.isStatic = false
                val buildType = when (this.buildType) {
                    NativeBuildType.RELEASE -> {
                        "Release"
                    }

                    NativeBuildType.DEBUG -> {
                        //TODO 🤡🤡🤡 when platform is simulator, kmm will generate a release framework, fucking idiot 🤡🤡🤡
                        if (platform == "iphonesimulator") "Release" else "Debug"
                    }

                    else -> "Debug"
                }
                this.baseName = "ComposeApp"
                // 不要使用transitiveExport = true。使用 transitive export 在许多情况下会禁用死代码消除：
                // 编译器必须处理大量未使用的代码。它会增加编译时间。export明确用于导出所需的项目和依赖项。
                @OptIn(ExperimentalKotlinGradlePluginApi::class)
                this.transitiveExport = false
                this.freeCompilerArgs += "-Xbinary=bundleId=${configProperties.getProperty("BundleId")}.dyn"
                this.linkerOpts += "-Objc"
                if (System.getenv("XCODE_VERSION_MAJOR") == "1500") {
                    // workaround for xcode 15 and kotlin < 1.9.10:
                    // https://youtrack.jetbrains.com/issue/KT-60230/Native-unknown-options-iossimulatorversionmin-sdkversion-with-Xcode-15-beta-3
//                    this.linkerOpts += "-ld_classic"
                }
                linkerOpts += listOf(
                    "-U", // 忽略未定义的符号
                    "-dead_strip", // 删除未使用的符号
                    "-force_load", "composeApp.framework/composeApp", // 强制加载 composeApp 框架中的符号
                )

                freeCompilerArgs += "-Xg0" // 移除调试信息
                this.linkerOpts += listOf(
                    "-framework", "SystemConfiguration",
                    "-framework", "CoreTelephony",
                    "-framework", "CoreBluetooth",
                    "-framework", "QuartzCore",
                    "-framework", "SpriteKit",
                    "-framework", "CoreGraphics",
                    "-framework", "CoreFoundation",
                    "-framework", "Security",
                    "-framework", "UniformTypeIdentifiers",
                    "-framework", "MobileCoreServices",
                    "-framework", "MetalKit",
                    "-framework", "CoreText",
                    "-framework", "UIKit",
                    "-framework", "Foundation",
                )
                val searchFrameworks = getLinkFrameworkDirs(platform, buildType)
                // 可选优化，排查符号冲突
                searchFrameworks.forEach { (static, filePair) ->
                    val (file, name) = filePair
                    if (static) {
                        linkerOpts += listOf("-L${file.absolutePath}")
                    } else {
                        println("==连接: file://${file.absolutePath}")
                        linkerOpts += listOf("-F${file.absolutePath}")
                    }
                }
                val podRoot = layout.buildDirectory.get()
                    .asFile.resolve("cocoapods/synthetic/ios/")
                val downloadRoot = podRoot.resolve("Pods/")

                this.linkerOpts += listOf("-lZXYBSDK")
                this.linkerOpts += listOf("-framework", "TencentMeetingSDK")
//                this.linkerOpts += listOf("-framework","PLVStarscream")
//                this.linkerOpts += listOf("-framework","PLVSocketIOClientSwift")

                this.linkerOpts += listOf("-ld_classic")
                this.linkerOpts += "-L/usr/lib/swift"

                this.freeCompilerArgs += "-Xverbose-phases=Linker"
                this.freeCompilerArgs +=
                    "-Xoverride-konan-properties=osVersionMin.ios_simulator_arm64=${libs.versions.iosDeploymentTarget.get()};" +
                            "osVersionMin.ios_x64=${libs.versions.iosDeploymentTarget.get()};" +
                            "osVersionMin.ios_arm64=${libs.versions.iosDeploymentTarget.get()}"

                // The notification-service-extension is limited to 24 MB of memory.
                // With mimalloc we can easily hit the 24 MB limit, and the OS kills the process.
                // But with standard allocation, we're using less then half the limit.
                // this.freeCompilerArgs += "-Xallocator=std"
//                this.freeCompilerArgs += listOf("-linker-options", "-application_extension")
                this.freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"

                println("==连接linkerOpts: ${linkerOpts.joinToString(" ")}")
            }
        }
    }

    cocoapods {
        summary = "ComposeApp"
        homepage = "."
        version = "1.0.0"
        license = "MIT"
        ios.deploymentTarget = libs.versions.iosDeploymentTarget.get()
        //source = "https://cdn.cocoapods.org"
        source = "https://github.com/CocoaPods/Specs.git"
        specRepos {
            //url("https://github.com/CocoaPods/Specs.git")
            url("https://git2.baijiashilian.com/open-ios/specs.git")
        }
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "ComposeApp"
            isStatic = false
//            optimized = true
//            debuggable = false
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            transitiveExport = false
            @Suppress("unused")
            val platform = when (this.target.name) {
                "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
                "iosArm64" -> "iphoneos"
                else -> error("Unsupported target ${this.target.name}")
            }
            val buildType = when (this.buildType) {
                NativeBuildType.RELEASE -> {
                    "Release"
                }

                NativeBuildType.DEBUG -> {
                    if (platform == "iphonesimulator") "Release" else "Debug"
                }

                else -> "Debug"
            }
            linkerOpts += listOf(
                "-U", // 忽略未定义的符号
                "-dead_strip", // 删除未使用的符号
            )

            freeCompilerArgs += "-Xg0" // 移除调试信息
            linkerOpts += "-Objc"
            val searchFrameworks = getLinkFrameworkDirs(platform, buildType)
            linkerOpts += listOf(
                "-framework", "SystemConfiguration",
                "-framework", "CoreTelephony",
                "-framework", "CoreBluetooth",
                "-framework", "QuartzCore",
                "-framework", "SpriteKit",
                "-framework", "CoreGraphics",
                "-framework", "CoreFoundation",
                "-framework", "Security",
                "-framework", "UniformTypeIdentifiers",
                "-framework", "MobileCoreServices",
                "-framework", "MediaPlayer",
                "-framework", "MetalKit",
                "-framework", "CoreText",
                "-framework", "UIKit",
                "-framework", "Foundation",
            )
            linkerOpts += listOf(
                "-framework", "polyv"
            )
            // 可选优化，排查符号冲突
            searchFrameworks.forEach { (static, filePair) ->
                val (file, name) = filePair
                if (static) {
                    linkerOpts += listOf("-L${file.absolutePath}")
                } else {
                    println("连接:   file://${file.absolutePath}")
                    linkerOpts += listOf("-F${file.absolutePath}")
                }
            }
            val podRoot = layout.buildDirectory.get()
                .asFile.resolve("cocoapods/synthetic/ios/")
            val downloadRoot = podRoot.resolve("Pods/")
            // 由于cocoapods插件只能使用源码, 不支持vendored_frameworks 和 vendored_libraries,所以需要通过-F 和 -l来指定framework和.a的路径
            val xcFrameworkTag = when (platform) {
                "iphonesimulator" -> "ios-arm64_x86_64-simulator"
                "iphoneos" -> "ios-arm64"
                else -> ""
            }

            this.linkerOpts += listOf("-lZXYBSDK")
            this.linkerOpts += listOf("-framework", "TencentMeetingSDK")
            this.linkerOpts += listOf("-ld_classic")
//            linkerOpts += listOf("-framework","PLVStarscream")
//            linkerOpts += listOf("-framework","PLVSocketIOClientSwift")

            if (System.getenv("XCODE_VERSION_MAJOR") == "1500") {
                // workaround for xcode 15 and kotlin < 1.9.10:
                // https://youtrack.jetbrains.com/issue/KT-60230/Native-unknown-options-iossimulatorversionmin-sdkversion-with-Xcode-15-beta-3
//                this.linkerOpts += "-ld_classic"
            }
            this.linkerOpts += "-L/usr/lib/swift"

            freeCompilerArgs += listOf(
                "-Xoverride-konan-properties=osVersionMin.ios_simulator_arm64=${libs.versions.iosDeploymentTarget.get()};" +
                        "osVersionMin.ios_x64=${libs.versions.iosDeploymentTarget.get()};" +
                        "osVersionMin.ios_arm64=${libs.versions.iosDeploymentTarget.get()}"
            )
            ///freeCompilerArgs += "-Xverbose-phases=Linker"
            this.binaryOption("bundleId", "${configProperties.getProperty("BundleId")}.pod")
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
        }

        pod("DTCoreText") {
            this.source = git("https://github.com/vickyleu/DTCoreText.git") {
                branch = "develop"
            }
            packageName = "uooc.DTCoreText"
//            linkOnly = true
        }
        pod("DTFoundation")
        pod("iosMath") {
            version = "~> 0.9"
            extraOpts += listOf("-compiler-option", "-fmodules")
            moduleName = "iosMath"
            linkOnly = true
        }
        pod("QCloudCOSXML/Transfer")
        pod("AFNetworking")
        pod("UMDevice") {
            version = "3.4.0"
            moduleName = "UMDevice"
            linkOnly = true
        }
        //必须集成
        pod("UMCommon")//由原来的UMCCommon变为UMCommon
        {
            version = "7.5.0"
        }

        pod("SDWebImage") {
            linkOnly = true
        }

        pod("PLVBytedEffectSDK") {
            linkOnly = true
        }

        pod("PLVLOpenSSL") {
            linkOnly = true
        }

        pod("BaijiaYun/_BJLiveUIBigClass") {
            version = "4.18.0"
            moduleName = "BJLiveUIBigClass"
            packageName = "what.the.fuck.with.baijiayun.live.big"
            linkOnly = true
        }
        pod("BaijiaYun/_BJLiveUISmallClass") {
            version = "4.18.0"
            moduleName = "BJLiveUISmallClass"
            packageName = "what.the.fuck.with.baijiayun.live.small"
            linkOnly = true
        }
        pod("BaijiaYun/_BJLiveUIBase") {
            version = "4.18.0"
            moduleName = "BJLiveUIBase"
            packageName = "what.the.fuck.with.baijiayun.live.uibase"
            extraOpts += listOf("-compiler-option", "-fmodules")
            linkOnly = true
        }
        pod("BaijiaYun/_BJLiveUIEE") {
            version = "4.18.0"
            moduleName = "BJLiveUIEE"
            // BJLiveUIEE-Swift.h:307:67: error: cannot find protocol declaration for 'BJLInnerRoomVCProtocol'
            packageName = "what.the.fuck.with.baijiayun.live.uiee"
            //useInteropBindingFrom("BJLiveUIBase")
            extraOpts += listOf("-compiler-option", "-fmodules")
            linkOnly = true
        }
        pod("BJLiveBase") {
            version = "4.18.0"
            moduleName = "BJLiveBase"
            packageName = "what.the.fuck.with.baijiayun.live.base"
            linkOnly = true
        }

        pod("BaijiaYun/_BJLSellUI") {
            version = "4.18.0"
            moduleName = "BJLSellUI"
            extraOpts += listOf("-compiler-option", "-fmodules")
            packageName = "what.the.fuck.with.baijiayun.live.shell"
            // extraOpts += listOf(
            //     "-Xignore", "BJLSellUI-Swift.h"
            //  )
            linkOnly = true
        }
        pod("BaijiaYun/_ZXYB") {
            version = "4.18.0"
            moduleName = "ZXYBSDK"
            linkOnly = true
        }
        pod("VIMediaCache") {
            source = git("https://github.com/vickyleu/VIMediaCache.git") {
                branch = "master"
            }
            moduleName = "VIMediaCache"
            linkOnly = true
        }
        pod("BaijiaYun/BJYRTCEngine") {
            version = "4.18.0"
            moduleName = "BJYRTCEngine"
            packageName = "what.the.fuck.with.baijiayun.live.engine"
            //useInteropBindingFrom("BJYIJKMediaFramework")
            //useInteropBindingFrom("BRTC")
            linkOnly = true
        }
        pod("BJYIJK") {
            version = "4.3.1"
            moduleName = "BJYIJKMediaFramework"
            linkOnly = true
        }

        pod("BaijiaYun/_BJLiveCore") {
            version = "4.18.0"
            moduleName = "BJLiveCore"
            //useInteropBindingFrom("ZXYBSDK")
            //useInteropBindingFrom("BJLLog")
            packageName = "what.the.fuck.with.baijiayun.live.core"
            linkOnly = true
        }
        pod("BJLLog") {
            version = "3.7.4.6"
            moduleName = "BJLLog"
            linkOnly = true
        }

        pod("TXLiteAVSDK_TRTC/TRTC") {
            version = "12.0.16292"
            linkOnly = true
        }

        pod("BaijiaYun/_BJPlaybackUI") {
            version = "4.18.0"
            moduleName = "BJPlaybackUI"
            packageName = "what.the.fuck.with.baijiayun.playback"
            linkOnly = true
        }

        pod("BaijiaYun/BJVideoPlayerCore") {
            version = "4.18.0"
            moduleName = "BJVideoPlayerCore"
            packageName = "what.the.fuck.with.baijiayun.playback"
            linkOnly = true
        }
        pod("BaijiaYun/BJPlayerUIBase") {
            version = "4.18.0"
            moduleName = "BJPlayerUIBase"
            packageName = "what.the.fuck.with.baijiayun.playback"
            linkOnly = true
        }
        pod("BRTC") {
            version = "3.0.14"
            moduleName = "BRTC"
            linkOnly = true
        }
        pod("Bugly") {
            moduleName = "Bugly"
            packageName = "what.the.fuck.Bugly"
        }
        pod("FinApplet") {
            moduleName = "FinApplet"
            packageName = "what.the.fuck.finclip"
        }
        pod("SSZipArchive") {
            moduleName = "SSZipArchive"
            version = "2.1.5"
            linkOnly=true
        }
        pod("MJRefresh") {
            moduleName = "MJRefresh"
            linkOnly=true
        }

        pod("polyv") {
            source = git("https://github.com/vickyleu/polyvmultiplatform.git") {
                branch = "main"
            }
            moduleName = "polyv"
//            useInteropBindingFrom("PLVCLogan")
//            useInteropBindingFrom("PLVFoundationConsoleLoggerSDK")
//            useInteropBindingFrom("PLVFoundationSafeModelSDK")
//            useInteropBindingFrom("PLVFoundationSDK")
            linkOnly = true
        }


        /*pod("PLVLiveScenesSDK") {
            version = "1.19.1"
            moduleName = "PLVLiveScenesSDK"
            linkOnly = true
        }*/


        pod("PLVVolcEngineRTCExt") {
            version = "3.41.304"
            moduleName = "PLVVolcEngineRTCExt"
            linkOnly = true
        }
        pod("PLVLiveScenesSDK/BaseSDK") {
            version = "1.19.1"
            moduleName = "PLVLiveScenesSDK"
            linkOnly = true
        }
        pod("PLVLiveScenesSDK/Core") {
            version = "1.19.1"
            moduleName = "PLVLiveScenesSDK"
            linkOnly = true
        }
        pod("PLVLiveScenesSDK/Player") {
            version = "1.19.1"
            moduleName = "PLVLiveScenesSDK"
            linkOnly = true
        }
        pod("PLVLiveScenesSDK/PrivacyInfo") {
            version = "1.19.1"
            moduleName = "PLVLiveScenesSDK"
            linkOnly = true
        }
        pod("PLVAliHttpDNS") {
//            version = "1.19.1"
            moduleName = "PLVAliHttpDNS"
            linkOnly = true
        }
        pod("AliyunOSSiOS") {
            version = "2.10.19"
            linkOnly = true
        }
        /*- PLVLiveScenesSDK/BaseSDK (= 1.19.1)
        - PLVLiveScenesSDK/Core (= 1.19.1)
        - PLVLiveScenesSDK/OtherPart (= 1.19.1)
        - PLVLiveScenesSDK/Player (= 1.19.1)
        - PLVLiveScenesSDK/PrivacyInfo (= 1.19.1)*/


        pod("PLVFoundationSDK/Core") {
            version = "1.19.0"
            moduleName = "PLVFoundationSDK"
//            useInteropBindingFrom("UMDevice")
            linkOnly = true
        }



        pod("PLVFDB") {
            version = "1.0.5"
            moduleName = "PLVFDB"
            linkOnly = true
        }

        pod("PLVFoundationSDK/AbstractBase") {
            version = "1.19.0"
            linkOnly = true
        }
        pod("PLVFoundationSDK/ConsoleLogger") {
            version = "1.19.0"
            moduleName = "PLVFoundationConsoleLoggerSDK"
            linkOnly = true
        }
        pod("PLVFoundationSDK/SafeModel") {
            version = "1.19.0"
            moduleName = "PLVFoundationSafeModelSDK"
            linkOnly = true
        }
        pod("PLVBusinessSDK/Core") {
            version = "1.19.1"
            moduleName = "PLVBusinessSDK"
            //useInteropBindingFrom("PLVSocketIOClientSwift")
            linkOnly = true
        }
        pod("PLVSocketIOClientSwift") {
            version = "0.2.1"
            moduleName = "PLVSocketIOClientSwift"
            //useInteropBindingFrom("PLVStarscream")
            linkOnly = true
        }
        pod("PLVStarscream") {
            version = "0.2.0"
            moduleName = "PLVStarscream"
            linkOnly = true
        }

        pod("PLVCLogan") {
            version = "1.0.0"
            moduleName = "PLVCLogan"
            linkOnly = true
        }
        pod("PLVAliHttpDNS/UTDID") {
            version = "1.10.0"
            moduleName = "UTDID"
            linkOnly = true
        }
//        pod("PLVLiveScenesSDK") {
//            version = "1.19.1"
//            moduleName = "PLVLiveScenesSDK"
//            //useInteropBindingFrom("PLVFoundationConsoleLoggerSDK")
//            //useInteropBindingFrom("PLVFoundationSafeModelSDK")
//            //useInteropBindingFrom("PLVFDB")
//            //useInteropBindingFrom("UTDID")
//            //useInteropBindingFrom("PLVFoundationSDK")
//            //useInteropBindingFrom("PLVBusinessSDK")
//            linkOnly = true
//        }

        pod("PLVImagePickerController") {
            version="0.1.3"
            moduleName = "PLVImagePickerController"
            linkOnly = true
        }
        pod("SVGAPlayer") {
            version="~> 2.3"
            moduleName = "SVGAPlayer"
            //extraOpts += listOf("-compiler-option", "-DGPB_USE_PROTOBUF_FRAMEWORK_IMPORTS=1")
            linkOnly = true
        }
        pod("Protobuf") {
            version="3.22.4"
            moduleName = "Protobuf"
//            extraOpts += listOf("-compiler-option", "-fmodules")
            linkOnly = true
        }
        extraSpecAttributes["frameworks"] =
            "['SystemConfiguration', 'CoreTelephony',  'QuartzCore',  'MediaPlayer',  'SpriteKit', 'CoreGraphics',  'CoreFoundation', 'Security', 'UniformTypeIdentifiers', 'MobileCoreServices', 'MetalKit', 'CoreText', 'UIKit']" //导入系统库
        extraSpecAttributes["libraries"] = "['sqlite3']" //导入系统库 //'c++', ,'z'

        extraSpecAttributes["vendored_frameworks"] = "['framework/${project.name}.framework']"
        extraSpecAttributes["static_framework"] = "false"
//        extraSpecAttributes["resource_bundles"] = """
//            {
//             'iosMath' => ['$(SYNTHETIC_BUILD_DIR)/iosMath/mathFonts.bundle'],
//            }
//        """.trimIndent()
        /*extraSpecAttributes["pod_target_xcconfig"] = """
            {
             'OTHER_LDFLAGS' => '-U -dead_strip -force_load ${'$'}(PROJECT_DIR)/composeApp.framework/composeApp'
            }
        """.trimIndent()*/
    }

    targets.withType<KotlinNativeTarget> {
        @Suppress("unused")
        val platform = when (this.targetName) {
            "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
            "iosArm64" -> "iphoneos"
            else -> error("Unsupported target ${this.targetName}")
        }
        compilations.getByName("main") {
            val thirdpartyPath = projectDir.resolve("src/nativeInterop/thirdparty")
            val tencentMeetingPath = projectDir.resolve("../iosApp/iosApp/TencentMeetingSDK")
            val podRoot = layout.buildDirectory.get()
                .asFile.resolve("cocoapods/synthetic/ios/")
            val downloadRoot = podRoot.resolve("Pods/")


            val systemFramework = listOf(
                "-framework", "SystemConfiguration",
                "-framework", "CoreTelephony",
                "-framework", "QuartzCore",
                "-framework", "SpriteKit",
                "-framework", "MediaPlayer",
                "-framework", "CoreGraphics",
                "-framework", "CoreFoundation",
                "-framework", "Security",
                "-framework", "UniformTypeIdentifiers",
                "-framework", "MobileCoreServices",
                "-framework", "MetalKit",
                "-framework", "CoreText",
                "-framework", "UIKit",
                "-framework", "Foundation",
            )

            cinterops.create("observer") {
                definitionFile = projectDir.resolve("src/nativeInterop/cinterop/observer.def")
            }
            // TODO 可千万要小心, 因为腾讯会议不能有任何引用, 只是在这里通过cinterop生成bindings,和源代码实际上是没有一毛钱关系的,所以xcode里面还要弄个framework search path
            // TODO 然后这个framework search path 目前会乱报错, 导致找不到真实的错误,一定一定要注意这里
            cinterops.create("TencentMeeting") {
                definitionFile = thirdpartyPath.resolve("TencentMeeting.def")
                packageName = "what.the.fuck.with.tencent"
                /*val tx = tencentMeetingPath.walk().filter { it.isDirectory && it.endsWith(".framework") }.map {
                    listOf("-framework","${it.nameWithoutExtension}","-F${tencentMeetingPath.absolutePath}")
                }.flatten().toList()
                compilerOpts(*systemFramework.toTypedArray(),*tx.toTypedArray())*/
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("TencentMeetingSDK"))
            }
            cinterops.create("BJLiveUIBaseCinterop") {
                definitionFile = thirdpartyPath.resolve("BJLiveUIBase.def")
                packageName = "what.the.fuck.with.baijiayun.live.cinterop.uibase"
                //ios-arm64
                //ios-arm64_x86_64-simulator
                /*val xcFrameworkTag=  when(platform){
                     "iphonesimulator"->"ios-arm64_x86_64-simulator"
                     "iphoneos"->"ios-arm64"
                     else -> ""
                 }

                 compilerOpts(*systemFramework.toTypedArray(),
                     "-framework", "BJLiveUIBase", "-F${downloadRoot.resolve("BaijiaYun/frameworks/").absolutePath}",
                     "-framework", "BJLLog", "-F${downloadRoot.resolve("BJLLog/frameworks/BJLLog.xcframework/$xcFrameworkTag").absolutePath}",
                     "-framework", "BJYIJKMediaFramework", "-F${downloadRoot.resolve("BJYIJK/BJYIJK/").absolutePath}",
                     "-framework", "BRTC", "-F${downloadRoot.resolve("BRTC/BRTC/").absolutePath}",
                 )*/
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("baijiayun/BJLiveUIBase"))
            }
            cinterops.create("iosMathCinterop") {
                definitionFile = thirdpartyPath.resolve("iosMath.def")
                packageName = "what.the.fuck.with.iosMath.cinterop"
//                extraOpts += listOf("-compiler-option", "-fmodules")
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("iosMath"))
            }
            cinterops.create("BJVideoPlayerCoreCinterop") {
                definitionFile = thirdpartyPath.resolve("BJVideoPlayerCore.def")
                packageName = "what.the.fuck.with.baijiayun.playback.cinterop.core"
                /*compilerOpts(*systemFramework.toTypedArray(),
                    "-framework", "BJVideoPlayerCore", "-F${downloadRoot.resolve("BaijiaYun/frameworks/").absolutePath}",
                )*/
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("baijiayun/BJVideoPlayerCore"))
            }
            cinterops.create("BJPlaybackUICinterop") {
                definitionFile = thirdpartyPath.resolve("BJPlaybackUI.def")
                packageName = "what.the.fuck.with.baijiayun.playback.cinterop.ui"
//                compilerOpts(*systemFramework.toTypedArray(),"-framework", "BJPlaybackUI", "-F${downloadRoot.resolve("BaijiaYun/frameworks/").absolutePath}")
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("baijiayun/BJPlaybackUI"))
            }
            cinterops.create("BJPlayerUIBaseCinterop") {
                definitionFile = thirdpartyPath.resolve("BJPlayerUIBase.def")
                packageName = "what.the.fuck.with.baijiayun.playback.cinterop.uibase"
//                compilerOpts(*systemFramework.toTypedArray(),"-framework", "BJPlayerUIBase", "-F${downloadRoot.resolve("BaijiaYun/frameworks/").absolutePath}")
                // 添加头文件目录
                includeDirs(thirdpartyPath.resolve("baijiayun/BJPlayerUIBase"))
            }
        }
    }




    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
                optIn("androidx.compose.runtime.ExperimentalComposeApi")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
                optIn("kotlin.experimental.ExperimentalObjCRefinement")
                optIn("kotlin.experimental.ExperimentalObjCName")
            }
        }
        commonMain.get().apply {
            kotlin.srcDir("${project.layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            // 全局注解处理器
            implementation(projects.annotation)
            // rust
            implementation(projects.rust)
            // 视频播放器
            implementation(projects.mediaPlayer)
            // 阴影
            implementation(libs.compose.shadow)
            // 通用注解
            implementation(libs.androidx.annotations)
            // 反射辅助
            api("io.github.ltttttttttttt:VirtualReflection-lib:1.2.1")//this, such as 1.2.1
            /***********************************************************
             *********************Jetbrains Compose*********************
             ************************************************************/
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // 原生导航
            implementation(libs.compose.navigation)
            // 生命周期
            implementation(libs.compose.lifecycle.runtime)
            implementation(libs.compose.lifecycle.viewmodel)
            // 时间
            implementation(libs.kotlinx.datetime)
            // io
            implementation(libs.kotlinx.io.core)
            /***********************************************************
             *********************Jetbrains Compose*********************
             ***********************************************************/
            // 导航
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.navigator.bottomsheet)
            implementation(libs.voyager.navigator.tab)
            implementation(libs.voyager.transitions)
            // 日志
            implementation(libs.logback)
            // 保利威视
            implementation(libs.polyv)
            // 图片预览
            implementation(libs.compose.scale.viewer)
            implementation(libs.compose.scale.sampling)
            implementation(libs.compose.scale.zoomableview)
            // material 库
            runtimeOnly(compose.material)
            // BOM 依赖
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))
            implementation(project.dependencies.platform(libs.kotlin.bom))
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(project.dependencies.platform(libs.ktor.bom))
            implementation(project.dependencies.platform(libs.coil.bom))

            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            // 图片加载
            implementation(libs.coil.compose)
            implementation(libs.coil.core)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)
            // 数据库
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.extensions.coroutines)
            // lottie动画
            implementation(libs.lottie)
            // 键值对存储
            implementation(libs.settings.noarg)
            // 序列化
            implementation(libs.kotlin.serialization.core)
            implementation(libs.kotlin.serialization.json)
            // 网络请求
            implementation(libs.ktor.http)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.client.encoding)
            implementation(libs.ktor.client.websocket)
            implementation(libs.ktor.client.resource)
            // 级联菜单
            implementation(libs.compose.dropdown)
            // 日历
            implementation(libs.compose.calendar)
            // webview
            implementation(libs.compose.webview)
            // 图片选择
            implementation(libs.compose.imagepicker)
            // 文件选择
            implementation(libs.compose.filepicker)
            // toast
            implementation(libs.compose.sonner)
            // 通用控件
            implementation(libs.compose.views)
            // 扫码
            implementation(libs.compose.qrcode.scan)
            // 下拉刷新
            implementation(libs.compose.swiperefresh)
            implementation(libs.compose.swiperefresh.classic)
            implementation(libs.compose.swiperefresh.progress)
            implementation(libs.compose.swiperefresh.lottie)
            // 文档查看
            implementation(libs.compose.documentviewer)

            kotlin("stdlib")
            // html解析
            implementation(libs.ksoup)
            // okio流处理
            implementation(libs.okio)
            // 权限请求
            implementation(libs.permissions.compose)
            // 面板模糊
            implementation("dev.chrisbanes.haze:haze:0.9.0-rc03")
            implementation("dev.chrisbanes.haze:haze-materials:0.9.0-rc03")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {

            implementation(compose.preview)
            implementation(libs.polyv.live)
            implementation(libs.polyv.common)
            implementation(libs.coil.video)
            implementation(libs.coil.gif)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.driver.android)
            implementation(libs.ktor.client.okhttp)
            // splash screen
            implementation(libs.androidx.splashscreen)
            implementation(libs.finclip)
            // CameraX core library
            implementation(libs.androidx.camera.core)
            // CameraX Camera2 extensions
            implementation(libs.androidx.camera.camera2)
            // CameraX Lifecycle library
            implementation(libs.androidx.camera.lifecycle)
            // CameraX View class
            implementation(libs.androidx.camera.view)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)

            implementation(libs.coil.network.okhttp)
            implementation(libs.richtext)
            implementation(libs.richtext.html)
            implementation(libs.richtext.markdown)

            implementation(kotlin("reflect"))

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.9.0")

            implementation("com.qcloud.cos:cos-android-lite-nobeacon:5.9.34")
            implementation("com.tencent.bugly:crashreport:4.1.9.3")
            implementation(
                libs.wemeet.get().let { "${it.module}:${it.versionConstraint.requiredVersion}" }) {
                exclude(group = "com.tencent.liteav", module = "LiteAVSDK_Player")
                exclude(group = "com.tencent.tbs", module = "tbssdk")
                exclude(group = "com.tencent.tbs", module = "tbsfile")
                exclude(group = "com.tencent.wemeet.third-party", module = "tbssdk-dynamic")
            }
            implementation(
                libs.baijia.live.get()
                    .let { "${it.module}:${it.versionConstraint.requiredVersion}" }) {
                exclude(group = "com.tencent.tbs", module = "tbssdk")
                exclude(group = "com.tencent.tbs", module = "tbsfile")
                exclude(group = "com.tencent.wemeet.third-party", module = "tbssdk-dynamic")
            }
            implementation(
                libs.baijia.playback.get()
                    .let { "${it.module}:${it.versionConstraint.requiredVersion}" }) {
                exclude(group = "com.tencent.tbs", module = "tbssdk")
                exclude(group = "com.tencent.tbs", module = "tbsfile")
                exclude(group = "com.tencent.wemeet.third-party", module = "tbssdk-dynamic")
            }

            implementation("com.umeng.umsdk:common:9.7.7")//必选
            implementation("com.umeng.umsdk:asms:1.8.3")//必选
            implementation("com.umeng.umsdk:uyumao:1.1.4") //高级运营分析功能依赖库（可选）。使用卸载分析、开启反作弊能力请务必集成，以免影响高级功能使用。common需搭配v9.6.3及以上版本，asms需搭配v1.7.0及以上版本。需更新隐私声明。需配置混淆，以避免依赖库无法生效，见本文下方【混淆设置】部分。

        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
            implementation(libs.ktor.client.darwin)
            // https://mvnrepository.com/artifact/com.rickclephas.kmp/nsexception-kt-core
            implementation("com.rickclephas.kmp:nsexception-kt-core:1.0.0-BETA-8")

        }
    }
}

configurations.all {
    exclude(group = "com.zzhoujay.markdown")
    //TODO
//    exclude(group = "com.vickyleu.filepicker")
}

dependencies {
    implementation(libs.androidx.lifecycle.common.jvm)
    val dependencyNotation = projects.processor
    add("kspCommonMainMetadata", dependencyNotation)
//    add("kspAndroid", dependencyNotation)
////    add("kspDesktop", dependencyNotation)
//     add("kspIosArm64", dependencyNotation)
    // add("kspIosX64", dependencyNotation)
    // add("kspMacosArm64", dependencyNotation)
    // add("kspMacosX64", dependencyNotation)
}

ksp {
    arg("buildDir", project.layout.buildDirectory.get().asFile.absolutePath)
}
// 在全局定义时间戳
val buildTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
buildkonfig {
    packageName = configProperties.getProperty("Packages")
    exposeObjectWithName = "BuildKonfig"
    // default config is required
    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "namespace",
            configProperties.getProperty("Packages")
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "timestamp",
            buildTimestamp
        )
        val flexible = (properties.getOrDefault("publish.flexible.host", "false")?.toString()
            ?.toBooleanStrictOrNull() ?: false).toString()
        buildConfigField(
            FieldSpec.Type.BOOLEAN,
            "flexible",
            flexible
        )
        val proxiesDisable =
            (properties.getOrDefault("publish.disable.proxies", "false")?.toString()
                ?.toBooleanStrictOrNull() ?: false).toString()
        buildConfigField(
            FieldSpec.Type.BOOLEAN,
            "proxiesDisable",
            proxiesDisable
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "gitCommitHash",
            getGitCommitHash()
        )
    }
    targetConfigs {
        // names in create should be the same as target names you specified
        create("android") {
//            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "official")
        }
        create("ios") {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "AppStore")
        }
    }
//    val allChannels =properties.getOrDefault("publish.all.channels","false")?.toString()?.toBooleanStrictOrNull()?:false
    val flavor = properties.getOrDefault("buildkonfig.flavor", "official")?.toString() ?: "official"
    if (flavor != "official") {
        makeFlavor(flavor)
    }
}

private fun BuildKonfigExtension.makeFlavor(flavor: String) {
    // flavor is passed as a first argument of targetConfigs
    this.targetConfigs(flavor = flavor) {
        this.create("android") {
//            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", flavor)
        }
        this.create("ios") {
            buildConfigField(FieldSpec.Type.STRING, "FLAVOR", "AppStore")
        }
    }
}


kotlin {
    sourceSets.commonMain {
        kotlin.srcDir("${project.layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/metadata/commonMain/kotlin")
    }
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-Xexpect-actual-classes", // remove warnings for expect classes
                "-Xskip-prerelease-check",
                "-opt-in=kotlinx.cinterop.BetaInteropApi",
                "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
                "-opt-in=kotlinx.cinterop.ExperimentalForeignApi",
                "-opt-in=org.jetbrains.compose.resources.InternalResourceApi",
            )
        )
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
    }
    // 使ksp 在每次 gradle build的时候就执行,提前生成代码
    tasks.withType<KotlinCompilationTask<*>>().configureEach { //KotlinCompile
        if (name != "kspCommonMainKotlinMetadata") {
            println("kspCommonMainKotlinMetadata===>>name::::${name}")
            if (tasks.findByName("kspCommonMainKotlinMetadata") != null) {
                dependsOn("kspCommonMainKotlinMetadata")
            }
        }
//        if (name in listOf(
//                "compileCommonMainKotlinMetadata",
//                "compileKotlinAndroid",
//                "compileKotlinIosX64"
//            )
//        ) {
//
//        }
    }
    tasks.withType<KspTaskMetadata>().configureEach {
        notCompatibleWithConfigurationCache("Configuration cache not supported due to serialization")
    }
}


compose.resources {
    publicResClass = true
    packageOfResClass = "${configProperties.getProperty("Packages")}.resources"
    generateResClass = always

}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    bundle.storeArchive.enable = true
    ndkVersion = "23.0.7599858"
    namespace = configProperties.getProperty("Packages")
    sourceSets["main"].apply {
        java.srcDirs("src/androidMain/java", "src/androidMain/kotlin")
        manifest {
            srcFile("src/androidMain/AndroidManifest.xml")
        }
        res {
            srcDirs("src/androidMain/res")
        }
        resources {
            srcDirs("src/commonMain/resources")
        }
    }
    defaultConfig {
        manifestPlaceholders += mapOf(
            "PACKAGE_NAME" to "$applicationId",
            "APP_NAME" to configProperties.getProperty("AppName").toString()
        )
        applicationId = configProperties.getProperty("AndroidBundleId")
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = configProperties.getProperty("AppBuild").toInt()
        versionName = configProperties.getProperty("AppVersion")
    }
    packaging {
        resources {
            excludes += listOf(
                "META-INF/*.RSA",
                "META-INF/*.SF",
                "META-INF/*.DSA",
                "META-INF/*.LIST",
                "license/LICENSE",
                "license/LICENSE.*.txt",
                "license/README.*.txt",
                "license/NOTICE",
                "play-services*",
                "*push_version",
                "**push_version",
                "javax/**/*",
                "junit/**/*",
                "junit/**/*",
                "firebase*.properties",
                "transport*.properties",
                "META-INF/*.properties",
                "META-INF/*.version",
                "androidsupportmultidexversion.txt",
                "DebugProbesKt.bin",
                "LICENSE-junit.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/license.txt",
                "META-INF/notice.txt",
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/ASL2.0",
                "META-INF/proguard/androidx-annotations.pro",
                "META-INF/atomicfu.kotlin_module",
            )
//            pickFirsts.add("META-INF/*")
//            pickFirsts.add("license/**/*")
//            pickFirsts.add("license/*")
        }
        jniLibs {
            keepDebugSymbols.add("lib/**/*.so")
            keepDebugSymbols.add("**/*.so")
            pickFirsts.add("lib/**/*.so")
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = "education"
            keyPassword = "123456"
            storeFile = file("./keystore.jks")
            storePassword = "123456"
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }
    // 配置 ABI 分包
    splits {
        abi {
            isEnable = true // 启用 ABI 分包
            reset()         // 清除默认配置
            include("armeabi-v7a", "arm64-v8a") // 包含 32 位和 64 位
            isUniversalApk = false // 不生成通用 APK
        }
    }


    // 定义 flavorDimension
    flavorDimensions += "flavor"

    // 定义产品风味
    productFlavors {
        val allChannels = properties.getOrDefault("publish.all.channels", "false")?.toString()
            ?.toBooleanStrictOrNull() ?: false
        if (allChannels) {
            listOf("official", "oppo", "vivo", "huawei").onEach {
                create(it) {
                    dimension = "flavor"
                    buildConfigField("String", "FLAVOR", "\"$it\"")
                }
            }
        } else {
            val flavor =
                properties.getOrDefault("buildkonfig.flavor", "official")?.toString() ?: "official"
            create(flavor) {
                dimension = "flavor"
                buildConfigField("String", "FLAVOR", "\"$flavor\"")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            multiDexKeepProguard = file("multidex-config.pro")
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            multiDexKeepProguard = file("multidex-config.pro")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // 修改 APK 输出名称
    androidComponents {
        // 千万不要删除这个方法,否则会导致编译失败,
        beforeVariants { variant ->
            // 如果变体包含 `oppo` flavor 并且是 debug 类型，则忽略该变体
            if (variant.buildType?.lowercase() == "debug") {
                println("variant.flavorName==${variant.flavorName}")
                variant.enable = false
            }
        }
        onVariants { variant ->
            variant.outputs.forEach { output ->
                val flavor = variant.flavorName ?: "official"
                val buildType = variant.buildType ?: "debug"
                //VariantOutput 根本不存在abi
                if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                    val abi =
                        output.filters.find { it.filterType == com.android.build.api.variant.FilterConfiguration.FilterType.ABI }?.identifier
                            ?: "universal"
                    val timestamp = LocalDateTime.from(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").parse(buildTimestamp)
                    )
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                    output.outputFileName = "${flavor}_${buildType}_${abi}_${timestamp}.apk"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvmTarget.get())
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.addAll(listOf("MissingTranslation", "UnusedResources", "Instantiatable"))
        checkDependencies = false
        checkAllWarnings = false
        checkTestSources = false
        abortOnError = false
        ignoreTestSources = true
        ignoreTestFixturesSources = true
        ignoreWarnings = true
    }
    buildFeatures {
        buildConfig = true
        compose = true
        dataBinding = true
    }

    configurations.all {
        resolutionStrategy {
            force(libs.slf4j)
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        debugImplementation(compose.uiTooling)
        testImplementation(libs.junit)
        testImplementation(libs.androidx.test.junit)
        testImplementation(libs.androidx.espresso.core)
    }
}
// Configure a Gradle plugin
sqldelight {
    linkSqlite = true
    databases {
        create("AppDatabase") {
            packageName.set("org.uooc.compose.core.database.core.database")
            deriveSchemaFromMigrations.set(true)
            generateAsync.set(true)
        }
    }
}
val buildDir: File = project.layout.buildDirectory.get().asFile
tasks.withType<PodBuildTask>().configureEach {
    val file = this.buildSettingsFile.get().asFile
    syntheticXCodeprojsTarget(buildDir, libs.versions.iosDeploymentTarget.get())
}
tasks.withType<PodspecTask>().configureEach {
    val platforms = kotlin.targets.filterIsInstance<KotlinNativeTarget>().map {
        when (it.targetName) {
            "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
            "iosArm64" -> "iphoneos"
            else -> error("Unsupported target ${it.targetName}")
        }
    }
    //
    updatePodspecFile(
        rootDir = rootDir,
        outputFile,
        xcodePath = xcodePath,
        archs = platforms,
        forceUseSpecialDevice = false,
        configProperties,
        libs.versions.iosDeploymentTarget.get()
    )
}

//*
// * TODO synthetic Pod 默认是Release 模式, 编译慢的要死, 改成Debug
//tasks.withType<PodSetupBuildTask>().configureEach {
//    val file = this.buildSettingsFile.get().asFile
//    doLast {
//        val content = file.readText()
//        file.writeText(content.replace(Regex("Release"),"Debug"))
//        println("PodSetupBuildTask===>$content")
//    }
//}
tasks.withType<PodGenTask>().configureEach {
    val platforms = kotlin.targets.filterIsInstance<KotlinNativeTarget>().map {
        when (it.targetName) {
            "iosX64", "iosSimulatorArm64" -> "iphonesimulator"
            "iosArm64" -> "iphoneos"
            else -> error("Unsupported target ${it.targetName}")
        }
    }
    syntheticPodfileGen(
        podfile.get(),
        xcodePath = xcodePath,
        archs = platforms,
        excludePods = listOf(
            /*
            "BJSwiftBase",
            "FinApplet",
            "BRTC",
            "BRTCCore",
            "BJLLog"*/
        )
    )
}
rootProject.plugins.withType(org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.NONE // NONE | FAIL
    rootProject.the<YarnRootExtension>().reportNewYarnLock = false // true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true // true
}

fun Project.getLinkFrameworkDirs(
    platform: String, buildType: String
): List<Pair<Boolean, Pair<File, String>>> {
    val podRoot = layout.buildDirectory.get()
        .asFile.resolve("cocoapods/synthetic/ios/")
    val downloadRoot = podRoot.resolve("Pods/")
    // 由于cocoapods插件只能使用源码, 不支持vendored_frameworks 和 vendored_libraries,所以需要通过-F 和 -l来指定framework和.a的路径
    val xcFrameworkTag = when (platform) {
        "iphonesimulator" -> "ios-arm64_x86_64-simulator"
        "iphoneos" -> "ios-arm64"
        else -> ""
    }
    val podBugLink = listOf(
        downloadRoot.resolve("BaijiaYun/frameworks"),
        downloadRoot.resolve("BJLLog/frameworks/BJLLog.xcframework/$xcFrameworkTag"),
        downloadRoot.resolve("BRTC/BRTC"),
        downloadRoot.resolve("Bugly"),
        downloadRoot.resolve("PLVLiveScenesSDK/Frameworks/MiniFramework"),
        downloadRoot.resolve("PLVBusinessSDK/Frameworks/MiniFramework"),
        downloadRoot.resolve("PLVFoundationSDK/Frameworks/MiniFramework")
    )


    val podBugStaticLink = listOf(
//        projectDir.resolve("src/nativeInterop/thirdparty/swift/$platform"),
        downloadRoot.resolve("BaijiaYun/library/libZXYBSDK"),
//        when(platform){
//            "iphonesimulator" -> Paths.get("/Library/Developer/CoreSimulator/Volumes/iOS_22A3351/Library/Developer/CoreSimulator/Profiles/Runtimes/iOS 18.0.simruntime/Contents/Resources/RuntimeRoot/usr/lib/swift/").toFile()
//            "iphoneos" -> Paths.get("/Users/vickyleu/Library/Developer/Xcode/iOS DeviceSupport/iPhone14,2 18.1 (22B83)/Symbols/usr/lib/swift/").toFile()
//            else ->null
//        },
    ).filterNotNull()
    val releaseRoot = podRoot.resolve("build/$buildType-$platform/")

    val intermediatesDir = releaseRoot.resolve("XCFrameworkIntermediates")
    val thirdpartyPath = projectDir.resolve("src/nativeInterop/thirdparty")
    val tencentMeetingPath = projectDir.resolve("../iosApp/iosApp/TencentMeetingSDK")

    //TXLiteAVSDK_TRTC
    val trtcDir = intermediatesDir.resolve("TXLiteAVSDK_TRTC")

    // 动态获取路径
    val xcodePath = getXcodeSelectPath()
    val libs = findTopLevelParentDirectories(
        listOf(
            Triple(false, releaseRoot, trtcDir),
            Triple(false, tencentMeetingPath, null),
            *podBugLink.map {
                Triple(false, it, null)
            }.toTypedArray()
        ), platform = platform
    ).toList()+
            (podBugStaticLink.map {
        true to (it to "")
//        Triple(true, it, null)
    }
        .toTypedArray()) + arrayOf(true to (File("$xcodePath/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$platform") to ""))
    // 使用 distinctBy 来去除重复的目录
    val distinctLibs = libs.distinctBy { it.second.first.absolutePath }
    return distinctLibs
      //  .filter {
  //      it.second.first.absolutePath.contains("Frameworks/MiniFramework").not()
    //}
        .apply {
        this.map { it.second.first.absolutePath }.apply {
            println("distinctLibs===>${this.joinToString(",")}")
        }
    }
//    return emptySet()
}