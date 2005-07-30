@echo off

rem -------------------------- DEPOT CLI ---------------------------------------------------

setlocal

rem set JAVA CMD
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java

goto SET_DEPOT

:USE_JAVA_HOME
set JAVA="%JAVA_HOME%\bin\java"

:SET_DEPOT
if "%DPML_HOME%" == "" set DPML_HOME=%APPDATA%\DPML
if "%DPML_SYSTEM%" == "" set DPML_SYSTEM=%DPML_HOME%\Shared

set $DPML_BOOT=%DPML_HOME%\Data\boot\%RANDOM%
mkdir %$DPML_BOOT%
copy/B/Y %DPML_HOME%\Data\lib %$DPML_BOOT% > %DPML_HOME%\Data\logs\boot.log

set DPML_DEPOT_JAR=%$DPML_BOOT%\@DEPOT-PATH@
set DPML_TRANSIT_JAR=%$DPML_BOOT%\@TRANSIT-PATH@
set $DEPOT_CLASSPATH="%DPML_DEPOT_JAR%";"%DPML_TRANSIT_JAR%"
set $POLICY=-Djava.security.policy="%DPML_SYSTEM%\bin\security.policy"
set $ARGS=%*

:RUN_DEPOT
%JAVA% -Djava.system.class.loader=@DEPOT-CLASSLOADER-CLASS@ %$POLICY% %DEPOT_JVM_OPTS% -classpath %$DEPOT_CLASSPATH% @DEPOT-MAIN-CLASS@ %DEPOT_ARGS% %$ARGS%
goto EndOfScript
:EndOfScript

RMDIR/S/Q %$DPML_BOOT%

endlocal

rem 