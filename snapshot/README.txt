=======================================================================
            __
           /  \    Apache
          /    \__________________________________
         /  /\  \  \/  /    \ |  |  /   \|   \|  |
        /  /  \  \    /  /\  \|  |_|  O  |  \ \  |
       /__/    \__\__/__/  \__\_____\__ /|__|\___|

=======================================================================


=======================================================================
summary:   Apache Avalon provides a complete platform for component
           programming including a core framework, utilities, tools,
           components, and a container written in the Java language.
publisher: Apache Software Foundation
website:   http://avalon.apache.org/
license:   Please see the LICENSE.txt file
=======================================================================

This is the main cvs module for the Apache Avalon project. It is
organised into several subdirectories containing several subprojects.
See the documentation and other resources inside those subdirectories
for more information.


More documentation?
===================
Most of our documentation is stored in SVN only in xml format. The HTML-
formatted pages that are generated from those xml files are available at
our website at

    http://avalon.apache.org/


Building from source?
=====================
Avalon uses an Apache Ant based build system dubbed 'magic'.  To
build from source, download an install Ant 1.6.1 or later, then
run:

   ant setup               <-- only needed once to bootstrap magic
   ant clean               <-- deletes any 'dirty' content
   ant                     <-- builds entire Avalon platform

Magic uses an environmental variable "MAGIC_HOME" which defaults
to ${user.home}/.magic .  This directory contains artifact
and document caches.

Magic also uses a set of property files for handling default behavior.
You may want to copy the contents of the 'build.properties' file into
a new file named 'user.properties' which you can then use to override
any default build values (such as HTTP proxies).

The 'setup' tasks requires internet access to download the latest
plugins and dependencies. [fixme: offline setup instructions]


What is the repository layout?
==============================
A brief explanation of the source respository structure.  For more
info, see each subproject's own README documentation:

   central              
     laboratory         <--  code sandbox, latest development magic system home directory
     site               <--  XML source site documentation
     system             <--  site transformation (XSL) resources
   discovery            <--  service publishing and delivery system
   planet               <--  service and component library
     cornerstone        <--  component block library
     facilities         <--  container extensions
     tutorials          <--  tutorial source code
   runtime              <--  core Avalon platform source code
     activation         <--  service activation library
     buildsystem        <--  (old) build system resources
     composition        <--  service composition library
     framework          <--  core Avalon framework
     logging            <--  Avalon logging framework
     logkit             <--  Avalon LogKit framework
     merlin             <--  core merlin container runtime source
     meta               <--  Avalon meta framework
     repository         <--  Avalon repository framework
     util               <--  Avalon utilities source code
     versioning         <--  source code versioning entity documents
   studio               <--  Avalon IDE Studio
   tools                <--  Avalon tools
     magic              <--  Avalon Magic Build System


Where did the XXXX subproject go?
=============================================
There's been quite a bit of reorganization and somethings have been
moved, deprecated, or archived.

    excalibur/ecm   -->  http://excalibur.apache.org
    fortress        -->  http://excalibur.apache.org
    phoenix         -->  http://loom.codehaus.org

A CVS snapshot can be found in SVN at:

   https://svn.apache.org/repos/asf/avalon/cvs-migration-snapshot/

More information on Avalon legacy software can be found at:

   http://avalon.apache.org/products/legacy/index.html
