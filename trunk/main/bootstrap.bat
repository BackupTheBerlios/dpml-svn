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
CALL :transit-main
CALL :transit-tools
CALL :tools-library
CALL :tools-ant
CALL :depot-console
GOTO :EOF

:antlib-cleanup
PUSHD %HOMEDRIVE%%HOMEPATH%
del/q .ant\lib\dpml-*.jar
POPD
GOTO :EOF

:transit-main
PUSHD transit\core\handler
CALL :build clean install
POPD
GOTO :EOF

:transit-tools
PUSHD transit\core\tools
CALL :build clean install
POPD
GOTO :EOF

:tools-library
PUSHD tools\library
CALL :build clean install
POPD
GOTO :EOF

:tools-ant
PUSHD tools\ant
CALL :build clean install
POPD
GOTO :EOF

:depot-console
PUSHD depot\console
CALL :build clean bootstrap-install
POPD
GOTO :EOF

:build
IF "%ID%" == "" set ID=SNAPSHOT
set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%BUILD_ID%]
CALL ant %BUILD_ID% %*
set BUILD_ID=""
goto :EOF

:install
IF "%ID%" == "" set ID=SNAPSHOT
set BUILD_ID=-Dbuild.signature=%ID%
ECHO installing project with release ID [%BUILD_ID%]
PUSHD depot\console
CALL ant -f install.xml %BUILD_ID% %*
POPD
set BUILD_ID=""
GOTO :EOF


