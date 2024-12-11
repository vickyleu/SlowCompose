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
package com.base.xcodeproj.buildphases

import com.base.xcodeproj.Dict
import com.base.xcodeproj.Element
import com.base.xcodeproj.ProjectFile

abstract class BuildPhase @JvmOverloads constructor(
    projectFile: ProjectFile,
    dict: Dict? = projectFile.createDict()
) :
    Element(projectFile, dict) {
    companion object {
        private val buildPhasesPackage: Package = BuildPhase::class.java.getPackage()

        fun create(projectFile: ProjectFile?, dict: Dict?): BuildPhase {
            val isa = dict?.getString("isa")
            try {
                val clazz = Class.forName(buildPhasesPackage.name + "." + isa)
                return clazz.getDeclaredConstructor(
                    *arrayOf(
                        ProjectFile::class.java,
                        Dict::class.java
                    )
                ).newInstance(
                    projectFile, dict
                ) as BuildPhase
            } catch (e: RuntimeException) {
                throw e
            } catch (e: Exception) {
                throw RuntimeException(
                    "Could not instanciate build phase for type (isa) '$isa'.",
                    e
                )
            }
        }
    }
}