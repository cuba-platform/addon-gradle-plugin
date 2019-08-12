package com.haulmont.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class ExtDbTasksPlugin implements Plugin<Project> {

    protected static final Integer DEFAULT_DB_PORT = 9010

    @Override
    void apply(Project project) {

        project.logger.info("[AddonPlugin] applying db tasks to project $project.name")

        project.afterEvaluate { Project p ->
            if (project != project.rootProject) {
                if (project.name.endsWith("-core")) {
                    configureDbTasks(project)
                }
            }
        }
    }

    private void configureDbTasks(Project project) {
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
        if (project.hasProperty("testMode")) {
            if (!project.getTasks().findByName("createTestDb")) {
                def assembleDbScripts = project.getTasks().findByName("assembleDbScripts");
                def createTestDb = project.task([dependsOn: assembleDbScripts, description: 'Creates local test database', type: Class.forName('CubaDbCreation')], "createTestDb")
                fillDbTaskValues(project, createTestDb)
            }
            if (!project.getTasks().findByName("updateTestDb")) {
                def assembleDbScripts = project.getTasks().findByName("assembleDbScripts");
                def updateTestDb = project.task([dependsOn: assembleDbScripts, description: 'Updates local test database', type: Class.forName('CubaDbUpdate')], "updateTestDb")
                fillDbTaskValues(project, updateTestDb)
            }
        }
    }

    private void fillDbTaskValues(Project project, Task dbTask) {
        def createDb = project.tasks.findByName("createDb")

        def dbms = project.hasProperty("test.db.dbms") ? project.property("test.db.dbms") : "hsql"

        dbTask.dbms = dbms
        dbTask.host = project.hasProperty("test.db.host") ? project.property("test.db.host") : createDb.host
        dbTask.dbName = project.hasProperty("test.db.dbname") ? project.property("test.db.dbname") : createDb.dbName
        dbTask.dbUser = project.hasProperty("test.db.username") ? project.property("test.db.username") : createDb.dbUser
        dbTask.dbPassword = project.hasProperty("test.db.password") ? project.property("test.db.password") : createDb.dbPassword

        if (dbms.equals("oracle")) {
            dbTask.oracleSystemUser = project.property("test.db.oracleSystemUser")
            dbTask.oracleSystemPassword = project.property("test.db.oracleSystemPassword")
        }

        if (dbms.equals("mssql")) {
            dbTask.dbmsVersion = project.property("test.db.dbmsVersion")
        }

        if (dbms.equals("mysql")) {
            dbTask.connectionParams = project.property("test.db.connectionParams")
            if (project.hasProperty("test.db.driver")) {
                def driver = project.property("test.db.url")
                dbTask.driver = driver
                if (driver.equals("org.mariadb.jdbc.Driver")) {
                    dbTask.masterUrl = project.property("test.db.masterUrl")
                    dbTask.dbUrl = project.property("test.db.url")
                }
            }
        }
    }
}
