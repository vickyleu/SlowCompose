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

import com.base.xcodeproj.BuildFile
import com.base.xcodeproj.Dict
import com.base.xcodeproj.ElementFactory
import com.base.xcodeproj.ProjectFile
import com.base.xcodeproj.ReferenceArray

class PBXShellScriptBuildPhase @JvmOverloads constructor(
    projectFile: ProjectFile,
    dict: Dict? = projectFile.createDict()
) :
    BuildPhase(projectFile, dict) {
    fun setDefaultValues() {
        val d: Dict? = dict
        d!!.setString("isa", isa)
        d.setArray("files", projectFile?.createArray())
        d.setArray("inputPaths", projectFile?.createArray())
        d.setArray("outputPaths", projectFile?.createArray())
        d.setString("runOnlyForDeploymentPostprocessing", "0")
        d.setString("shellPath", "/bin/sh")
    }

    val files: ReferenceArray<BuildFile>
        get() = ReferenceArray(
            projectFile, dict?.getOrCreateAndSetArray("files"),
            BuildFileFactory()
        )

    val inputPaths: ReferenceArray<BuildFile>
        get() {
            return ReferenceArray(
                projectFile, dict?.getOrCreateAndSetArray("inputPaths"),
                BuildFileFactory()
            )
        }

    val outputPaths: ReferenceArray<BuildFile>
        get() {
            return ReferenceArray(
                projectFile, dict?.getOrCreateAndSetArray("outputPaths"),
                BuildFileFactory()
            )
        }

    val runOnlyForDeploymentPostprocessing: String?
        get() {
            return dict?.getString("runOnlyForDeploymentPostprocessing")
        }

    val shellPath: String?
        get() {
            return dict?.getString("shellPath")
        }

    var shellScript: String?
        get() {
            return dict?.getString("shellScript")
        }
        set(script) {
            dict?.setString("shellScript", script)
        }

    private class BuildFileFactory : ElementFactory<BuildFile> {
        override fun create(projectFile: ProjectFile, dict: Dict?): BuildFile {
            return BuildFile(projectFile, dict)
        }
    }

    companion object {
        val isa: String = PBXShellScriptBuildPhase::class.java.simpleName
    }
}
