
To build the DPML suite - please following the following procedure:

  a) build the bootstrap resources
  b) build the main DPML system
  c) build the DPML installation

Bootstrap build
---------------

Create a directory to hold the svn codebase (e.g. c:\dev\dpml), checkout the 
sources, cd to the main directory and invoke the bootstrap build procedure.

  $ cd dev\dpml
  $ svn checkout svn://svn.berlios.de/dpml/trunk/main main
  $ cd main
  $ bootstrap

System Build
------------

Following the bootstrap build you can invoke the complete system build using the following command.

  $ build

Installation Build
------------------

Creation of the installation is in progress.  For the moment the main/target/bundle 
directory contains the generated installation files.  Packaging of the installation 
in zip and tar archives is not yet complete.



