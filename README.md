# CUBA Addon Gradle Plugin

[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

CUBA Addon Gradle Plugin is optional gradle plugin which provides configured checkstyle, javadoc and spotbugs plugins

## Build and install

In order to build the project from source, you need to install the Java 8 Development Kit (JDK).

Open terminal and run the following command to build and install the plugin into your local Maven repository (`~/.m2`):
```
    gradlew install    
```

## Development

There is no any prerequisites so just import the project as Gradle project and start working.

## Debugging

Export `GRADLE_OPTS` environment variable:
```
   export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

Set up `Remote` debug configuration for port 5005 in Intellij Idea.

Stop existing Gradle daemon:
```
   gradlew --stop
```

Start build of the test project with `--no-daemon` option.