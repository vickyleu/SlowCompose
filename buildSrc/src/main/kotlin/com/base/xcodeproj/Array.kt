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
package com.base.xcodeproj

import java.util.Date

interface Array : MutableList<Any?> {
    fun getString(index: Int): String

    fun addString(value: String?)

    fun setString(index: Int, value: String?)

    fun getInteger(index: Int): Int

    fun addInteger(value: Int?)

    fun setInteger(index: Int, value: Int?)

    fun getDouble(index: Int): Double

    fun addDouble(value: Double?)

    fun setDouble(index: Int, value: Double?)

    fun getBool(index: Int): Boolean

    fun addBool(value: Boolean?)

    fun setBool(index: Int, value: Boolean?)

    fun getDate(index: Int): Date

    fun setDate(index: Int, value: Date?)

    fun addDate(value: Date?)

    fun getData(index: Int): ByteArray

    fun getDataAsUTF8String(index: Int): String

    fun setData(index: Int, value: ByteArray?)

    fun setDataAsUTF8String(index: Int, value: String)

    fun addData(value: ByteArray?)

    fun addDataAsUTF8String(value: String)

    fun getDict(index: Int): Dict

    fun addDict(value: Dict?)

    fun setDict(index: Int, value: Dict?)

    fun getArray(index: Int): Array

    fun addArray(value: Array?)

    fun setArray(index: Int, value: Array?)
}
