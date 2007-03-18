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

CD ..\main
CALL bootstrap
POPD
IF ERRORLEVEL 1 GOTO :exit

REM
REM execute the build of the runtime systems using the bootstrap artifacts
REM

CD ..\main
CALL build clean install -decimal
POPD
IF ERRORLEVEL 1 GOTO :exit

REM
REM build the tutorials using the resources established from the general system build
REM

CD ..\tutorials
CALL build clean install
POPD

REM
REM build the documentation
REM

CD ..\central
CALL build clean install -decimal
POPD

GOTO :EOF

:exit
ECHO RELEASE PRODUCTION FAILED
GOTO :EOF

