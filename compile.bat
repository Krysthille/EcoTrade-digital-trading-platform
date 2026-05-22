@echo off
echo Compiling EcoTrade Java files...

REM Set classpath for MySQL connector
set CLASSPATH=lib/mysql-connector-j-9.2.0.jar

REM Compile all .java files in com/ecotrade
javac -d bin -cp "%CLASSPATH%" src/com/ecotrade/*.java

IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Compilation failed. Check the errors above.
) ELSE (
    echo.
    echo ✅ Compilation successful! Class files are in the /bin folder.
)

pause
