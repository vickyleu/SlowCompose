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

/**
 * The Plist root object. An Xcode project file for example uses the plist format to store its
 * information.
 */
interface Plist {
    var version: String?

    /**
     * @return the root dict of the Plist.
     */
    val dict: Dict?

    /**
     * Factory method. Creates a new array but does not add it to the plist.
     */
    fun createArray(): Array

    /**
     * Factory method. Creates a new dict but does not add it to the plist.
     */
    fun createDict(): Dict
}