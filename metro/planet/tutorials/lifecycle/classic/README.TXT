
Excalibur Extension Tutorial
----------------------------

This tutorial covers usage of classic excalibur lifecycle extensions.

$ maven
$ merlin -execute target\classes

[INFO   ] (tutorial.extension): initialize
[INFO   ] (tutorial.extension): invoking create on target
[INFO   ] (tutorial.hello): extension said: create id: 24109300, Thread[Thread-0,5,main]
[INFO   ] (tutorial.extension): invoking access on target
[INFO   ] (tutorial.hello): extension said: access id: 24109300, Thread[Thread-0,5,main]
[INFO   ] (tutorial.extension): invoking destroy on target
[INFO   ] (tutorial.hello): extension said: destroy id: 24109300, Thread[Thread-0,5,main]
[INFO   ] (tutorial.extension): dispose
