
Product: Depot
Description: DPML Product Management Tool
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Release Notes: Version 20050726
-------------------------------

URL: svn+ssh://svn.berlios.de/svnroot/repos/dpml/trunk/main
Repository UUID: 00579e91-1ffa-0310-aa18-b241b61564ef
Revision: 119

Running RMID with Transit
-------------------------

The following example commandline demonstrates the execution of the RMID process with Transit enabled on sub-processes. The [DPML] symbol should be replaced by the the absolute file path to respective directories.

rmid -J-Xrs -log [DPML]\logs\rmid -J-Djava.security.policy=[DPML]\Shared\bin\security.policy -C-Xbootclasspath/a:[DPML]\Data\lib\dpml-transit-main.jar -C-Djava.security.policy=[DPML]\Shared\bin\security.policy -C-Djava.protocol.handler.pkgs=net.dpml.transit


