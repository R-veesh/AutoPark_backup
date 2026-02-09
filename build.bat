@echo off
cd /d C:\Users\Veesh\AndroidStudioProjects\AutoPark

REM Set Java path - try the JRE first
set JAVA_HOME=C:\Users\Veesh\.local\share\opencode\bin\kotlin-ls\jre
REM Add java to PATH
set PATH=%JAVA_HOME%\bin;%PATH%

REM Test if java works
echo Testing Java...
java -version

REM Run Gradle build
echo.
echo Building project...
call gradlew.bat assembleDebug

REM Check result
if %ERRORLEVEL% EQU 0 (
  echo.
  echo BUILD SUCCESSFUL!
) else (
  echo.
  echo BUILD FAILED with exit code %ERRORLEVEL%
)

REM Keep the window open to see output
pause
