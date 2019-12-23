package com.haulmont.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.JavadocMemberLevel

class ExtJavaDocPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.logger.info("[AddonPlugin] applying javadoc to project $project.name")
        project.afterEvaluate { Project p ->
            if (p == project.rootProject) {
                p.subprojects { Project sp ->
                    if (!project.getTasks().findByName("javadoc")) {
                        project.task("javadoc")
                    }
                }
                if (!p.getTasks().findByName("aggregateJavadoc")) {
                    def javadoc = p.task([type       : Javadoc,
                                          description: 'Generate javadocs from all child projects as if it was a single project',
                                          group      : 'Documentation'], "aggregateJavadoc")
                    configureJavaDoc(p, javadoc)
                }
            }
        }
    }

    private static void configureJavaDoc(Project project, Task javadoc) {
        javadoc.destinationDir = new File("$project.buildDir/docs/javadoc")
        javadoc.title = "${project.name.toUpperCase()} API"

        def options = javadoc.options

        options.encoding = 'UTF-8'
        options.addStringOption("sourcepath", "")
        options.memberLevel = JavadocMemberLevel.PROTECTED

        project.subprojects.each { proj ->
            def javadocTask = proj.tasks.getByPath('javadoc')

            if (javadocTask.enabled) {
                javadoc.source += javadocTask.source
                javadoc.classpath += javadocTask.classpath
            }
        }
    }
}
