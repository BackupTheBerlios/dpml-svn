
Build instructions
------------------

  $ build clean install


Notes
-----

This project is a tescase for XML-Schema based validation of a generalized 
plugin descriptor that leverages XML-namespaces to allow the declaration of 
a custom plugin handler.  Two examles are provided 'plugin.xml' and 
'component.xml'.  The plugin.xml file is an example of what a transit plugin
defintion wouold look like (it contains a transit plugin strategy declaration).
The component.xml used the same enclosing structure but defines a component
deployment strategy.

The purpose of the project is to check that everything works as expected when
running under any of the following four scenarios:

  1.  JDK 1.4 plain
  2.  JDK 1.4 with xeces in lib/endorced
  3.  JDK 1.5 plain
  4.  JDK 1.5 with xeces in lib/endorced

When running under 1.4 without xerces xml content will not be validated.
When running under 1.4 + Xerces xml content will be validated using XML-Schema.
Test related to 1.5 are pending.

Namespace names and schema resolving strategies are subject to change.
