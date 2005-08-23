
Product: Depot
Description: DPML Product Management Tool
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Release Notes: Version SNAPSHOT
-------------------------------

Incorporated Janel distribution under ASL 2.0 and added project and 
copyright atrtribution to NOTICE.TXT.  Upgrade to Janel 2.5.0 (resolving 
issues with working directory resolution). Updates to DPML Station with 
support for multi-JVM deployment and teardown.  Added a remote logging
services that handles message aggregation from multiple JVMs to the central
station and support for JCM process identity within the default log message
formatter. Mahor cleanup of the profile API and implementation with 
consolidation around the ApplicationProfile.

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


