@echo off

REM bootstrap.bat
REM -------------
REM
REM Utility to execute the bootsrap build.  Bootstrapping involves the creation of 
REM the transit main protocol handler, transit antlib, the tools index and 
REM ant plugin, and the console CLI handler.
REM
REM Usage:
REM ------
REM
REM bootstrap
REM


set ARG_ONE=%1
CALL :antlib-cleanup
rem IF ERRORLEVEL 1 GOTO :exit
CALL :transit-main
IF NOT ERRORLEVEL 0 GOTO :exit
CALL :dpml-metro-part
IF NOT ERRORLEVEL 0 GOTO :exit
CALL :dpml-depot-library
IF NOT ERRORLEVEL 0 GOTO :exit
CALL :dpml-util-cli
IF NOT ERRORLEVEL 0 GOTO :exit
CALL :dpml-depot-build
IF NOT ERRORLEVEL 0 GOTO :exit
CALL :dpml-depot-builder
IF NOT ERRORLEVEL 0 GOTO :exit

rem
rem Depot build tool is now available.
rem Proceed with the creation of the external modules using Depot build exe.
rem

CALL :external-modules
IF ERRORLEVEL 0 ( ECHO BOOTSTRAP SUCCESSFUL [%ERRORLEVEL%] ) ELSE ( ECHO BOOTSTRAP FAILED [%ERRORLEVEL%] )

rem
rem Bootstrap build is not complete and external module definitions are in the cache.
rem We can now processed with a regular build.
rem

GOTO :EOF


:exit
IF NOT ERRORLEVEL 0 ECHO BOOTSTRAP FAILED
GOTO :EOF

:antlib-cleanup
PUSHD %HOMEDRIVE%%HOMEPATH%
del/q .ant\lib\dpml-*.jar
POPD
GOTO :EOF

:dpml-metro-part
PUSHD metro\part
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:transit-main
PUSHD transit\core
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-depot-library
PUSHD depot\library
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-depot-builder
PUSHD depot\tools
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-util-cli
PUSHD util\cli
rem CALL :build clean install
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-depot-build
PUSHD depot\build
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:external-modules
PUSHD external
CALL build clean install
POPD
GOTO :EOF

:build

@echo on
CD
@echo off
CALL ant -Dbuild.signature=BOOTSTRAP %*
goto :EOF


