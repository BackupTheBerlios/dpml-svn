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
if "%DPML_DEPOT_JAR%" == "" set DPML_DEPOT_JAR=%DPML_HOME%\Data\lib\@DEPOT-PATH@
if "%DPML_TRANSIT_JAR%" == "" set DPML_TRANSIT_JAR=%DPML_HOME%\Data\lib\@TRANSIT-PATH@
 
set $DEPOT_CLASSPATH="%DPML_DEPOT_JAR%";"%DPML_TRANSIT_JAR%"
set $POLICY=-Djava.security.policy="%DPML_SYSTEM%\bin\security.policy"
set $ARGS=%*

:RUN_DEPOT
%JAVA% -Djava.system.class.loader=net.dpml.depot.lang.DepotClassLoader %$POLICY% %DEPOT_JVM_OPTS% -classpath %$DEPOT_CLASSPATH% @DEPOT-MAIN-CLASS@ %DEPOT_ARGS% %$ARGS%
goto EndOfScript
:EndOfScript

endlocal

rem 