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

import com.base.xcodeproj.buildphases.BuildPhase

class Target internal constructor(projectFile: ProjectFile?, target: Dict?) :
    NamedElement(projectFile, target) {
    constructor(projectFile: ProjectFile) : this(projectFile, projectFile.createDict())

    val buildConfigurationList: BuildConfigurationList
        get() {
            val buildConfigurationListRef: String? =
                dict?.getString("buildConfigurationList")
            val buildConfigurationList: Dict? =
                projectFile?.getObjectByReference(buildConfigurationListRef)
            return BuildConfigurationList(
                projectFile,
                buildConfigurationList
            )
        }

    val buildPhases: ReferenceArray<BuildPhase>
        get() {
            return ReferenceArray<BuildPhase>(projectFile,
                dict?.getOrCreateAndSetArray("buildPhases"),
                object :
                    ElementFactory<BuildPhase> {
                    override fun create(
                        projectFile: ProjectFile,
                        dict: Dict?
                    ): BuildPhase {
                        return BuildPhase.Companion.create(
                            projectFile,
                            dict
                        )
                    }
                })
        }
}
