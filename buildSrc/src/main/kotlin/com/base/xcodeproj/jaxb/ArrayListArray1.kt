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

internal class ArrayListArray : ArrayList<Any?>(), Array {
    override fun getString(index: Int): String {
        return get(index) as String
    }

    override fun addString(value: String?) {
        add(value)
    }

    override fun setString(index: Int, value: String?) {
        set(index, value)
    }

    override fun getInteger(index: Int): Int {
        return get(index) as Int
    }

    override fun addInteger(value: Int?) {
        add(value)
    }

    override fun setInteger(index: Int, value: Int?) {
        set(index, value)
    }

    override fun getDouble(index: Int): Double {
        return get(index) as Double
    }

    override fun addDouble(value: Double?) {
        add(value)
    }

    override fun setDouble(index: Int, value: Double?) {
        set(index, value)
    }

    override fun getBool(index: Int): Boolean {
        return get(index) as Boolean
    }

    override fun addBool(value: Boolean?) {
        add(value)
    }

    override fun setBool(index: Int, value: Boolean?) {
        set(index, value)
    }

    override fun getDate(index: Int): Date {
        return get(index) as Date
    }

    override fun setDate(index: Int, value: Date?) {
        set(index, value)
    }

    override fun addDate(value: Date?) {
        add(value)
    }

    override fun getData(index: Int): ByteArray {
        return get(index) as ByteArray
    }

    override fun getDataAsUTF8String(index: Int): String {
        val bytes: ByteArray = get(index) as ByteArray
        try {
            return String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    override fun setData(index: Int, value: ByteArray?) {
        set(index, value)
    }

    override fun setDataAsUTF8String(index: Int, value: String) {
        try {
            set(index, value.toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    override fun addData(value: ByteArray?) {
        add(value)
    }

    override fun addDataAsUTF8String(value: String) {
        try {
            add(value.toByteArray(charset("UTF-8")))
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    override fun getDict(index: Int): Dict {
        return get(index) as Dict
    }

    override fun addDict(value: Dict?) {
        add(value)
    }

    override fun setDict(index: Int, value: Dict?) {
        set(index, value)
    }

    override fun getArray(index: Int): Array {
        return get(index) as Array
    }

    override fun addArray(value: Array?) {
        add(value)
    }

    override fun setArray(index: Int, value: Array?) {
        set(index, value)
    }

    companion object {
        private val serialVersionUID: Long = -7528319956295843074L
    }
}
