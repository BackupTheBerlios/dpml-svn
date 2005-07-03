@echo off

REM build.bat
REM ---------
REM
REM Utility script used when building a release.  The script is typically used
REM as follows:
REM
REM $ build 1234
REM
REM

set ID=%1

:build
IF not "%ID%" == "" set BUILD_ID=-Ddpml.release.signature=%ID%
ECHO building project with release ID [%ID%]
CALL ant %BUILD_ID% 
set BUILD_ID=""
goto :EOF

