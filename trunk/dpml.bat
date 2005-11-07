@echo off

set ID=%1
IF "%ID%" == "" set ID=SNAPSHOT

PUSHD main
call bootstrap %ID%
IF ERRORLEVEL 1 goto cleanup
call build clean install -version %ID%
IF ERRORLEVEL 1 goto cleanup
POPD
PUSHD planet
call build clean install -version %ID%
IF ERRORLEVEL 1 goto cleanup
POPD
GOTO :EOF

:cleanup
POPD
set ID=""
GOTO :EOF

