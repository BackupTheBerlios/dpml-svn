@echo on

mkdir %DPML_HOME%\Data\work
mkdir %DPML_HOME%\Data\logs\depot

dpml\depot.exe -install DEPOT %JAVA_HOME%\jre\bin\server\jvm.dll -Djava.class.path=%DPML_SYSTEM%\bin -Djava.system.class.loader=net.dpml.depot.lang.DepotClassLoader -Djava.security.policy=%DPML_SYSTEM%\bin\security.policy -Djava.class.path=%DPML_HOME%\Data\lib\dpml-transit-main-SNAPSHOT.jar;%DPML_HOME%\Data\lib\dpml-depot-console-SNAPSHOT.jar -start net.dpml.depot.Main -params start -stop net.dpml.depot.Main -method stop -out %DPML_HOME%\Data\logs\depot\stdout.log -err %DPML_HOME%\Data\logs\depot\stderr.log -current ;%DPML_HOME%\Data\work -manual

@pause
