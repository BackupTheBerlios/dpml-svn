@echo off
if "%AVALON_HOME%" == "" set AVALON_HOME=%USERPROFILE%\.avalon
set AVALON_CMD_LINE_ARGS=%*
set AVALON_BOOTSTRAP_JAR=%AVALON_HOME%\repository\avalon-repository\jars\avalon-repository-cli-@AVALON_CLI_VERSION@.jar
set AVALON_SECURITY_POLICY=-Djava.security.policy=%AVALON_HOME%\bin\security.policy
java %AVALON_SECURITY_POLICY% %AVALON_JVM_OPTS% -jar %AVALON_BOOTSTRAP_JAR% %AVALON_CMD_LINE_ARGS%
goto EndOfScript
:EndOfScript
