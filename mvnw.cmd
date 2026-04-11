@REM Maven Wrapper script for Windows
@echo off

setlocal

for %%i in ("%~dp0.") do set MAVEN_PROJECTBASEDIR=%%~fi
set MAVEN_WRAPPER_JAR_PATH=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

@REM Download maven-wrapper.jar if not present
if not exist "%MAVEN_WRAPPER_JAR_PATH%" (
    echo Downloading Maven Wrapper from %WRAPPER_URL%
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%MAVEN_WRAPPER_JAR_PATH%'"
)

java "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %MAVEN_OPTS% -classpath "%MAVEN_WRAPPER_JAR_PATH%" org.apache.maven.wrapper.MavenWrapperMain %*
