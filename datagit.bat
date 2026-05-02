@echo off

set SCRIPT_DIR=%~dp0
set JAR_PATH=%SCRIPT_DIR%build\libs\datagit.jar

if not exist "%JAR_PATH%" (
  echo ERROR datagit.jar not found. Run "gradlew build" first.
  exit /b 1
)

java -jar "%JAR_PATH%" %*