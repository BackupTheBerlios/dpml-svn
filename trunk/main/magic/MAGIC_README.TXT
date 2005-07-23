
Product: Magic
Description: Intelligent Build System
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Installation Instructions
=========================

$ depot -install magic

Special Notes:
--------------

23-JUL-2005:
------------

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


