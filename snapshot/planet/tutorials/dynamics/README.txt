
Dynamics
========

A demonstration of the dynamic manipulation of the meta-model.

Overview
--------

The examples demonstrates the activation of a two component models, 
gizmo and widget.  The example shows the programatic manipulation 
of the component model configration and context entires (as examples
of dynamic model modification). During the example an widget is 
created and deployed using its default parameters.  We then update 
the configuration and redeploy the a component. Logging information
reflects the new profile.  The same process is applied to the gizmo
coponent except that we modify a context entry as opposed to the 
configuration.


Build
-----

Build the project using the following command:

$ ant

Runtime
-------

$ merlin target\deliverables\jars\avalon-tutorial-dynamics.jar -execute


