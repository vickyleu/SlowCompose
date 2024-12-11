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

interface Dict : MutableMap<String?, Any?> {
    fun getString(key: String?): String

    fun setString(key: String?, value: String?)

    fun getInteger(key: String?): Int

    fun setInteger(key: String?, value: Int?)

    fun getDouble(key: String?): Double

    fun setDouble(key: String?, value: Double?)

    fun getBool(key: String?): Boolean

    fun setBool(key: String?, value: Boolean?)

    fun getDate(key: String?): Date

    fun setDate(key: String?, value: Date?)

    fun getData(key: String?): ByteArray

    fun getDataAsUTF8String(key: String?): String

    fun setData(key: String?, value: ByteArray?)

    fun setDataAsUTF8String(key: String?, value: String)

    fun getArray(key: String?): Array

    fun getOrCreateAndSetArray(key: String?): Array

    fun setArray(key: String?, value: Array?)

    fun getDict(key: String?): Dict

    fun getOrCreateAndSetDict(key: String?): Dict

    fun setDict(key: String?, value: Dict?)
}
