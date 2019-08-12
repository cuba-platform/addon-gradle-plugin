package com.haulmont.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.artifacts.dependencies.DefaultClientModule
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency
import org.gradle.api.internal.file.collections.ImmutableFileCollection

public class DbClassResolverExtension {

    String hsql = "org.hsqldb:hsqldb:2.4.1";
    String postgresql = "org.postgresql:postgresql:42.2.5";
    String maria = "org.mariadb.jdbc:mariadb-java-client:2.4.1";
    String mysql = "mysql:mysql-connector-java:5.1.46";
    String mssql2005 = "net.sourceforge.jtds:jtds:1.3.1";
    String mssql = "com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8";

    Dependency findProjectDBDependency(Project project, String dbms, String dbmsVersion, String driver) {
        switch (dbms) {
            case "hsql":
                return getDependencyFromCoordinates(hsql)
            case "mysql":
                if (driver != null && driver.equals("org.mariadb.jdbc.Driver")) {
                    return getDependencyFromCoordinates(maria)
                } else {
                    return getDependencyFromCoordinates(mysql)
                }
            case "postgres":
                return getDependencyFromCoordinates(postgresql)
            case "mssql":
                switch (dbmsVersion) {
                    case "2005":
                        return getDependencyFromCoordinates(mssql2005)
                    default:
                        return getDependencyFromCoordinates(mssql)
                }
                break
            case "oracle":
                def driverClasspath = project.property("test.db.driverClasspath")
                return new DefaultSelfResolvingDependency(ImmutableFileCollection.of(new File(driverClasspath)))
        }
        return null
    }

    Dependency getDependencyFromCoordinates(String coordinates) {
        String[] splitted = coordinates.split(":")
        return new DefaultClientModule(splitted[0], splitted[1], splitted[2])
    }
}
