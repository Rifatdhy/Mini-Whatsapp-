@echo off
setlocal
set "JAVAC=javac"
where javac >nul 2>nul
if errorlevel 1 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\javac.exe" (
        set "JAVAC=C:\Program Files\Apache NetBeans\jdk\bin\javac.exe"
    )
)

if not exist out mkdir out
"%JAVAC%" -encoding UTF-8 -d out src\com\jgu\chat\*.java
echo.
echo Compile selesai. Jalankan server dengan run-server.bat dan client dengan run-client.bat
