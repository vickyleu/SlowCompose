/*
 * #%L
 * xcode-project-reader
 * %%
 * Copyright (C) 2012 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.base.xcodeproj.jaxb

import com.base.xcodeproj.Array
import com.base.xcodeproj.Dict
import java.io.UnsupportedEncodingException
import java.util.Date

internal class LinkedHashMapDict : LinkedHashMap<String?, Any?>(), Dict {
    override fun getString(key: String?): String {
        return get(key) as String
    }

    override fun setString(key: String?, value: String?) {
        put(key, value)
    }

    override fun getInteger(key: String?): Int {
        return get(key) as Int
    }

    override fun setInteger(key: String?, value: Int?) {
        put(key, value)
    }

    override fun getDouble(key: String?): Double {
        return get(key) as Double
    }

    override fun setDouble(key: String?, value: Double?) {
        put(key, value)
    }

    override fun getBool(key: String?): Boolean {
        return get(key) as Boolean
    }

    override fun setBool(key: String?, value: Boolean?) {
        put(key, value)
    }

    override fun getDate(key: String?): Date {
        return get(key) as Date
    }

    override fun setDate(key: String?, value: Date?) {
        put(key, value)
    }

    override fun getData(key: String?): ByteArray {
        return get(key) as ByteArray
    }

    override fun getDataAsUTF8String(key: String?): String {
        val bytes: ByteArray = get(key) as ByteArray
        try {
            return String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    override fun setData(key: String?, value: ByteArray?) {
        put(key, value)
    }

    override fun setDataAsUTF8String(key: String?, value: String) {
        try {
            put(key, value.toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    override fun getArray(key: String?): Array {
        return get(key) as Array
    }

    override fun getOrCreateAndSetArray(key: String?): Array {
        var array: Array? = getArray(key)
        if (array == null) {
            array = JAXBPlist().createArray()
            setArray(key, array)
        }
        return array
    }

    override fun setArray(key: String?, value: Array?) {
        put(key, value)
    }

    override fun getDict(key: String?): Dict {
        return get(key) as Dict
    }

    override fun getOrCreateAndSetDict(key: String?): Dict {
        var dict: Dict? = getDict(key)
        if (dict == null) {
            dict = JAXBPlist().createDict()
            setDict(key, dict)
        }
        return dict
    }

    override fun setDict(key: String?, value: Dict?) {
        put(key, value)
    }

    companion object {
        private const val serialVersionUID: Long = 6861596829305840360L
    }
}
