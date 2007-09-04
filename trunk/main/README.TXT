DPML Platform Installation README.
----------------------------------

The DPML SDK distribution comes in three flavours:

  1. a automatic installer for Windows
  2. an alternative zip file for a windows installation
  3. a tar.gz file for nix installation

If your not using the Windows installer then you will need to unpack the 
archive in your preferred location.  This result in the creation of a 
single directory named 'dpml'.

To complete the installation:

  1. define the DPML_HOME environment variable pointing to the 
     'dpml' directory

  2. add the [the-dpml-home-path]/share/bin directory into 
     your system path

To verify your installation you can validate the installed Metro version 
using the following command:

  $ metro -version

Win32 users can install the Station as a NT service by executing the 
install-DPML-SCM.bat command file (note that this is automatically 
installed as a manually activated service by the Windows installer).  
After installation you can start the station manually using the 
following command:

  $ net start dpml

Alternatively, Windows users can modify the DPML service settings using 
the Control Panel / Administrative Tools / Service control panel.

The equivalent functionality on UNIX platforms is achieved using the 
station commands within machine start-up and shutdown scripts.

  $ station startup
  $ station shutdown

If you encounter any problems please consult relevant online documentation 
and/or post a note to the DPML support list.

  http://www.dpml.net/about/resources/lists.html

Stephen McConnell, DPML


