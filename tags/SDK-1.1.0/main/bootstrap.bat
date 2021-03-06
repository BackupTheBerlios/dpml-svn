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
CALL :xml-setup
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-module
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-part
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-state
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-type
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-component
IF ERRORLEVEL 1 GOTO :exit
CALL :transit-main
IF ERRORLEVEL 1 GOTO :exit
CALL :transit-tools
IF ERRORLEVEL 1 GOTO :exit
CALL :dpml-library
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-ant-builder
IF ERRORLEVEL 1 GOTO :exit
CALL :external-modules
IF ERRORLEVEL 1 GOTO :exit
CALL :util-cli
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-build
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-core
IF ERRORLEVEL 1 GOTO :exit
ECHO BOOTSTRAP SUCCESSFUL

:exit
IF ERRORLEVEL 1 ECHO BOOTSTRAP FAILED
GOTO :EOF

:antlib-cleanup
PUSHD %HOMEDRIVE%%HOMEPATH%
del/q .ant\lib\dpml-*.jar
POPD
GOTO :EOF

:xml-setup
CALL ant -f bootstrap.xml xml
GOTO :EOF

:dpml-module
PUSHD lang\module
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-part
PUSHD lang\part
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-state
PUSHD lang\state
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-component
PUSHD lang\component
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-type
PUSHD lang\type
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:transit-main
PUSHD transit\core
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:transit-tools
PUSHD transit\tools
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:dpml-library
PUSHD depot\library
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:depot-ant-builder
PUSHD depot\tools
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:external-modules
PUSHD external
CALL :build clean install
POPD
GOTO :EOF

:util-cli
PUSHD util\cli
CALL :build clean install
POPD
GOTO :EOF

:depot-build
PUSHD depot\build
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:depot-core
PUSHD depot\core
CALL :build -f bootstrap.xml clean install
POPD
GOTO :EOF

:build

@echo on
CD
@echo off
CALL ant -Dbuild.signature=BOOTSTRAP %*
goto :EOF


