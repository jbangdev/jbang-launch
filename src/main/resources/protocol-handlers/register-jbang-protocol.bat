@echo off
REM Register jbang protocol handler for Windows
REM This script should be run with administrator privileges

echo Registering jbang protocol handler...

REM Register the protocol
reg add "HKEY_CLASSES_ROOT\jbang" /ve /d "URL:JBang Protocol" /f
reg add "HKEY_CLASSES_ROOT\jbang" /v "URL Protocol" /d "" /f

REM Set the command to execute
reg add "HKEY_CLASSES_ROOT\jbang\shell\open\command" /ve /d "\"%~dp0jbang-launch.exe\" \"%%1\"" /f

echo jbang protocol handler registered successfully.
echo You can now use jbang:// URLs in your browser and other applications. 