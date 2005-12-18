@echo off

REM bootstrap.bat
REM -------------
REM
REM Utility to execute the bootsrap build.  Bootstrapping involves the creation of 
REM the transit main protocol handler, transit antlib, the tools library and 
REM ant plugin, and the console CLI handler.  Following the bootstrap build, the 
REM procedure invokes the installation of resources into ${dpml.home) using the 
REM depot/console./install.xml ant build.
REM

set ID=%1
CALL :antlib-cleanup
IF ERRORLEVEL 1 GOTO :exit
CALL :transit-main
IF ERRORLEVEL 1 GOTO :exit
CALL :transit-tools
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-library
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-ant-builder
IF ERRORLEVEL 1 GOTO :exit
CALL :external-modules
IF ERRORLEVEL 1 GOTO :exit
CALL :util-cli
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-library-console
IF ERRORLEVEL 1 GOTO :exit
CALL :depot-core-console
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

:transit-main
PUSHD transit\core
CALL :build clean install
POPD
GOTO :EOF

:transit-tools
PUSHD transit\tools
CALL :build clean install
POPD
GOTO :EOF

:depot-library
PUSHD depot\library\common
CALL :build clean install
POPD
GOTO :EOF

:depot-ant-builder
PUSHD depot\tools\builder
CALL :build clean install
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

:depot-library-console
PUSHD depot\library\console
CALL :build clean install
POPD
GOTO :EOF

:depot-core-console
PUSHD depot\core\console
CALL :build clean bootstrap
POPD
GOTO :EOF

:build
IF "%ID%" == "" set ID=SNAPSHOT
set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%BUILD_ID%]
CALL ant %BUILD_ID% %*
set BUILD_ID=""
goto :EOF


