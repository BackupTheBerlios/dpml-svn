
Project: Test
Description: Integration tests.
Publisher: The Digital Product Meta Library

The test project contains a series of components and integration testcases dealing with the DPML platform. 

The package is dependent on the following modules:

  dpml-transit
  dpml-magic
  dpml-depot
  dpml-metro

The project contains:

  test/components

     simple - A series of components dealing with content management.
              This includes examples ranging from very simple 
              constructed context values through to components 
              assigned as context values, examples of typed context
              interfaces, and context management by a managing 
              component.

       plus - The project contains a component that manages the 
              state of another component, and a component that 
              manages the configuration of another component.

  test/plugins

     layout - A plugin that provides classic repository layout support
              used to validate pluggable layout strategies under Transit.

  test/unit

     simple - The test/unit/simple/build.xml contains lots of examples
              of part creation using the <component> task.  Unit tests
              include SimpleTestCase and ExampleTestCase which 
              exercises the parts declared in in the build file. The 
              CompositeTestCase is more focussed on container related
              testing.

     plus -   The test/unit/plus/build.xml contains a couple of examples
              of composite components which are exercised in 
              ConfigurationTestCase and ManagementTestCase.  The package 
              also includes URLTestCase which demonstrates programmatic 
              setup of a Transit content manager and the a range of 
              component related operations that can be performed against a 
              URL.

/SJM