#! /bin/sh
#

echo "DEPRECATED!!!"
echo "  use 'merlin' instead. This file will disappear in Merlin 4.0"
echo ""

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# Checking for JAVA_HOME is required on *nix due
# to some distributions stupidly including kaffe in /usr/bin
if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

# Checking for JAVA_HOME is required on *nix due
# to some distributions stupidly including kaffe in /usr/bin
if [ "$MERLIN_HOME" = "" ] ; then
  echo "ERROR: MERLIN_HOME not found in your environment."
  echo
  echo "Please, set the MERLIN_HOME variable in your environment to match the"
  echo "location of Merlin distribution."
  exit 1
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  MERLIN_HOME=`cygpath --unix "$MERLIN_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

JAVA=$JAVA_HOME/bin/java

# switch necessary paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  MERLIN_HOME=`cygpath --windows "$MERLIN_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

MERLIN_CLASSPATH=@UNIX-CLI-CLASSPATH@:$MERLIN_HOME/system/@UNIX-CLI-MAIN-PATH@

echo "Starting Merlin."
echo "================"
echo "      Security policy: $MERLIN_HOME/bin/security.policy"
echo "          JVM Options: $MERLIN_JVM_OPTS"
echo "        Bootstrap JAR: $MERLIN_CLASSPATH"
echo ""

"$JAVA" $MERLIN_JVM_OPTS "-Djava.security.policy=$MERLIN_HOME/bin/security.policy" -classpath "$MERLIN_CLASSPATH" org.apache.avalon.merlin.cli.Main "$@"
