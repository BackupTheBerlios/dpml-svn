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
REM  all      builds main, planet, and central
REM  main     build transit, util, magic, metro and depot
REM  planet   build planet components
REM  central  build site

set TARGET=%1
set ID=%2
IF "%TARGET%" == "" set TARGET=all
IF "%TARGET%" == "help" goto help
IF "%TARGET%" == "main" CALL :main
IF "%TARGET%" == "planet" CALL :planet
IF "%TARGET%" == "central" CALL :central
IF "%TARGET%" == "all" CALL :all
goto end

:all
CALL :main
IF ERRORLEVEL 1 goto fail
CALL :planet
IF ERRORLEVEL 1 goto fail
CALL :central
IF ERRORLEVEL 1 goto fail
GOTO :EOF

:fail
set ID=""
GOTO :EOF

:end
set ID=""
GOTO :EOF

:main
PUSHD main
set ORIGINAL_ID=%ID%
IF not "%ID%" == "" CALL main all %ID%
IF "%ID%" == "" CALL main all
set ID=%ORIGINAL_ID%
POPD
GOTO :EOF

:planet
PUSHD planet
CALL :build clean install
POPD
GOTO :EOF

:central
PUSHD central
CALL :build clean install
POPD
GOTO :EOF

:build
set BUILD_ID=""
IF not "%ID%" == "" set BUILD_ID=-Dbuild.signature=%ID%
ECHO building project with release ID [%ID%]
CALL ant %BUILD_ID% %*
set BUILD_ID=""
goto :EOF

:help
echo Build one or more of the DPML products:
echo   build [product] [revision]
echo     [product] product name
echo       all       -- builds main, planet and central.
echo       main      -- build the main distribution
echo       planet    -- build the planet distribution
echo       central   -- build the central site
goto :EOF
