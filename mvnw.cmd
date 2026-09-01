@echo off
set PROJECT_DIR=%~dp0
java -classpath "%PROJECT_DIR%\.mvn\wrapper\maven-wrapper.jar" -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*

