package org.uooc.compose.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import org.uooc.compose.models.NoProguard
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Pref<T : Any>(
    private val key: String?,
    private val default: T,
    private val callback: ((T) -> Unit)? = null
) {
    private var state = mutableStateOf(default)

    private var _settingsInstance: Settings? = null
    private val _settings: Lazy<Settings?>
        get() = lazy {
            if(_settingsInstance!=null){
                return@lazy _settingsInstance
            }
            try {
                Settings().apply {
                    _settingsInstance = this
                }
            }catch (e:Exception){
                null
            }
        }
    private val settings:Settings?
        get() = _settings.value

    init {
        state.value = get()
    }

    fun asFlow(): Flow<T> {
        return snapshotFlow { state.value }
    }

    fun get(): T {
        val name = key ?: return state.value
        return getValueInner(name)
    }

    fun set(value: T) {
        val name = key ?: return
        setValueInner(name, value)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getValueInner(name: String): T {
        val settings = settings?:return state.value
        val s: T = when (default::class) {
            Int::class -> {
                if (settings.hasKey(key = name)) {
                    try {
                        (settings.getIntOrNull(key = name)?:default as Int) as T
                    }catch (e:Exception){
                        (default as Int) as T
                    }
                } else {
                    default
                }
            }

            Long::class -> {
                if (settings.hasKey(key = name)) {
                    try {
                        (settings.getLongOrNull(key = name)?:default as Long) as T
                    }catch (e:Exception){
                        (default as Long) as T
                    }
                } else {
                    default
                }
            }

            String::class -> {
                if (settings.hasKey(key = name)) {
                    try {
                        (settings.getStringOrNull(key = name)?:default as String) as T
                    }catch (e:Exception){
                        (default as String) as T
                    }
                } else {
                    default
                }
            }

            Float::class -> {
                if (settings.hasKey(key = name)) {
                    try {
                        (settings.getFloatOrNull(key = name)?:default as Float) as T
                    }catch (e:Exception){
                        (default as Float) as T
                    }
                } else {
                    default
                }
            }

            Double::class -> {
                if (settings.hasKey(key = name)) {
                    try {
                        (settings.getDoubleOrNull(key = name)?:default as Double) as T
                    }catch (e:Exception){
                        (default as Double) as T
                    }
                } else {
                    default
                }
            }

            Boolean::class -> {
                (if (settings.hasKey(key = name)) {
                    try {
                        (settings.getBooleanOrNull(key = name)?:default as Boolean) as T
                    }catch (e:Exception){
                        (default as Boolean) as T
                    }
                } else {
                    default
                })
            }

            NoProguard::class -> {
                val content: String? = settings.getStringOrNull(key = name)
                if (content != null) {
                    default::class.serializerOrNull()?.let {
                        // 我想将NoProguard的序列化存储为文本信息,这里是从文本信息中还原出来
                        try {
                            Json.decodeFromString(it.nullable, content) as? T
                        } catch (e: Exception) {
                            null
                        }
                    } ?: default
                } else {
                    default
                }
            }

            else -> throw IllegalArgumentException("Invalid type!")
        }
        state.value = s
        return state.value
    }

    private fun setValueInner(name: String, value: T) {
        val settings = settings?:return
        when (default::class) {
            Int::class -> {
                settings.set(key = (name), value = value as Int)
            }

            Long::class -> {
                settings.set(key = (name), value = value as Long)
            }

            String::class -> {
                settings.set(key = (name), value = value as String)
            }

            Float::class -> {
                settings.set(key = (name), value = value as Float)
            }

            Double::class -> {
                settings.set(key = (name), value = value as Double)
            }

            Boolean::class -> {
                settings.set(key = (name), value = value as Boolean)
            }

            NoProguard::class -> {
                @Suppress("UNCHECKED_CAST")
                (default::class.serializerOrNull() as? KSerializer<T>)?.apply {
                    try {
                        val json = Json.encodeToString(this@apply, value)
                        settings.set(key = (name), value = json)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            else -> throw IllegalArgumentException("Invalid type!")
        }
        state.value = value
        callback?.invoke(value)
    }

    operator fun getValue(any: Any, property: KProperty<*>): T {
        return getValueInner(key ?: property.name)
    }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        setValueInner(key ?: property.name, value)
    }

}

 fun < AnyClz,  T : Any> AnyClz.pref(
    default: T,
    key: String? = null,
     callback: ((T) -> Unit)? = null
): Pref<T> {
    return Pref(key, default, callback)
}