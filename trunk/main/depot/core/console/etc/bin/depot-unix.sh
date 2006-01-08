#!/bin/sh
#
# Copyright 2004 Niclas Hedhman
# Copyright 2005 Stephen McConnell
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

TITLE="Starting Depot $DEPOT_VERSION."
echo $TITLE
echo "$TITLE" | sed 's/./=/g'
echo "             Platform: $PLATFORM"
echo "            Java Home: $JAVA_HOME"
echo "          DPML System: $DPML_SYSTEM"
echo "            DPML Home: $DPML_HOME"
echo "      Security Policy: $DEPOT_SECURITY_POLICY"
echo "          JVM Options: $DEPOT_JVM_OPTS"
echo "            Classpath: $DEPOT_CLASSPATH"
echo "      Depot Arguments: $DEPOT_ARGS"
echo ""

JAVA="$JAVA_HOME/bin/java"

ARGS="$DEPOT_JVM_OPTS -classpath \"$DEPOT_CLASSPATH\" @DEPOT-MAIN-CLASS@ $DEPOT_ARGS"

echo $ARGS | xargs "$JAVA"
