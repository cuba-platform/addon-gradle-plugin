package com.haulmont.gradle

import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.sql.SQLException

class CheckDBExistsTask extends DefaultTask {

    public static final String POSTGRES_DBMS = "postgres";
    public static final String MSSQL_DBMS = "mssql";
    public static final String ORACLE_DBMS = "oracle";
    public static final String MYSQL_DBMS = "mysql";
    public static final String HSQL_DBMS = "hsql";

    protected static final String MS_SQL_2005 = "2005";

    Task createDBTask

    def dbUrl
    def driverClasspath
    def driver

    @TaskAction
    def void createDbIfNotExists() {
        init()
        try {
            Sql sql = getSql();
            project.logger.info("[CheckDBExistsTask] check that DB exists for $project.name")
            sql.rows("select count(*) from SYS_DB_CHANGELOG");
            project.logger.info("[CheckDBExistsTask] DB exists for $project.name")
        } catch (SQLException e) {
            project.logger.info("[CheckDBExistsTask] DB not exists for $project.name. New DB will be created")
            createDBTask.createDb();
        }
        finally {
            sql.close()
        }
    }

    protected Sql getSql() throws SQLException{
        try {
            return Sql.newInstance(dbUrl, createDBTask.dbUser, createDBTask.dbPassword, driver);
        } catch (ClassNotFoundException e) {
            throw new GradleException("Driver class $driver not found", e);
        }
    }

    protected void init() {
        if (StringUtils.isBlank(driver) || StringUtils.isBlank(dbUrl)) {
            def dbms = createDBTask.dbms
            def host = createDBTask.host
            def dbName = createDBTask.dbName
            def connectionParams = createDBTask.connectionParams
            if (POSTGRES_DBMS.equals(dbms)) {
                driver = "org.postgresql.Driver";
                dbUrl = "jdbc:postgresql://" + host + "/" + dbName + connectionParams;
            } else if (MSSQL_DBMS.equals(dbms)) {
                if (MS_SQL_2005.equals(createDBTask.dbmsVersion)) {
                    driver = "net.sourceforge.jtds.jdbc.Driver";
                    dbUrl = "jdbc:jtds:sqlserver://" + host + "/" + dbName + connectionParams;
                } else {
                    driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    dbUrl = "jdbc:sqlserver://" + host + ";databaseName=" + dbName + connectionParams;
                }
            } else if (ORACLE_DBMS.equals(dbms)) {
                driver = "oracle.jdbc.OracleDriver";
                dbUrl = "jdbc:oracle:thin:@//" + host + "/" + dbName + connectionParams;
            } else if (HSQL_DBMS.equals(dbms)) {
                driver = "org.hsqldb.jdbc.JDBCDriver";
                dbUrl = "jdbc:hsqldb:hsql://" + host + "/" + dbName + connectionParams;
            } else if (MYSQL_DBMS.equals(dbms)) {
                driver = "com.mysql.jdbc.Driver";
                if (StringUtils.isBlank(connectionParams)) {
                    connectionParams = "?useSSL=false&allowMultiQueries=true&serverTimezone=UTC";
                }
                dbUrl = "jdbc:mysql://" + host + "/" + dbName + connectionParams;
            } else
                throw new UnsupportedOperationException("DBMS " + dbms + " is not supported. " +
                        "You should either provide 'driver' and 'dbUrl' properties, or specify one of supported DBMS in 'dbms' property");
        }
        ClassLoader classLoader = GroovyObject.class.getClassLoader();
        if (StringUtils.isBlank(driverClasspath)) {
            driverClasspath = project.getConfigurations().getByName("jdbc").fileCollection({ dependency -> true }).getAsPath();
            project.getConfigurations().getByName("jdbc").fileCollection({ dependency -> true }).getFiles()
                    .forEach({ file ->
                        try {
                            Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                            addURLMethod.setAccessible(true);
                            addURLMethod.invoke(classLoader, file.toURI().toURL());
                        } catch (NoSuchMethodException | IllegalAccessException
                        | InvocationTargetException | MalformedURLException e) {
                            throw new GradleException("Exception when invoke 'java.net.URLClassLoader.addURL' method", e);
                        }

                    });
        } else {
            StringTokenizer tokenizer = new StringTokenizer(driverClasspath, File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                try {
                    URL url = new File(tokenizer.nextToken()).toURI().toURL();
                    Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addURLMethod.setAccessible(true);
                    addURLMethod.invoke(classLoader, url);
                } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | MalformedURLException e) {
                    throw new GradleException("Exception when invoke 'java.net.URLClassLoader.addURL' method", e);
                }
            }
        }
        project.getLogger().info("[CheckDBExistsTask] driverClasspath: " + driverClasspath);
    }

}
