package com.haulmont.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ExtFindbugsPlugin implements Plugin<Project> {
    void apply(Project p) {
        p.apply plugin: 'findbugs'
        extend(p)
    }

    def extend(Project project) {
        def findbugs = project.extensions.findByName('findbugs')
        findbugs.toolVersion = "3.0.1"
        findbugs.ignoreFailures = true

        Task findbugsMain = project.tasks.findByName("findbugsMain")
        project.task('checkFindBugsReport') {
            doLast {
                def xmlReport = findbugsMain.reports.xml
                if (!xmlReport.destination.exists()) return
                def slurped = new XmlSlurper().parse(xmlReport.destination)
                def report = ""
                slurped['BugInstance'].eachWithIndex { bug, index ->
                    report += "\n${index + 1}. Found bug risk ${bug.@'type'} of category ${bug.@'category'} "
                    report += "in the following places"
                    bug.Class.SourceLine.each { place ->
                        report += "\n       ${place.@'classname'} at lines ${place.@'start'}:${place.@'end'}"
                    }
                }
                if (report.length() > 1) {
                    throw new GradleException( "[FINDBUGS]\n ${report}")
                }
            }
        }

        findbugsMain.finalizedBy 'checkFindBugsReport'
    }

}
