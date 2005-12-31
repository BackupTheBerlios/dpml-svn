
Product: Transit
Description: Resource Management Framework
Publisher: The Digital Product Meta Library 
Home: http://www.dpml.net
Revision: @PROJECT-VERSION@

Installation Instructions
========================= 

Release Notes: Version @PROJECT-VERSION@
-------------------------------

Moved ${dpml.data}/lib to ${dpml.system}/lib.
Resolve bug in modern layout handler.
Updated transit to provide access to named layouts.
Changed the local system repository default layout from 'classic' to 'modern'.


Release Notes: Version 20051224
-------------------------------

Added a registry protocol handler enabling publication of references to an 
RMI registry in the form registry://[host]:[port]/[path] or the shortform
registry:/[path]. Updates to the standard log message formatter to include
the JVM process identifier.  Updates to the ContentModel implementation to
include the declaration of constructed types used as plugin parameters.
Improvement to the plugin loader preventing the assignment of an argument 
to more than one constructor parameter value.  Addition of support for
primitive arguments in plugin constructors (such as int, boolean, etc.).
Upgrade of plugin model to include support for system level jar files 
(e.g. protocol handlers or L&F implementations that need to exist in the 
system classloader).  Added Enum and ValuedEnum to util package. Extended
the Construct class to alow references to static member variables. Add
a prefs task to Transit tools supporting the generation of XML based 
preferences info in preparation for replacement of configuration and 
parameters.  Consolidated all configuration options down to a single strategy
based on the net.dpml.transit.info immutable datatypes.  Added support for 
the 'modern' layout strategy which add compatibility with the recent Maven2
repository layout.  Added the transit commandline configuration tool.

Release Notes: Version 20050809
-------------------------------

Revision: 191
Implementation improvements to transit management of dynamic content
handlers concerning supply of the current logging channel to the handler.
Addition of isWarnEnabled to Translit Logger. Improvements to debug info 
generated from the transit classloader.

Release Notes: Version 20050806
-------------------------------

Revision: 176
Complete checkstyle cleanup.
Javadoc improvements.

Release Notes: Version 20050803
-------------------------------

Revision: 155

Running RMID with Transit
  
  The following example commandline demonstrates the execution 
  of the RMID process with Transit enabled on sub-processes. 
  The [DPML] symbol should be replaced by the the absolute file 
  path to respective directories.

  $ rmid -J-Xrs -log [DPML]\logs\rmid 
      -J-Djava.security.policy=[DPML]\Shared\bin\security.policy 
      -C-Xbootclasspath/a:[DPML]\Data\lib\dpml-transit-main.jar 
      -C-Djava.security.policy=[DPML]\Shared\bin\security.policy 
      -C-Djava.protocol.handler.pkgs=net.dpml.transit

Release Notes: Version 20050726
-------------------------------

Revision: 118


