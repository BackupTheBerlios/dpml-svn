@echo off

REM build.bat
REM ---------
REM
REM Utility script used when building a release.  The script is typically used
REM as follows:
REM
REM $ build all 1234
REM
REM where the first argument is product group and the second (optional) argument is the
REM build revision identifier. Allowable product group names include:
REM
REM  all      builds transit, util, magic, metro, depot and test
REM  transit  build transit and util
REM  magic    build magic
REM  tools    build tools
REM  depot    build depot
REM  metro    build metro
REM  test     build test
REM  ci       builds luntbuild-clean, transit, magic, depot, metro, test

set ID=%2
set TARGET=%1
IF "%TARGET%" == "help" goto help

IF "%TARGET%" == "" set TARGET=all
IF "%TARGET%" == "transit" CALL :transit
IF "%TARGET%" == "util" CALL :util
IF "%TARGET%" == "magic" CALL :magic
IF "%TARGET%" == "tools" CALL :tools
IF "%TARGET%" == "depot" CALL :depot
IF "%TARGET%" == "metro" CALL :metro
IF "%TARGET%" == "test" CALL :test
IF "%TARGET%" == "report" CALL :report
IF "%TARGET%" == "all" CALL :all
goto end

:all
CALL :transit
IF ERRORLEVEL 1 goto fail
CALL :magic
IF ERRORLEVEL 1 goto fail
CALL :tools
IF ERRORLEVEL 1 goto fail
CALL :util
IF ERRORLEVEL 1 goto fail
CALL :metro
if ERRORLEVEL 1 goto fail
CALL :depot
if ERRORLEVEL 1 goto fail
CALL :test
if ERRORLEVEL 1 goto fail
CALL :report
GOTO :EOF

:fail
set ID=""
GOTO :EOF

:end
set ID=""
GOTO :EOF

:transit
PUSHD %HOMEDRIVE%%HOMEPATH%
del/q .ant\lib\dpml-*.jar
POPD
PUSHD transit
:transit_again
CALL :build clean install
IF ERRORLEVEL 99 goto transit_again
POPD
GOTO :EOF

:magic
PUSHD magic
CALL :build clean install
POPD
GOTO :EOF

:util
PUSHD util
CALL :build clean install
POPD
GOTO :EOF

:tools
PUSHD tools
CALL :build clean install
POPD
GOTO :EOF

:depot
PUSHD depot
CALL :build clean install
POPD
GOTO :EOF

:metro
PUSHD metro
CALL :build clean install
POPD
GOTO :EOF

:test
PUSHD test
CALL :build clean install
POPD
GOTO :EOF

:report
CALL :build junit-report
GOTO :EOF

:build
IF not "%ID%" == "" set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%ID%]
CALL ant %BUILD_ID% %*
set BUILD_ID=""
goto :EOF

:help
echo Build one or more of the DPML products:
echo   build [product] [revision]
echo     [product] product name
echo       transit   -- build the transit distribution
echo       magic     -- build the magic distribution
echo       tools     -- build the tools distribution
echo       util      -- build the util distribution
echo       metro     -- build the metro distribution
echo       depot     -- build the depot distribution
echo       test      -- build and execute integration tests
echo       all       -- builds transit, magic, tools, util, depot, and metro distributions and executes integration tests
echo     [revision] optional build revision identified used when
echo                building number releases.
goto :EOF
