MERLIN NSIS Installer Script

You will need the Nullsoft Installation System to compile this script
see: http://nsis.sourceforge.net/

Features:
  - Installs the base Merlin platform
  - Optionally installs the docs, tutorials, plugins and service
  - Creates a MERLIN_HOME env variable for ALL USERS
  - Installs the Merlin NT service
  - Adds Merlin to the PATH
  - Installs the plugins into MAVEN_HOME/plugins or if maven is
    not installed into MERLIN_HOME/plugins

To Do:
  - Check for JVM and version.  Install endorsed libs if needed
  - All install for one user or many
  - Install a default app that includes the merlin facilities
  - Add desktop and start menu shortcuts (to what?)

To compile:

   1. check out the meta, util, repository, tutorials, and merlin CVS modules
   2. use maven to build the merlin 
   3. use maven to build merlin docs (maven site:generate)
   4. use maven to build the tutorials (maven avalon:build)
   5. compile the NSIS script

 
