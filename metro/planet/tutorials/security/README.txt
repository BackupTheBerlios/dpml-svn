
Security enabled Composition Management
=======================================

Overview
--------

This is an extension of the tutorial/composition, in that here
we use security profiles to GRANT access for one component, but
not the other, to the Location, which is configured to 
"Europe.France.Paris" in the Application.xprofile defaults.
One application component has the security enabled for Spain
and the other has the security enabled for France. 

This tutorial is interesting as it shows that it is possible
to grant different security access to the same codebase, which
is not possible in standard Java security policies.

For more information about the other aspects of this example, 
please check the tutorial/composition, which is practically
identical.

Build and execution instructions:
---------------------------------

  $ cd application/impl
  $ maven build
  $ merlin -execute target\*.jar -repository %MAVEN_HOME% 

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

  $ merlin -execute -repository %MAVEN_HOME% target\*.jar -config conf\debug.xml

Summary
-------
The purpose of this tutorial is to show the power of Merlin's security
features. That it is possible to assign permissions to individual 
components, even if they are part of the same codebase.

