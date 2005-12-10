
Installing the DPML Station is an NT Service.

$ cd %DPML_HOME%\bin\server
$ INSTSRV.EXE DPML %DPML_HOME%\bin\server\SRVANY.EXE

In regedit open the item 

  HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\DPML

Add a sub-key named 'Parameters' and add a REG_SZ value inside Parameters with the 
name 'Application'. Set the value of the 'Application' field to the absolute 
filename of DPML.EXE.

Add a string value (REG_SZ) named 'AppDirectory' and set its value to the absolute 
path of the working directory.  E.g. C:/dpml/Data/work.

You can optionally add a sub-key named 'Description' to the DPML entry with a value 
such as 'DPML Station provides support for the management of multiple Metro processes.'.

Startup the server.

$ net start dpml

