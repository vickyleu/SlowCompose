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
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElements
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlType(name = "array")
@XmlJavaTypeAdapter(
    JAXBArrayAdapter::class
)
class JAXBArray {
    @get:XmlElements(
        XmlElement(
            name = "string",
            type = String::class
        ),
        XmlElement(
            name = "dict",
            type = JAXBDict::class
        ),
        XmlElement(
            name = "array",
            type = JAXBArray::class
        ),
        XmlElement(name = "integer", type = Int::class),
        XmlElement(name = "real", type = Double::class),
        XmlElement(
            name = "true",
            type = JAXBTrue::class
        ),
        XmlElement(
            name = "false",
            type = JAXBFalse::class
        ),
        XmlElement(
            name = "date",
            type = JAXBDate::class
        ),
        XmlElement(
            name = "data",
            type = JAXBData::class
        )
    )
    var elements: Array = JAXBPlist().createArray()
}
