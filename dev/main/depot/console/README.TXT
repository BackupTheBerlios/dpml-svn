
A commandline handler that will read a classpath from preferences, create the initial classloader, and deloy.  The cli handler will be 100% standalone (no dependencies).  Preference node selection will be based on a profile name.

Example usage that default to launching the preferences editor:

  $ depot

Alternative target application launching:

  $ depot -profile [name]

The profile [name] is a user preferences node under the path net/dpml/depot/console/profiles.

Application preferences node atributes:

  title -- a human readable title of the profile
  classname -- the classname of a class implemeting a static main method

Children nodes:

  classpath -- the value of each named attribute is a classpath url
  properties -- each attribute is a system property name and value

