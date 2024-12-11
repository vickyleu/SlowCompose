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
import javax.xml.bind.annotation.adapters.XmlAdapter

class JAXBDictAdapter :
    XmlAdapter<JAXBDict, Dict>() {
    @Throws(Exception::class)
    override fun marshal(dict: Dict): JAXBDict {
        val jaxbDict: JAXBDict = JAXBDict()
        val elements: Array = JAXBPlist().createArray()
        val converter: JAXBPlistElementConverter = JAXBPlistElementConverter(
            this, JAXBArrayAdapter()
        )
        for (entry: Map.Entry<String?, Any?> in dict.entries) {
            val key: JAXBKey = JAXBKey()
            key.value = entry.key
            elements.add(key)

            var value: Any? = entry.value
            value = converter.convertToJAXB(value)

            elements.add(value)
        }
        jaxbDict.elements = elements
        return jaxbDict
    }

    @Throws(Exception::class)
    override fun unmarshal(jaxbDict: JAXBDict): Dict {
        val dict: Dict = LinkedHashMapDict()
        val converter: JAXBPlistElementConverter = JAXBPlistElementConverter(
            this, JAXBArrayAdapter()
        )
        var i: Int = 0
        while (i < jaxbDict.elements.size) {
            var key: Any? = jaxbDict.elements.get(i)
            if (key is JAXBKey) {
                key = key.value
            }

            var value: Any? = jaxbDict.elements.get(i + 1)
            value = converter.convertFromJAXB(value)

            dict.put(key as String?, value)
            i += 2
        }
        return dict
    }
}