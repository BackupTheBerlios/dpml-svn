#! /bin/sh
#
# -----------------------------------------------------------------------------
# Merlin start script. Based on the Avalon-Phoenix start script.
#
# Author: Alexis Agahi <alag@users.sourceforge.net>
#         Peter Donald <peter at apache.org>
#         Leo Simons <leosimons at apache.org>
#
# Environment Variable Prequisites
#
#   MERLIN_OPTS       (Optional) Java runtime options used when the command is
#                      executed.
#
#   JAVA_HOME          Must point at your Java Development Kit installation.
#
#   MERLIN_JVM_OPTS   (Optional) Java runtime options used when the command is
#                       executed.
#
# -----------------------------------------------------------------------------


usage()
{
    echo "Usage: $0 {start|stop|run|restart|check}"
    exit 1
}

[ $# -gt 0 ] || usage

##################################################
# Get the action & configs
##################################################

ACTION=$1
shift
ARGS="$@"



# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# resolve links - $0 may be a softlink
THIS_PROG="$0"

while [ -h "$THIS_PROG" ]; do
  ls=`ls -ld "$THIS_PROG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    THIS_PROG="$link"
  else
    THIS_PROG=`dirname "$THIS_PROG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$THIS_PROG"`
MERLIN_HOME=`cd "$PRGDIR/.." ; pwd`

unset THIS_PROG

# For Cygwin this script does not make too much sense, since there is
# no native JVM available.
if $cygwin; then
  echo "This script is used to install Merlin as \*nix service."
  echo "Please use $PRGDIR/nt/InstallService-NT.bat to do so for Windows."
  exit 1
fi

if [ -r "$MERLIN_HOME"/bin/setenv.sh ]; then
  . "$MERLIN_HOME"/bin/setenv.sh
fi

if [ -z "$MERLIN_TMPDIR" ] ; then
  MERLIN_TMPDIR=$MERLIN_HOME/tmp
fi

if [ ! -d "$MERLIN_TMPDIR" ] ; then
  mkdir "$MERLIN_TMPDIR" 
fi

# Checking for JAVA_HOME is required on *nix due
# to some distributions stupidly including kaffe in /usr/bin
if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

# ----- Execute The Requested Command -----------------------------------------


# Uncomment to get enable remote debugging
# DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
#
# Command to overide JVM ext dir 
# 
# This is needed as some JVM vendors do foolish things 
# like placing jaxp/jaas/xml-parser jars in ext dir 
# thus breaking Merlin 
# 

JVM_EXT_DIRS="$MERLIN_HOME/ext" 
JVM_OPTS="-Djava.security.policy=$MERLIN_HOME/bin/security.policy " 
MERLIN_CLASSPATH=@UNIX-CLI-CLASSPATH@:$MERLIN_HOME/system/@UNIX-CLI-MAIN-PATH@

# Get the run cmd
RUN_CMD="$JAVA_HOME/bin/java $JVM_OPTS $DEBUG $MERLIN_JVM_OPTS -classpath $MERLIN_CLASSPATH org.apache.avalon.merlin.cli.Main $ARGS"

echo "Using MERLIN_HOME:   $MERLIN_HOME"
echo "Using JAVA_HOME:     $JAVA_HOME"
echo "Using RUN_CMD:       $RUN_CMD"

#####################################################
# Find a PID for the pid file
#####################################################
if [  -z "$MERLIN_PID" ]
then
  MERLIN_PID="$MERLIN_TMPDIR/merlin.pid"
fi

#####################################################
# Find a location for the merlin console
#####################################################
MERLIN_CONSOLE="$MERLIN_TMPDIR/merlin.console"
if [  -z "$MERLIN_CONSOLE" ]
then
  if [ -w /dev/console ]
  then
    MERLIN_CONSOLE=/dev/console
  else
    MERLIN_CONSOLE=/dev/tty
  fi
fi


#####################################################
# Action!
#####################################################

case "$ACTION" in
  start)
        echo "Starting Merlin: "

        if [ -f $MERLIN_PID ]
        then
            if ps -p `cat $MERLIN_PID` >/dev/null 2>/dev/null
            then
                echo "Already Running!!"
                exit 1
            fi

        fi

        echo "STARTED Merlin `date`" >> $MERLIN_CONSOLE

        nohup sh -c "exec $RUN_CMD >>$MERLIN_CONSOLE 2>&1" >/dev/null &
        echo $! > $MERLIN_PID
        echo "Merlin running pid="`cat $MERLIN_PID`
        ;;

  stop)
        PID=`cat $MERLIN_PID 2>/dev/null`
        echo "Shutting down Merlin: $PID"
        kill $PID 2>/dev/null
        sleep 2
        kill -9 $PID 2>/dev/null
        rm -f $MERLIN_PID
        echo "STOPPED `date`" >>$MERLIN_CONSOLE
        ;;

  restart)
        $0 stop $ARGS
        sleep 5
        $0 start $ARGS
        ;;

  supervise)
       #
       # Under control of daemontools supervise monitor which
       # handles restarts and shutdowns via the svc program.
       #
         echo "This command is not implemented yet."
         ;;

  run|demo)
        echo "Running Merlin: "
        echo " "

        if [ -f $MERLIN_PID ]
        then
            echo "Already Running!!"
            exit 1
        fi

        exec $RUN_CMD
        ;;

  check)
        echo "Checking arguments to Merlin: "
        echo "MERLIN_HOME:      $MERLIN_HOME"
        echo "MERLIN_TMPDIR:    $MERLIN_TMPDIR"
        echo "MERLIN_JVM_OPTS:  $MERLIN_JVM_OPTS"
        echo "JAVA_HOME:        $JAVA_HOME"
        echo "JVM_OPTS:         $JVM_OPTS"
        echo "MERLIN_CLASSPATH: $MERLIN_CLASSPATH"
        echo "RUN_CMD:          $RUN_CMD"
        echo

        if [ -f $MERLIN_PID ]
        then
            echo "Merlin running pid="`cat $MERLIN_PID`
            exit 0
        fi
        exit 1
        ;;

*)
        usage
        ;;
esac

exit 0



