@echo on

mkdir %DPML_HOME%\Data\work
mkdir %DPML_HOME%\Data\logs

dpml.exe -install STATION %JAVA_HOME%\jre\bin\server\jvm.dll -Djava.class.path=%DPML_HOME%\Data\lib -Djava.system.class.loader=net.dpml.transit.SystemClassLoader -Djava.security.policy=%DPML_HOME%\Shared\bin\security.policy -start net.dpml.depot.Main -method start -stop net.dpml.depot.Main -method stop -out %DPML_HOME%\Data\logs\dpml-station-stdout.log -err %DPML_HOME%\Data\logs\dpml-station-stderr.log -current %DPML_HOME%\Data\work -manual

@pause
