
Hello
=====

Hello is a demonstration component used as part of the 
Merlin Tutorial.

Overview
--------

An example of a minimal componet, packaged under a container, 
deployable under Merlin.  The project includes:
 
  * HelloComponent.java - the component source
  * block.xml - the container defintion

Build
-----

Build the project using the following command:

$ ant

The above command triggers the default target "install" which will 
create a jar file under the target/deliverables/jars directory named 
hello.jar.  The jar file contains a single component, generated .xinfo 
descriptor, and a bundled block.xml deployment descriptor. 

Runtime
-------

To see Merlin in action, execute Merlin and give it either the 
jar file of the target/classes directory as the deployment 
argument.  

$ merlin target\classes -execute

Or:

$ merlin target\deliverables\jars\hello.jar -execute

The -execute parameter in the above command line simply tells 
Merlin to deploy and decomission the component.  If we didn't
include the -execute option, then Merlin would stay running
until we forced termination using ^C.

To get an idea of what is happening behing the scenes you can
override the logging priorities using a configuration override
argument.  In addition, the -debug CLI parameter presents a 
summary of the resources pulling together to handle deployment:

$ merlin target\classes -execute -debug -config conf\config.xml


