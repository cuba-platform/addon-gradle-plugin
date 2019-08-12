package com.haulmont.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency

class ExtTestDbConfigurationPlugin implements Plugin<Project> {

    static def DB_LIB_GROUPS = ["org.hsqldb"             : "hsqldb",
                                "org.mariadb.jdbc"       : "mariadb-java-client",
                                "mysql"                  : "mysql-connector-java",
                                "org.postgresql"         : "postgresql",
                                "net.sourceforge.jtds"   : "jtds",
                                "com.microsoft.sqlserver": "mssql-jdbc"]

    @Override
    void apply(Project project) {
        project.logger.info("[AddonPlugin] applying jdbc to project $project.name")

        if (project.hasProperty("testMode")) {
            project.beforeEvaluate { Project p ->
                if (project != project.rootProject) {
                    if (project.name.endsWith("-core")) {
                        applyJdbcConfiguration(project)
                        applyJvmArgs(project)
                    }
                }
            }
        }
    }

    static def applyJvmArgs(Project project) {
        Task test = project.getTasks().findByName("test")
        copyPropertyToSystem(test, project, "test.db.driverClassName")
        copyPropertyToSystem(test, project, "test.db.username")
        copyPropertyToSystem(test, project, "test.db.url")
        copyPropertyToSystem(test, project, "test.db.password")
        copyPropertyToSystem(test, project, "test.db.dbmsType")
    }

    private static void copyPropertyToSystem(Task test, Project project, String name) {
        test.systemProperties.put(name, project.property(name))
    }

    static def applyJdbcConfiguration(Project project) {
        def extension = project.extensions.create('dbResolver', DbClassResolverExtension)
        excludeDefaultDbArtifact(project, extension)
        addDbArtifact(project, extension)
    }

    static def addDbArtifact(Project project, DbClassResolverExtension extension) {
        def dbms = project.hasProperty("test.db.dbms") ? project.property("test.db.dbms") : "hsql"
        def driver = project.hasProperty("test.db.driverClassName")?project.property("test.db.driverClassName"):null
        def dbmsVersion = project.hasProperty("test.db.dbmsVersion")?project.property("test.db.dbmsVersion"):null

        Dependency dependency = extension.findProjectDBDependency(project, dbms, dbmsVersion, driver)
        def jdbcDependencies = project.getConfigurations().getByName("jdbc").dependencies
        def testRuntimeDependencies = project.getConfigurations().getByName("testRuntime").dependencies

        jdbcDependencies.add(dependency);
        testRuntimeDependencies.add(dependency);

    }

    static def excludeDefaultDbArtifact(Project project, DbClassResolverExtension extension) {
        def createDb = project.tasks.findByName("createDb")
        def dbms = createDb.dbms != null ? createDb.dbms : "hsql"
        def dbmsVersion = createDb.dbmsVersion
        def driver = createDb.driver

        Dependency dependency = extension.findProjectDBDependency(project, dbms, dbmsVersion, driver)
        def jdbcDependencies = project.getConfigurations().getByName("jdbc").dependencies
        def testRuntimeDependencies = project.getConfigurations().getByName("testRuntime").dependencies

        jdbcDependencies.remove(jdbcDependencies.find { it ->
            return it.group == dependency.group && it.name == dependency.name
        })

        testRuntimeDependencies.remove(testRuntimeDependencies.find { it ->
            return it.group == dependency.group && it.name == dependency.name
        })
    }
}
