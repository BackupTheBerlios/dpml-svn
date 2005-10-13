@echo off
set CMD_LINE_ARGS=%*
if "%MAGIC_CLI_PLUGIN_URI%" == "" set MAGIC_CLI_PLUGIN_URI=artifact:plugin:dpml/depot/dpml-depot-build#SNAPSHOT
transit -load %MAGIC_CLI_PLUGIN_URI% %CMD_LINE_ARGS%
