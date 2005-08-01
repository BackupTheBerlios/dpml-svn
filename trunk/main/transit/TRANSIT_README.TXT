
Product: Transit
Description: Resource Management Framework
Publisher: The Digital Product Meta Library 
Home: http://www.dpml.net
Revision: @PROJECT-VERSION@

Installation Instructions
========================= 

Transit is installed as a part of the Depot installation.  
Please refer to the Depot README for additional information.

Release Notes: Version 20050726
-------------------------------

Revision: 118

Release Notes: Version 20050802
-------------------------------

Revision: 152

Running RMID with Transit
  
  The following example commandline demonstrates the execution 
  of the RMID process with Transit enabled on sub-processes. 
  The [DPML] symbol should be replaced by the the absolute file 
  path to respective directories.

  $ rmid -J-Xrs -log [DPML]\logs\rmid 
      -J-Djava.security.policy=[DPML]\Shared\bin\security.policy 
      -C-Xbootclasspath/a:[DPML]\Data\lib\dpml-transit-main.jar 
      -C-Djava.security.policy=[DPML]\Shared\bin\security.policy 
      -C-Djava.protocol.handler.pkgs=net.dpml.transit


