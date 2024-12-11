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

import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Locale

class ProjectFile(val plist: Plist) : Element(null, plist.dict) {
    fun createArray(): Array {
        return plist.createArray()
    }

    fun createDict(): Dict {
        return plist.createDict()
    }

    val version: String?
        get() {
            return plist.version
        }

    val objectVersion: String?
        get() {
            return dict?.getString("objectVersion")
        }

    fun getObjectByReference(reference: String?): Dict? {
        return objects!!.get(reference) as Dict?
    }

    fun setObjectByReference(reference: String, `object`: Dict?) {
        objects!!.put(reference, `object`)
    }

    fun generateReference(): String {
        var md: MessageDigest? = null
        var prng: SecureRandom? = null
        try {
            md = MessageDigest.getInstance("SHA1")
            prng = SecureRandom.getInstance("SHA1PRNG")
        } catch (e: NoSuchAlgorithmException) {
        }

        val randomNum: String = prng!!.nextInt().toString()
        val ref: String = String(Hex.encodeHex(md!!.digest(randomNum.toByteArray())))
        return ref.uppercase(Locale.getDefault()).substring(0, 24)
    }

    private val objects: Dict?
        get() {
            return dict?.getDict("objects")
        }

    val project: Project
        get() {
            val projectRef: String? = dict?.getString("rootObject")
            val project: Dict? = getObjectByReference(projectRef)
            return Project(this, project)
        }
}
