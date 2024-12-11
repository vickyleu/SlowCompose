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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.xml.bind.DatatypeConverter

class JAXBPlistElementConverter
    (private val dictAdapter: JAXBDictAdapter, private val arrayAdapter: JAXBArrayAdapter) {
    private val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    init {
        format.setTimeZone(TimeZone.getTimeZone("UTC"))
    }

    @Throws(Exception::class)
    fun convertFromJAXB(value: Any?): Any? {
        var value: Any? = value
        if (value is JAXBDict) {
            value = dictAdapter.unmarshal(value)
        } else if (value is JAXBArray) {
            value = arrayAdapter.unmarshal(value)
        } else if (value is JAXBTrue) {
            value = java.lang.Boolean.TRUE
        } else if (value is JAXBFalse) {
            value = java.lang.Boolean.FALSE
        } else if (value is JAXBDate) {
            value = DatatypeConverter.parseDateTime(value.value).getTime()
        } else if (value is JAXBData) {
            value = DatatypeConverter.parseBase64Binary(value.value)
        }
        return value
    }

    @Throws(Exception::class)
    fun convertToJAXB(value: Any?): Any? {
        var value: Any? = value
        if (value is Dict) {
            value = dictAdapter.marshal(value)
        } else if (value is Array) {
            value = arrayAdapter.marshal(value)
        } else if (value is Boolean) {
            value = if (value) JAXBTrue() else JAXBFalse()
        } else if (value is Date) {
            val date: JAXBDate = JAXBDate()
            date.value = format.format(value)
            value = date
        } else if (value is ByteArray) {
            val data: JAXBData = JAXBData()
            data.value = DatatypeConverter.printBase64Binary(value)
            value = data
        }
        return value
    }
}
