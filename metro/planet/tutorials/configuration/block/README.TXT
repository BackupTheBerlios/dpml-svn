
Configuration Management
------------------------

This tutorial covers the declaration of an configuration in the 
block.xml deployment directive plus a few releated features.

Build using:

  $ ant

Execute the demo and note the message concerning configuration source.
In the example the configuration is based on the directive included 
under the <conponent> tag.

  $ merlin target\classes -execute

Execute the demo again using an alternative configuration target.

  $ merlin target\classes -execute - config conf/config.xml

Note that the configuration source has changed based on the 
target declared in config.xml.  Just for fun you can also see how
you can use targets to modify logging priorities:

  $ merlin target\classes -execute - config conf/categories.xml

