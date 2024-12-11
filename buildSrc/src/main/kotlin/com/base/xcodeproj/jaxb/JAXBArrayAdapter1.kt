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
import javax.xml.bind.annotation.adapters.XmlAdapter

class JAXBArrayAdapter :
    XmlAdapter<JAXBArray, Array>() {
    @Throws(Exception::class)
    override fun marshal(array: Array): JAXBArray {
        val jaxbArray: JAXBArray = JAXBArray()
        val elements: Array = JAXBPlist().createArray()
        val converter: JAXBPlistElementConverter = JAXBPlistElementConverter(
            JAXBDictAdapter(),
            this
        )
        for (value: Any? in array) {
            var value: Any? = value
            value = converter.convertToJAXB(value)
            elements.add(value)
        }
        jaxbArray.elements=elements
        return jaxbArray
    }

    @Throws(Exception::class)
    override fun unmarshal(jaxbArray: JAXBArray): Array {
        val array: Array = JAXBPlist().createArray()
        val converter: JAXBPlistElementConverter = JAXBPlistElementConverter(
            JAXBDictAdapter(),
            this
        )
        for (value: Any? in jaxbArray.elements) {
            var value: Any? = value
            value = converter.convertFromJAXB(value)
            array.add(value)
        }
        return array
    }
}