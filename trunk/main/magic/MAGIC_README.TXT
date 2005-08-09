
Product: Magic
Description: Intelligent Build System
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Installation Instructions
-------------------------

$ depot -setup

Release Notes: Version 20050809
-------------------------------

Revision: 191
Improvements to <x:path> enabling self reference of content in a path
(i.e. a project can request a path that includes its own resources).
Restructruring of the handling of links under <x:path> creation and 
path creation in the Resource class. Enhancements to the replicate 
task to provide support for replication of project resources associated
with a project invoking <x:replicate>.

Release Notes: Version 20050806
-------------------------------

Revision: 176

Checkstyle task moved out of core and to an independent plugin.
Complete checkstyle cleanup.

Release Notes: Version 20050803
-------------------------------

Revision: 155

Release Notes: Version 20050726
-------------------------------

Revision: 118

The usage of non-versioned tremplates has been removed.  As such you may need to update you Transit import statements to either reference the template from DPML Preferences or declare you own alternative uri.  To update build files to use the Magic's Prefs supplied template you can use the following ant script:

  <target name="patch">
    <replace token="artifact:template:dpml/magic/reactor" 
         value="local:template:magic/reactor" dir=".">
      <include name="**/*.xml"/>
    </replace>
    <replace token="artifact:template:dpml/magic/standard" 
         value="local:template:magic/standard" dir=".">
      <include name="**/*.xml"/>
    </replace>
  </target>

