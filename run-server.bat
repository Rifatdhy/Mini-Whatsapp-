@echo off
setlocal
set "JAVA=javaw"
where javaw >nul 2>nul
if errorlevel 1 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\javaw.exe" (
        set "JAVA=C:\Program Files\Apache NetBeans\jdk\bin\javaw.exe"
    ) else (
        set "JAVA=java"
    )
)

start "" "%JAVA%" -cp out com.jgu.chat.ChatServer 5000
