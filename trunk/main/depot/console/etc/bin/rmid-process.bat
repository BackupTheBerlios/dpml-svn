@echo off

rem -------------------------- RMID CLI ---------------------------------------------------
rem   This is an example commandfile that launches the RMID server.  Typically the 
rem   server would be mounted as a service in Windows with an equivalent command line
rem   argument.  It is provided here as it is helpful to run the RMID process locally
rem   when debugging RMI aspects. Two important additions are included in this command
rem   file: firstly the RMI trace property is enabled, and secondly, Transit is appended
rem   to the bootstrap classpath (and the system property java.protocol.handler is declared
rem   to include Transit as the handler.  The inclusion of Transit enables the usage of
rem   Transit URIs as the source for activatable server applications.
rem ---------------------------------------------------------------------------------------

setlocal

:SET_RMID
if "%DPML_HOME%" == "" set DPML_HOME=%APPDATA%\DPML
if "%DPML_SYSTEM%" == "" set DPML_SYSTEM=%DPML_HOME%\Shared

set $DPML_BOOT=%DPML_HOME%\Data\lib
set DPML_TRANSIT_JAR=%$DPML_BOOT%\@TRANSIT-PATH@
set $POLICY=-Djava.security.policy=%DPML_SYSTEM%\bin\security.policy
set $BOOTCLASSPATH=-Xbootclasspath/a:%DPML_TRANSIT_JAR%
set $TRACE=-Dsun.rmi.server.exceptionTrace=true
set $TRANSIT=-Djava.protocol.handler.pkgs=net.dpml.transit

:RUN_RMID
@echo on
rmid -log %DPML_HOME%\Data\logs\rmid -J%$POLICY% -J%$TRACE% -C%$BOOTCLASSPATH% -C%$POLICY% -C%$TRANSIT%
@echo off

goto EndOfScript
:EndOfScript

endlocal
