
Composition Management
======================

Overview
--------

This tutorial presents the creation of virtual components
using composition of package containers.  In this demonstration
we have three application groups, a top level application, a 
locator system, and a publisher system.  The top level app
pulls in the two sub-systems as virtual components.  The sub-
systems are themselves fully deployable blocks in their 
own right (although in this demonstratotion they represent
very simple sub-systems).  In practice, the combination of 
packaged deployment profiles, default configurations, 
auto discovery, auto assembly, and block composition provides
a powerful framework complex systems delivery.

Build and execution instructions:
---------------------------------

  $ cd application/impl
  $ ant
  $ merlin -execute target\deliverables\bars\*.*

  [INFO   ] (application.publisher.publisher): created
  [INFO   ] (application.location.info): location: Paris
  [INFO   ] (application.application): servicing application
  [INFO   ] (application.location.info): location: Paris
  [INFO   ] (application.publisher.publisher): created
  [INFO   ] (application.publisher.publisher):
  ******************
  * Paris
  ******************
  [INFO   ] (application.application): done


Use the following command to include container debug level log entries for the 
demanding developer.

  $ merlin -execute target\deliverables\bars\*.* -config conf\config.xml

Summary
-------

The purpose of this demonstration is to show how the container can be viewed as
an implementation strategy for the creation of a virtual service, and how these
service can be composed within a higher level containers, enabling new 
implementation solutions.  The key to achiving this is related to the <service> 
directives inside the <container> directive.  Each service directive describes 
a service that is exported by the container in its role as a virtual component.


