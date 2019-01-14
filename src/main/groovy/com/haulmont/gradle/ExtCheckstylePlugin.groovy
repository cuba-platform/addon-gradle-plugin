package com.haulmont.gradle

import com.google.common.io.Files
import org.gradle.api.Plugin
import org.gradle.api.Project

class ExtCheckstylePlugin implements Plugin<Project> {
    void apply(Project p) {
        p.apply plugin: 'checkstyle'
        extend(p)
    }

    def extend(Project project) {
        def checkstyle = project.extensions.findByName('checkstyle')
        String checkstyleConfigDir = "${project.rootProject.projectDir}/config/checkstyle"
        def file = new File(checkstyleConfigDir + "/checkstyle.xml")

        if (!file.exists()) {
            new File(checkstyleConfigDir).mkdirs()
            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkstyle/addon-checkstyle.xml")
            Files.write(stream.getBytes(), file)
        }

        def ignoreFile = new File(checkstyleConfigDir + "/checkstyle-ignore.xml")

        if (!ignoreFile.exists()) {
            new File(checkstyleConfigDir).mkdirs()
            InputStream stream = getClass().getClassLoader().getResourceAsStream("checkstyle/addon-checkstyle-ignore.xml")
            Files.write(stream.getBytes(), ignoreFile)
        }

        checkstyle.toolVersion = "6.2"
        checkstyle.configFile = file
        checkstyle.ignoreFailures = false

    }

}
