@echo off
setlocal
set "JAVA=java"
where java >nul 2>nul
if errorlevel 1 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
        set "JAVA=C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
    )
)

"%JAVA%" -cp out com.jgu.chat.ChatServer 5000
