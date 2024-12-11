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

import com.base.xcodeproj.Plist
import org.apache.commons.lang3.SystemUtils
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.PropertyException
import javax.xml.bind.Unmarshaller
import javax.xml.bind.ValidationEvent
import javax.xml.bind.ValidationEventHandler
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource

class JAXBPlistParser {
    @Throws(
        SAXException::class,
        ParserConfigurationException::class,
        FileNotFoundException::class,
        JAXBException::class
    )
    fun load(projectFile: String): Plist {
        return load(File(projectFile))
    }

    @Throws(
        SAXException::class,
        ParserConfigurationException::class,
        FileNotFoundException::class,
        JAXBException::class
    )
    fun load(projectFile: File): Plist {
        val project: InputSource = InputSource(FileReader(projectFile))
        return unmarshallPlist(project)
    }

    @Throws(
        SAXException::class,
        ParserConfigurationException::class,
        JAXBException::class
    )
    fun load(project: InputSource): Plist {
        return unmarshallPlist(project)
    }

    @Throws(
        SAXException::class,
        ParserConfigurationException::class,
        JAXBException::class
    )
    private fun unmarshallPlist(project: InputSource): Plist {
        val dtd: InputSource = InputSource(javaClass.getResourceAsStream("/PropertyList-1.0.dtd"))
        val ss: SAXSource = createSAXSource(project, dtd)
        val ctx: JAXBContext = JAXBContext.newInstance(JAXBPlist::class.java)
        val unmarshaller: Unmarshaller = ctx.createUnmarshaller()

        // unexpected elements should cause an error
        unmarshaller.setEventHandler(object : ValidationEventHandler {
            override fun handleEvent(event: ValidationEvent): Boolean {
                return false
            }
        })

        return unmarshaller.unmarshal(ss) as Plist
    }

    @Throws(SAXException::class, ParserConfigurationException::class)
    private fun createSAXSource(project: InputSource, dtd: InputSource): SAXSource {
        val xmlReader: XMLReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader()
        xmlReader.setEntityResolver(object : EntityResolver {
            @Throws(SAXException::class)
            override fun resolveEntity(pid: String, sid: String): InputSource {
                if (sid == "http://www.apple.com/DTDs/PropertyList-1.0.dtd") return dtd
                throw SAXException("unable to resolve remote entity, sid = " + sid)
            }
        })
        val ss: SAXSource = SAXSource(xmlReader, project)
        return ss
    }

    @Throws(JAXBException::class)
    fun save(plist: Plist?, projectFile: String) {
        save(plist, File(projectFile))
    }

    @Throws(JAXBException::class)
    fun save(plist: Plist?, projectFile: File) {
        try {
            save(plist, FileWriter(projectFile))
        } catch (ex: IOException) {
            throw JAXBException(ex)
        }
    }

    @Throws(JAXBException::class)
    fun save(plist: Plist?, projectFile: Writer) {
        marshallPlist(plist, projectFile)
    }

    @Throws(JAXBException::class)
    private fun marshallPlist(plist: Plist?, projectFile: Writer) {
        val ctx: JAXBContext = JAXBContext.newInstance(JAXBPlist::class.java)
        val marshaller: Marshaller = ctx.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        try {
            marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", xmlHeaders)
        } catch (ex: PropertyException) {
            marshaller.setProperty("com.sun.xml.bind.xmlHeaders", xmlHeaders)
        }
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true)
        marshaller.marshal(plist, projectFile)
    }

    @Throws(IOException::class)
    fun convert(projectFile: String, destinationProjectFile: String) {
        convert(File(projectFile), File(destinationProjectFile))
    }

    @Throws(IOException::class)
    fun convert(projectFile: File, destinationProjectFile: File) {
        if (!SystemUtils.IS_OS_MAC_OSX) {
            throw UnsupportedOperationException(
                "The pbxproj file conversion can only be performed on a Mac OS X " +
                        "operating system as the Mac OS X specific tool 'plutil' gets called."
            )
        }
        val exec: Process = Runtime.getRuntime().exec(
            arrayOf(
                "plutil", "-convert", "xml1", "-o", destinationProjectFile.getAbsolutePath(),
                projectFile.getAbsolutePath()
            )
        )
        try {
            exec.waitFor()
        } catch (e: InterruptedException) {
        }

        if (exec.exitValue() != 0) {
            throw RuntimeException("Could not convert file (Exit Code: " + exec.exitValue() + ")")
        }
    }

    companion object {
        private const val xmlHeaders: String =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">"
    }
}
