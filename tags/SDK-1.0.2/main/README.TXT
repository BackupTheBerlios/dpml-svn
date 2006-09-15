
DPML PLatform Installation README.
----------------------------------

The DPML Platform distribution archive contains a complete development
and runtime installation.  The distribute archive contains a single
directory named 'dpml'.  After unpacking the archive, move the dpml 
directory to your prefered install location.

To complete the installation:

  1. define the DPML_HOME environment variable

  2. include the share/bin directory into your system path

To verify your installation you should be able to list the current 
Transit resource configuration using the following command:

  $ transit info

Win32 users can install the Station as a NT service by executing the 
install-DPML-SCM.bat command file.  Following installation you can start
the station manually using the following command:

  $ net start dpml

The equivalent functionality on unix platforms is achived using the 
station commands within machine startup and shutdown scripts.

  $ station startup
  $ station shutdown

If you encounter any problems please post a note to the DPML support list.

  http://www.dpml.net/about/resources/lists.html

