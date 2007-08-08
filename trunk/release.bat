@echo off

REM rebuild.bat
REM -------------
REM
REM Utility to execute the bootstrap and main build sequence
REM
REM Usage:
REM ------
REM
REM rebuild
REM


REM
REM execute the bootstrap build procedure
REM

PUSHD main
CALL bootstrap
POPD
CD
IF NOT ERRORLEVEL 0 GOTO :exit

REM
REM execute the build of the runtime systems using the bootstrap artifacts
REM

PUSHD main
CALL build clean install -decimal
POPD
CD
IF NOT ERRORLEVEL 0 GOTO :exit

REM
REM build the tutorials using the resources established from the general system build
REM

PUSHD tutorials
CALL build clean install
POPD
CD
IF NOT ERRORLEVEL 0 GOTO :exit

REM
REM build the documentation
REM

PUSHD central
CALL build clean install -decimal
POPD
CD
IF NOT ERRORLEVEL 0 GOTO :exit

GOTO :EOF

:exit
ECHO RELEASE PRODUCTION FAILED
GOTO :EOF

