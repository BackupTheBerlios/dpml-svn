
The DPML platform has the four major product groups.
To build respective products please incoke the follwing commands:

$ ant transit           ... builds the transit distribution and updates the local installation
$ ant magic             ... builds the magic build system (requires Transit)
$ ant util              ... builds the util package (requires Magic)
$ ant depot             ... builds Depot (requires Transit and Magic)
$ ant metro             ... builds Metro (requires Transit, Magic, Depot)
$ ant test              ... builds intergrating tests (requires Metro)

Please note that the Transit build will install the junit jar file
under the .ant/lib directory if not available.  In such a case the
build will exit with a failed condition and a message requesting
recommencement of the build.  If you run transit target a second
time the build will continue as normal.

You can alternatively use the build.bat or build.sh which contains additional
variants, such as "all".

 