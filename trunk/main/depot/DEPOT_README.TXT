
Product: Depot
Description: DPML Build System
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Release Notes: Version @PROJECT-VERSION@
-------------------------------

Resolves bugs identified following the 005124 release.

  1. Resolve bug concerning checkstyle plugin reference.
  2. Improve project invalid reference error reporting.
  3. Resolve bug concerning undefined default filter values.
  4. Resolve bug concerning checkstyle rules property evaluation.
  5. Resolve packaging bug (missing lib directory content)

Updates to the replicate task allowing for the selection of a target 
repository layout scheme.

Release Notes: Version 20051224
-------------------------------

Incorporated Janel distribution under ASL 2.0 and added project and 
copyright attribution to NOTICE.TXT.  Upgrade to Janel 2.5.0 (resolving 
issues with working directory resolution). Updates to DPML Station with 
support for multi-JVM deployment and teardown.  Added a remote logging
services that handles message aggregation from multiple JVMs to the central
station and support for JVM process identity within the default log message
formatter. Major cleanup of the profile API and implementation with 
consolidation around the ApplicationProfile class.  Updated Depot icons 
based on content from CrystalXP.net and inclusion of credits in 
NOTICE.TXT.  Improvements to Depot Preferences when running on JRE 5.0
under Windows and general enhancements to Preferences table information.

Enhanced the console to support deployment of plugin handlers that add 
supplimentary content to the system classloader.

Added support for dynamic loading of a part editor to ensure that different
versions of parts can supply their own editors based on the underlying 
controller declared by the part instance.

Release Notes: Version 20050809
-------------------------------

Revision: 191
Improvements to the install task relating to the removal of cached 
links during the bundle expansion process - which in turn resolves issue
related to version downgrading.  This milestone now properly supports setup
version arguments greater then or less than the current version.  Also 
includes upgrades to download logging info and general feedback provided 
during the setup process.

Release Notes: Version 20050806
-------------------------------

Revision: 176

Release Notes: Version 20050803
-------------------------------

Revision: 155

Separation of profile API.
Replacement of depot.bat with depot.exe.
Support for -D[name]=[value] under depot console
Separation of application handling as a plugin.
Replacement of -install option with -setup option.
Cleaner and safer setup process (such to validation unde nix).
Support for native Win32 setup subprocess and java for nix.

Release Notes: Version 20050726
-------------------------------

Revision: 119


