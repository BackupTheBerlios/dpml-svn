
The DPML platform has the following major product groups:

  transit     .. a resource management system
  magic       .. an intelligent build system
  metro       .. an advanced IoC container
  depot       .. an application managment system
 
To build respective products a build.bat and build.sh script are provided.

  $ build all   .. builds all of the DPML core products
  
You can also build a selected product:

  $ build transit
  $ build magic
  $ build metro
  $ build depot

Finally, the build script also enables the creation of distributions using a release identifier.  The following example build the distributiing using the build identifier 1234.

  $ build all 1234

Any issues concerning building of the platform should be directed to the DPML dev mailing list.

