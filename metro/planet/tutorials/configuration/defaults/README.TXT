
Configuration Management
------------------------

This tutorial covers default configuration using the <classname>.xconfig resource.

$ ant
$ merlin target\classes -execute
[INFO   ] (tutorial.hello): configuration stage
[INFO   ] (tutorial.hello): initialization stage
[INFO   ] (tutorial.hello): source: packaged default

In the above example the configuration source is resolved from a 
configuration default packaged with the class (see <classname>.xconfig).

