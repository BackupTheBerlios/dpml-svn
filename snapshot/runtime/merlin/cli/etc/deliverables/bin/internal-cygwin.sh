#! /bin/sh
#


# For Cygwin, ensure paths are in UNIX format before anything is touched
JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
MERLIN_HOME=`cygpath --unix "$MERLIN_HOME"`
[ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`

JAVA="$JAVA_HOME/bin/java"

# switch necessary paths to Windows format before running java
JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
MERLIN_HOME=`cygpath --windows "$MERLIN_HOME"`
MERLIN_CLASSPATH=`cygpath --windows "$MERLIN_CLASSPATH"`
[ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`


echo "Starting Merlin $MERLIN_VERSION."
echo "===================="
echo "             Platform: $PLATFORM"
echo "            Java Home: $JAVA_HOME"
echo "          Merlin Home: $MERLIN_HOME"
echo "      Security policy: $MERLIN_HOME/bin/security.policy"
echo "          JVM Options: $MERLIN_JVM_OPTS"
echo "     Merlin Classpath: $MERLIN_CLASSPATH"
echo "     Merlin Arguments: $MERLIN_ARGS $@"
echo ""

ARGS="$MERLIN_JVM_OPTS \"-Djava.security.policy=$MERLIN_HOME/bin/security.policy\" -classpath \"$MERLIN_CLASSPATH\" org.apache.avalon.merlin.cli.Main $MERLIN_ARGS $@"

echo -n "$JAVA" >$MERLIN_HOME/command-line.log
echo "$ARGS" >>$MERLIN_HOME/command-line.log

echo $ARGS | xargs "$JAVA"
