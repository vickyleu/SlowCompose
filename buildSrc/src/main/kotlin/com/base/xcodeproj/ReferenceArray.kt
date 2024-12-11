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

class ReferenceArray<T : Element>(
    private val projectFile: ProjectFile?,
    private val refs: Array?,
    private val elementFactory: ElementFactory<T>
) :
    Iterable<T?> {
    override fun iterator(): MutableIterator<T?> {
        return object : MutableIterator<T?> {
            private val refsIterator: Iterator<Any?> = refs!!.iterator()

            override fun hasNext(): Boolean {
                return refsIterator.hasNext()
            }

            override fun next(): T {
                return createObjectFromRef(refsIterator.next() as? String)
            }

            override fun remove() {
                throw UnsupportedOperationException()
            }
        }
    }

    private fun createObjectFromRef(ref: String?): T {
        return elementFactory.create(projectFile!!, projectFile.getObjectByReference(ref))
    }

    fun get(index: Int): T {
        val ref: String? = refs!!.get(index) as String?
        return createObjectFromRef(ref)
    }

    fun getByName(name: String): T? {
        for (`object`: T? in this) {
            if (name == `object`?.dict?.getString("name")) {
                return `object`
            }
        }
        return null
    }

    fun size(): Int {
        return refs!!.size
    }

    fun add(`object`: T) {
        val ref: String = projectFile!!.generateReference()
        projectFile.setObjectByReference(ref, `object`.dict)
        refs!!.add(ref)
    }
}
