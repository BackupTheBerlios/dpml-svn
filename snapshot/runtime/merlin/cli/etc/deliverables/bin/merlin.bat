@echo off

rem set JAVA CMD
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java

goto SET_MERLIN

:USE_JAVA_HOME
set JAVA=%JAVA_HOME%\bin\java

:SET_MERLIN
if "%MERLIN_HOME%" == "" set MERLIN_HOME=%USERPROFILE%\.merlin
set MERLIN_CMD_LINE_ARGS=%*
set MERLIN_SECURITY_POLICY=-Djava.security.policy=%MERLIN_HOME%\bin\security.policy
set MERLIN_CLASSPATH=@WINDOWS-CLI-CLASSPATH@;%MERLIN_HOME%\system\@WINDOWS-CLI-MAIN-PATH@

:RUN_MERLIN
%JAVA% %MERLIN_SECURITY_POLICY% %MERLIN_JVM_OPTS% -classpath %MERLIN_CLASSPATH% org.apache.avalon.merlin.cli.Main %MERLIN_CMD_LINE_ARGS%
goto EndOfScript
:EndOfScript
