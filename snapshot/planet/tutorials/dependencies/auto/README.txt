
Dependency Management
=====================

Overview
--------

The dependencies tutorial covers the declaration of service dependencies
that a component has, the resolution of dependecies by the container, and 
the application of dependent service by the container to the component 
via a ServiceManager.  

Service dependecies are declared in the component sources using the javadoc
tag:

  @avalon.dependency type="org.somewhere.MyInterface"

Components that provide services declare the service export using the 
tag @avalon.service. Merlin builds and validates a dependency graph before  
component initialization, and ensures that componets are actived in the  
correct order.  Consumers are always activated after suppliers and 
deactivated before deactivation of respective suppliers.

Build instructions
------------------

  $ ant

Runtime
-------

  $ merlin target\classes -execute

  [INFO   ] (tutorial.random): initialization
  [INFO   ] (tutorial.hello): initialization
  [INFO   ] (tutorial.random): processing request
  [INFO   ] (tutorial.hello): received random value: -1591330260
  [INFO   ] (tutorial.hello): disposal
  [INFO   ] (tutorial.random): disposal

Summary
-------

The purpose of this demonstration is to show the following:

  1. usage of @avalon.dependency tags
  2. usage of @avalon.service tags
  3. functionality of the container in handling automated 
     assembly
  4. orderly commissioning and decommissioning provided 
     by the container

While the demonstration deals with the very simple case of a
single supplier and a single consumer component, Merlin provides
complete support for n-to-n relationships across arbitarily deep
dependency graphs.


