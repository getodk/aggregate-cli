package org.opendatakit.gradle

import org.apache.tools.ant.taskdefs.condition.Os

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Util {
  static void truncate(path) {
    Path p = Paths.get(path.toString())
    Files.delete(p)
    Files.write(p, [], StandardOpenOption.CREATE)
    println("Truncated file at ${path}")
  }

  static def setXmlValue(path, String tag, value) {
    def xml = new XmlParser().parse(path.toString())
    xml[tag][0].setValue(value.toString())
    def printer = new XmlNodePrinter(new PrintWriter(new FileWriter(path.toString())))
    printer.setPreserveWhitespace(true)
    printer.print(xml)
    println("Set ${path}:<${tag}> to \"${value}\"")
  }

  static def setPropertiesValue(path, String key, value) {
    Properties props = new Properties()
    File propsFile = new File(path.toString())
    props.load(propsFile.newDataInputStream())

    props.setProperty(key, value.toString())
    props.store(propsFile.newWriter(), null)
    println("Set ${path}:${key} to \"${value}\"")
  }

  static def execute(cmd) {
    ['bash', '-c', cmd].execute().waitFor()
  }

  static def makeExecutableJar(path, targetPath) {
    execute("echo \"#!/bin/sh\" > ${targetPath}")
    execute("echo \"exec java -jar \\\$0 \"\\\$@\"\" >> ${targetPath}")
    execute("cat ${path} >> ${targetPath}")
    execute("chmod +x ${targetPath}")
  }

  static def zip(path) {
    execute("zip -9 ${path}.zip ${path}")
  }

  static String getValue(obj, key, defaultValue) {
    if (obj.hasProperty(key))
      obj.getProperty(key)
    else
      defaultValue
  }

  static String getVersionName() {
    if (Os.isFamily(Os.FAMILY_WINDOWS))
      "cmd /c git describe --tags --dirty --always".execute().text.trim()
    else
      "git describe --tags --dirty --always".execute().text.trim()
  }
}
