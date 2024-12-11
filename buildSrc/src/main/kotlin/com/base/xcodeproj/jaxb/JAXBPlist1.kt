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
import com.base.xcodeproj.Plist
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlRootElement(name = "plist")
class JAXBPlist : Plist {
    @get:XmlAttribute
    override var version: String? = null

    @get:XmlJavaTypeAdapter(
        JAXBDictAdapter::class
    )
    @get:XmlElement(name = "dict", required = true)
    override var dict: Dict? = null

    override fun createArray(): Array {
        return ArrayListArray()
    }

    override fun createDict(): Dict {
        return LinkedHashMapDict()
    }
}
