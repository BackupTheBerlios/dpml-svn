
To build the DPML suite - please following the following procedure:

  a) build the bootstrap resources
  b) build the main DPML system
  c) build the DPML installation

Bootstrap build
---------------

Create a directory to hold the svn codebase (e.g. c:\dev\dpml\main), checkout the 
sources, cd to the main directory and invoke the bootstrap build procedure.

  $ cd dev\dpml\main
  $ svn checkout svn://svn.berlios.de/dpml/trunk/main
  $ bootstrap

DPML Platform build
-------------------

The bootstrap procedure will create a number of executables under the DPML_HOME%/Shared/bin directory.  Before invoking 'build' make sure the %DPML_HOME%/Shared/bin is declared in your PATH environment variable.

  $ build

