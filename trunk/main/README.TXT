
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

  $ build

  Creation of the installation is currently in progress.  For the moment 
  the main/target/bundle directory contains the generated installation 
  files.  Packaging of the installation in zip and tar archives is not 
  yet complete.

DPML Planet build
-----------------

  $ cd dev\dpml\planet
  $ svn checkout svn://svn.berlios.de/dpml/trunk/planet
  $ build




