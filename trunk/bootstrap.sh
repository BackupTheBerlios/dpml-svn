#!/bin/sh
# DPML system bootstrap scrip.
#

executeAnt()
{
    STATUS=99
    while [ "$STATUS" -eq 99 ] ; do
      ant -Ddpml.signature=BOOTSTRAP "$@"
      STATUS="$?"
    done
    if [ "$STATUS" == 0 ] ; then
        echo ""
    else
        exit 1
    fi
}

dpmlLibrary()
{
    cd main/depot/library
    executeAnt -f bootstrap.xml clean install
    cd ../../..
}

dpmlPart()
{
    cd main/metro/part
    executeAnt -f bootstrap.xml clean install
    cd ../../..
}

dpmlTransit()
{
    cd main/transit/core
    executeAnt -f bootstrap.xml clean install
    cd ../../..   
}

dpmlCli()
{
    cd main/util/cli
    executeAnt -f bootstrap.xml clean install
    cd ../../..   
}

dpmlBuild()
{
    cd main/depot/build
    executeAnt -f bootstrap.xml clean install
    cd ../../..   
}

dpmlTools()
{
    cd main/depot/tools
    executeAnt -f bootstrap.xml clean install
    cd ../../..   
}

dpmlExternal()
{
    cd main/external
    bash ../depot/build/target/bin/build clean install
    cd ../..
}

source setup.sh
dpmlTransit
dpmlPart
dpmlLibrary
dpmlCli
dpmlBuild
dpmlTools
dpmlExternal

echo "BOOTSTRAP SUCCESSFUL"


