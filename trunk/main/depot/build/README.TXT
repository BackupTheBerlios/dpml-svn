
Overview
--------

This package contains an plugin that launches the Ant build system.  

The plugin declares dependencies on the core ant jar, ant-junit, and 
the jdk tools jar.  You will need to place a copy of tools.jar in you 
local repository under sun/jars/tools-1.4.2.jar.  

To use this package you need to build the plugin using classic ant.

  $ cd main
  $ ant


Demonstration:
--------------

To execute a demonstration of a ant-on-transit you can invoke the following 
command from the main directory (i.e. build the project using magic via ant 
via transit):

  $ magic clean install

Commandline options currently supported by the plugin include:

  -verbose -file [file] [target] [target] ...

Known issues:
-------------

The plugin is demonstrating a problem with xls sheet handling which appears to 
be related to classloader assumptions in ant's junit task.


