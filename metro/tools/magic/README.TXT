
Before attempting to build the bootstrap build system please 
read etc/deliverables/README.TXT.  You will also need Apache
Ant version 1.6.1 or later.

To build and install the antlib using the following command:

  $ cd avalon/tools/project
  $ ant -buildfile bootstrap.xml install

The core antlib is now installed and we can use magic to build and package itself in a block archive.

  $ ant clean install

To simulate a gump build of magic by magic

 $ ant -Dmagic.home=target/123 \
       -Dgump.signature=123 \
       -Dgump.resource.ant=lib/ant.jar \
       -Dgump.resource.junit=lib/junit.jar \
       -Dgump.resource.ant-junit=lib/ant-junit.jar \
       -Dgump.resource.ant-nodeps=lib/ant-nodeps.jar


