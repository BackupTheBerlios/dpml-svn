#!/bin/sh
#
# Copyright 2004 Niclas Hedhman
# Copyright 2005-2007 Stephen J. McConnell
#
# Licensed  under the  Apache License,  Version 2.0  (the "License");
# you may not use  this file  except in  compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed  under the  License is distributed on an "AS IS" BASIS,
# WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
# implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.

DEPOT_JVM_OPTS="\"-Djava.security.policy=$DEPOT_SECURITY_POLICY\" -Djava.endorsed.dirs=$DEPOT_ENDORSED_DIRECTORY $DEPOT_JVM_OPTS"

if [ "$DEPOT_DEBUG" = "true" ]; then
    TITLE="Starting Depot $DEPOT_VERSION."
    echo $TITLE
    echo "$TITLE" | sed 's/./=/g'
    echo "             Platform: $PLATFORM"
    echo "            Java Home: $JAVA_HOME"
    echo "          DPML System: $DPML_SYSTEM"
    echo "            DPML Home: $DPML_HOME"
    echo "      Security Policy: $DEPOT_SECURITY_POLICY"
    echo "   Endorced Directory: $DEPOT_ENDORSED_DIRECTORY"
    echo "          JVM Options: $DEPOT_JVM_OPTS"
    echo "            Classpath: $DEPOT_CLASSPATH"
    echo "      Depot Arguments: $DEPOT_ARGS"
    echo ""
fi

JAVA="$JAVA_HOME/bin/java"

ARGS="$DEPOT_JVM_OPTS -classpath \"$DEPOT_CLASSPATH\" @DEPOT-MAIN-CLASS@ $DEPOT_ARGS"

echo $ARGS | xargs "$JAVA"
