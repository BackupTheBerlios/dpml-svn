#! /bin/sh
#

echo "Starting Merlin $MERLIN_VERSION."
echo "===================="
echo "             Platform: $PLATFORM"
echo "            Java Home: $JAVA_HOME"
echo "          Merlin Home: $MERLIN_HOME"
echo "      Security policy: $MERLIN_HOME/bin/security.policy"
echo "          JVM Options: $MERLIN_JVM_OPTS"
echo "     Merlin CLasspath: $MERLIN_CLASSPATH"
echo "     Merlin Arguments: $MERLIN_ARGS $@"
echo ""

JAVA="$JAVA_HOME/bin/java"

ARGS="$MERLIN_JVM_OPTS \"-Djava.security.policy=$MERLIN_HOME/bin/security.policy\" -classpath \"$MERLIN_CLASSPATH\" org.apache.avalon.merlin.cli.Main $MERLIN_ARGS $@"
echo -n "$JAVA" >$MERLIN_HOME/command-line.log
echo "$ARGS" >>$MERLIN_HOME/command-line.log

echo $ARGS | xargs "$JAVA"

 
