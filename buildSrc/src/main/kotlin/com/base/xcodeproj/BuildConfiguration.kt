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

class BuildConfiguration internal constructor(projectFile: ProjectFile?, config: Dict?) :
    NamedElement(projectFile, config) {
    constructor(projectFile: ProjectFile) : this(projectFile, projectFile.createDict())

    val buildSettings: BuildSettings
        get() {
            return BuildSettings(
                projectFile,
                dict?.getDict("buildSettings")
            )
        }
}
