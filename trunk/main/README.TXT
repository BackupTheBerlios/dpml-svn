
The DPML platform has the following major product groups:

  transit     .. a resource management system
  magic       .. an intelligent build system
  metro       .. an advanced IoC container
  depot       .. an application managment system
 
To build respective products a main.bat and main.sh script are provided.

  $ main all   .. builds all of the DPML core products
  
You can also build a selected product:

  $ main transit
  $ main magic
  $ main metro
  $ main depot

Finally, the main script also enables the creation of distributions using a release identifier.  The following example build the distributiing using the build identifier 1234.

  $ main all 1234

Any issues concerning building of the platform should be directed to the DPML dev mailing list.

