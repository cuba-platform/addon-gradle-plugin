package com.haulmont.gradle

import com.google.common.io.Files
import groovy.io.FileType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ExtSpotbugsPlugin implements Plugin<Project> {
    void apply(Project p) {
        if (javaSourcesExists(p)) {
            p.apply plugin: 'com.github.spotbugs'
            extend(p)
        }
    }

    def extend(Project project) {
        def spotbugs = project.extensions.findByName('spotbugs')
        spotbugs.toolVersion = "3.1.12"
        spotbugs.ignoreFailures = false
        spotbugs.omitVisitors = ['FindDoubleCheck']
        spotbugs.effort = "max"
        spotbugs.reportLevel = "medium"

        String spotbugsConfigDir = "${project.rootProject.projectDir}/config/codestyle"

        def excludeFilter = new File(spotbugsConfigDir + "/exclude-filter.xml")
        if (!excludeFilter.exists()) {
            new File(spotbugsConfigDir).mkdirs()
            InputStream stream = getClass().getClassLoader().getResourceAsStream("codestyle/exclude-filter.xml")
            Files.write(stream.getBytes(), excludeFilter)
        }

        spotbugs.excludeFilter = excludeFilter

        def fancyHist = new File(spotbugsConfigDir + "/fancy-hist.xsl")
        if (!fancyHist.exists()) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("codestyle/fancy-hist.xsl")
            Files.write(stream.getBytes(), fancyHist)
        }

        Task spotbugsMain = project.tasks.findByName("spotbugsMain")

        spotbugsMain.jvmArgs = ['-Xmx2048m']
        def spotBugsReport = spotbugsMain.reports
        spotBugsReport.xml.enabled = false
        def htmlReport = spotBugsReport.html
        htmlReport.enabled = true
        htmlReport.stylesheet = project.resources.text.fromFile(fancyHist)
        htmlReport.destination = new File("${project.buildDir}/reports/spotbugs/${project.name}.html")
    }

    static boolean javaSourcesExists(Project project) {
        def list = []

        def dir = new File("${project.projectDir}/src")
        dir.eachFileRecurse(FileType.FILES) { file ->
            if (file.getName().endsWith(".java")) {
                list << file
            }
        }
        !list.isEmpty()
    }
}
