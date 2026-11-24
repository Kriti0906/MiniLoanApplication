@echo off
chcp 65001 >nul
echo ========================================================
echo  Mini Loan Origination Application - Build ^& Run
echo ========================================================

REM Navigate to project root
cd /d "%~dp0"

REM 1. Create output folder
if not exist "bin" mkdir bin

REM 2. Compile all Java sources
echo Compiling Java source files...
javac -encoding UTF-8 -d bin src\com\newgen\loan\model\*.java src\com\newgen\loan\db\*.java src\com\newgen\loan\dao\*.java src\com\newgen\loan\service\*.java src\com\newgen\loan\reports\*.java src\com\newgen\loan\utility\*.java src\com\newgen\loan\console\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

REM 3. Run application with MySQL driver if present in lib/
echo.
echo Launching application...
if exist "lib\*.jar" (
    java -cp "bin;lib/*" com.newgen.loan.console.MainMenu
) else (
    java -cp "bin" com.newgen.loan.console.MainMenu
)

pause
