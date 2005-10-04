
Product: Metro
Description: Component Runtime Platform
Publisher: The Digital Product Meta Library
Revision: @PROJECT-VERSION@

Release Notes: Version @PROJECT-VERSION@
-------------------------------

General enhancements to the Part contract model including improvements 
in the way top-level versus composite parts and handled.  Separation of
state related aspects into a new package 'net.dpml.state' supported by a 
cleaner implementation.  Introduced support for long-term storage of 
descriptors and directivies compliant with JSR-57. Added definition and 
implemetnation of a remote component model and context model suitable 
for deployment independently of a component runtime (i.e. management 
occurs on machine 'a' and deployment occurs on machine 'b'). Moved all
enumberations for formally typed enumerations (e.g. CollectionPolicy, 
LifestylePolicy, etc.).

Release Notes: Version 20050809
-------------------------------

Revision: 184

Internal changes to classloader construction arising from tests using 
different controller implementations within a single composite component.
These changes enable the concurrent execution of different controller 
implementations handling components models at different nodes in a component
model hierarchy. In practice this means that a component build with control
system A can deploy parts built with control system B which in turn can 
deploy parts using system X - the common factor being the part API (which 
is independent of lifecycle interfaces such as Configuration, Parameters, 
Logging, etc.).

Release Notes: Version 20050806
-------------------------------

Revision: 176

Release Notes: Version 20050803
-------------------------------

Revision: 155

Release Notes: Version 20050726
-------------------------------

Revision: 118

