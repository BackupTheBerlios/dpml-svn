
Dependency Management
=====================

Overview
--------

The manual dependencies tutorial covers additional information dealing 
with the explict control over dependency binding.  In this example we 
have HelloComponent with a dependency on two services of the same type. If we 
depended on classic merlin auto-assembly we would not necessarily get the 
desired result.  In such a situation, Merlin provides support for 
overriding the auto assembly process though explicit dependency directives
included inside a <component/> tag.

In the block.xml we have three component directives:

     <component name="gloria" class="tutorial.IdentifiableComponent"/>
     <component name="nancy" class="tutorial.IdentifiableComponent"/>

     <component name="hello" class="tutorial.HelloComponent">
       <dependencies>
         <dependency key="primary" source="gloria"/>
         <dependency key="secondary" source="nancy"/>
       </dependencies>
     </component>

The "hello" component has been manually wired together using named components references under a set of <dependency/> directives, thereby overriding Merlin auto-assembly huristics.

Build instructions
------------------

  $ ant

Runtime
-------

  $ merlin target\classes -execute

[INFO   ] (tutorial.nancy): contextualize
[INFO   ] (tutorial.gloria): contextualize
[INFO   ] (tutorial.hello): initialization
[INFO   ] (tutorial.hello): assigned primary: /tutorial/gloria
[INFO   ] (tutorial.hello): assigned secondary: /tutorial/nancy

Summary
-------

The purpose of this demonstration is to show the following:

  1. ability to override assembly using <dependency/> directives
     (i.e. your still in control)
  2. provide an example of dealing with multiple dependencies of 
     the same type
     
