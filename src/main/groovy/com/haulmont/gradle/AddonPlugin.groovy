package com.haulmont.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project

class AddonPlugin implements Plugin<Project> {

    protected static final Integer DEFAULT_DB_PORT = 9001

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

        project.afterEvaluate { Project p ->
            if (project != project.rootProject) {
                if (project.name.endsWith("-core")) {
                    def createDb = project.tasks.findByName("createDb")
                    def dbName = createDb.dbName
                    def testDbName = createDb.dbName + "-test"
                    def hsqlPort = DEFAULT_DB_PORT
                    def host = createDb.host
                    if (host.contains(":")) {
                        hsqlPort = host.substring(host.lastIndexOf(":") + 1) as Integer
                    }
                    if (!project.getTasks().findByName("startDb")) {
                        def startDb = project.task([type: Class.forName('com.haulmont.gradle.task.db.CubaHsqlStart')], "startDb")
                        startDb.dbName = dbName
                        startDb.dbPort = hsqlPort
                    }
                    if (!project.getTasks().findByName("stopDb")) {
                        def stopDb = project.task([type: Class.forName('com.haulmont.gradle.task.db.CubaHsqlStop')], "stopDb")
                        stopDb.dbName = dbName
                        stopDb.dbPort = hsqlPort
                    }
                    if (!project.getTasks().findByName("startTestDb")) {
                        def startTestDb = project.task([type: Class.forName('com.haulmont.gradle.task.db.CubaHsqlStart')], "startTestDb")
                        startTestDb.dbName = testDbName
                        startTestDb.dbPort = hsqlPort
                    }
                    if (!project.getTasks().findByName("stopTestDb")) {
                        def stopTestDb = project.task([type: Class.forName('com.haulmont.gradle.task.db.CubaHsqlStop')], "stopTestDb")
                        stopTestDb.dbName = testDbName
                        stopTestDb.dbPort = hsqlPort
                    }
                    if (!project.getTasks().findByName("killDb")) {
                        def killProcess = project.task([type: KillProcessTask], "killDb")
                        killProcess.port = hsqlPort
                    }
                }
            }
        }
    }
}
