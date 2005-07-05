
Product: Transit
Description: Resource Management Framework
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Installation Instructions
========================= 

Contents of the distribution (LINUX)
------------------------------------
 
  /dpml-transit-linux-@PROJECT-VERSION@
    /share
      /bin ............................. cli scripts for the transit system
      /local ........................... shared repository
      /docs ............................ javadoc for util and transit modules
    /data
      /data ............................ user's data
      /prefs ........................... application preferences
      /transit ......................... transit configuration files

Contents of the distribution (WIN32)
------------------------------------

  /dpml-transit-win32-@PROJECT-VERSION@
    /Shared
      /bin ............................. cli scripts for the transit system
      /local ........................... shared repository
      /docs ............................ javadoc for util and transit modules
    /Data
      /data ............................ user's data
      /prefs ........................... application preferences
      /transit ......................... transit configuration files

Creating the installation:
--------------------------

  1. Create your DPML system directory - instructions are provided
     at the following url (don't forget to include the 
     DPML_SYSTEM bin directory in you PATH environment variable)
     http://www.dpml.net/guide/setup/dpml.html

  2. Copy the contents share directory to your DPML_SYSTEM directory.

  2. Copy the contents data directory to your DPML_HOME home directory.

     If DMPL_HOME is not declared as an environment variable the default
     location on Linux is ${user.home}/.dpml.  The default location on
     Windows is 'Document and Setting\[username]\Application Data/DPML'.

  You should now have a directory structure that looks like the following;

    /[DPML-SYSTEM]
      /bin
      /docs
      /local

    /[DPML_HOME]
      /prefs
      /cache
      /dist

Verifying the installation
--------------------------

  The following command verifies that the Transit system is correctly installed.
  From a new console windw, execute the following command:

    $ transit -Xversion

  You should see output similar to the following:

    Transit: artifact:plugin:dpml/transit/dpml-transit-plugin#@PROJECT-VERSION@
    Build: 20050129.042138

Transit Documentation
---------------------

Reference documentation and user guides are available under the DPML Home.

  Transit Product Documentation
  http://www.dpml.net/products/transit

  Transit User's Guide
  http://www.dpml.net/guide/transit

