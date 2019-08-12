package com.haulmont.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class AddonPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.info("[AddonPlugin] applying to project $project.name")

        if (project != project.rootProject) {
            if (!project.plugins.findPlugin("checkstyle")) {
                new ExtCheckstylePlugin().apply(project)
            }
//            if (!project.plugins.findPlugin("findbugs")) {
//                new ExtFindbugsPlugin().apply(project)
//            }
            if (!project.plugins.findPlugin("com.github.spotbugs")) {
                new ExtSpotbugsPlugin().apply(project)
            }
        }

        new ExtDbTasksPlugin().apply(project)
        new ExtTestDbConfigurationPlugin().apply(project)
        new ExtJavaDocPlugin().apply(project)
    }
}
