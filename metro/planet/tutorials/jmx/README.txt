
JMX
=====

JMX is a demonstration component used as part of the 
Merlin Tutorial, showing how to establish JMX for Merlin hosted
components.

Overview
--------
We are using the simplest form of component, the Hello component,
and adding an simple management interface, which allows to set
the output string.

  * HelloComponent.java - the component source
  * block.xml - the container defintion

Build
-----

Build the project using the following command:

$ maven

The above command triggers the default goal jar:install which will 
create a jar file and copy it to your local Maven repository,
named jmx-hello-1.0.jar.  

Runtime
-------

To see Merlin in action

$ merlin --offline conf/hello.block

This will start the small HelloComponent and have it registered as
a JMX MBean in the embedded MBeanServer. You can view the content
by starting your favourite browser, and connect to;

   http://localhost:8082/
   
In there, you can locate the HelloComponent, set the output string
and execute the sayHello() method.


