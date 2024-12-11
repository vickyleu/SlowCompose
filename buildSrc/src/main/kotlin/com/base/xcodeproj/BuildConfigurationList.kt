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

class BuildConfigurationList internal constructor(
    projectFile: ProjectFile?,
    buildConfigurationList: Dict?
) :
    Element(projectFile, buildConfigurationList) {
    constructor(projectFile: ProjectFile) : this(projectFile, projectFile.createDict())

    val defaultConfigurationName: String?
        get() {
            return dict?.getString("defaultConfigurationName")
        }

    val buildConfigurations: ReferenceArray<BuildConfiguration>
        get() {
            return ReferenceArray(
                projectFile,
                dict?.getOrCreateAndSetArray("buildConfigurations"),
                object : ElementFactory<BuildConfiguration> {
                    override fun create(
                        projectFile: ProjectFile,
                        dict: Dict?
                    ): BuildConfiguration {
                        return BuildConfiguration(projectFile, dict)
                    }
                })
        }
}
