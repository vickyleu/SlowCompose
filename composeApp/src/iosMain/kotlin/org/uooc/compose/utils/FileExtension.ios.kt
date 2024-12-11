package org.uooc.compose.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.uikit.ComposeUIViewControllerConfiguration
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.uooc.compose.utils.encode.md5
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual suspend fun createTempFile(
    context: PlatformContext,
    launcher: StorageLauncher?,
    key: String, name: String
): FileImpl? {
    val completer = CompletableDeferred<FileImpl?>()
    withContext(Dispatchers.IO) {
        // 定义长期保留的文件目录
        val directory =(baseDir()?: return@withContext completer.complete(null))
        val subDir = directory.URLByAppendingPathComponent("temp_${key.md5()}")?: return@withContext completer.complete(null)
        val dir = FileImpl(subDir.path!!)
        dir.mkdirs()
        val fileName = name.ifEmpty { ".tmp" }
        val file = FileImpl(dir, fileName)
        completer.complete(file)
    }
    return completer.await()
}


private fun baseDir(): NSURL? {
    /*val libraryDir = NSSearchPathForDirectoriesInDomains(
        directory = NSLibraryDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true
    ).firstOrNull()?.let {
         NSURL.fileURLWithPath(it as String)
    }*/
    val documentDir: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSCachesDirectory,
//        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val directory = documentDir?.URLByAppendingPathComponent("download")
    return directory
}


actual fun getAllCacheDir(context: PlatformContext): List<FileImpl> {
    // 定义长期保留的文件目录
    val temp = mutableListOf<FileImpl>()
    val directory =baseDir()
    directory?.let {
        temp.add(FileImpl(it.path!!))
    }
    getCacheDirectory("image_cache").absoluteString?.let {
        temp.add(FileImpl(it))
    }
    return temp
}

actual val LocalStorageLauncher = compositionLocalOf<StorageLauncher?>(structuralEqualityPolicy()) {
    StorageLauncher()
}

actual class StorageLauncher {

}
/**
 * 将所有其他格式的图片类型转换为jpg格式
 */
actual fun convertToJpg(byteArray: ByteArray): ByteArray {
    memScoped {
        byteArray.usePinned { byteArrayPin->
            val ptr = byteArrayPin.addressOf(0)
            // 将 ByteArray 转换为 NSData
            val nsData = NSData.create(bytes = ptr, length = byteArray.size.toULong())
            // 使用 UIImage 解析图片数据
            val image = UIImage(data = nsData)
            // 将 UIImage 转换为 JPG 数据
            val jpgData = UIImageJPEGRepresentation(image, 1.0) // 质量100%
            // 将 NSData 转回 ByteArray
            return jpgData?.toByteArray() ?: byteArray
        }
    }
}

actual typealias CommonControllerConfiguration = ComposeUIViewControllerConfiguration