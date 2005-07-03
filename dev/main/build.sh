#!/bin/sh
#
# Utility script used when building a release.  The script is typically used
# as follows:
#
# $ build all 1234
#
# where the first argument is product group and the second (optional) argument is the
# build revision identifier. Allowable product group names include:
#
#  all      builds transit, util, magic, depot, metro
#  transit  build transit and util
#  magic    build magic
#  util     build util
#  depot    build depot
#  metro    build metro
#  test     build test
#  ci       builds luntbuild-clean, transit, magic, depot, metro, test

build()
{
    STATUS=99
    while [ "$STATUS" -eq 99 ] ; do
      if [ -n "$ID" ] ; then
        BUILD_ID="-Ddpml.release.signature=$ID"
      else
        BUILD_ID=""
      fi
      echo "building project with release ID [$ID]"
      ant $BUILD_ID "$@"
      STATUS="$?"
    done
    if [ "$STATUS" == 0 ] ; then
        echo ""
    else
        exit 1
    fi
}

transit()
{
    cd transit
    build clean install
    cd -
}

magic()
{
    cd magic
    build clean install
    cd -
}

util()
{
    cd util
    build clean install
    cd -
}

depot()
{
    cd depot
    build clean install
    cd -
}

metro()
{
    cd metro
    build clean install
    cd -
}

test()
{
    cd test
    build clean install
    cd -
}

luntbuildclean()
{
    build luntbuild-clean
}

ID=$2
TARGET=$1

if [ "$TARGET" == "help" ] ; then
    echo "Build one or more of the DPML products:"
    echo "  build [product] [revision]"
    echo "    [product] product name"
    echo "      transit   -- build the transit distribution"
    echo "      magic     -- build the magic distribution"
    echo "      util      -- build the util distribution"
    echo "      depot     -- build the depot distribution"
    echo "      metro     -- build the metro distribution"
    echo "      test      -- build the integration tests"
    echo "      all       -- builds transit, magic, metro distributions and integration tests"
    echo "      ci        -- build intended for contineous integration"
    echo "    [revision] optional build revision identified used when"
    echo "               building number releases."
    exit 1
fi

if [ "$TARGET" == "" ] ; then
    TARGET=main
fi

if [ "$TARGET" == "transit" ] ; then
    transit
fi

if [ "$TARGET" == "magic" ] ; then
    magic
fi

if [ "$TARGET" == "util" ] ; then
    util
fi

if [ "$TARGET" == "depot" ] ; then
    depot
fi

if [ "$TARGET" == "metro" ] ; then
    metro
fi

if [ "$TARGET" == "test" ] ; then
    test
fi

if [ "$TARGET" == "central" ] ; then
    central
fi

if [ "$TARGET" == "all" ] ; then
    transit
    magic
    util
    metro
    depot
    test
fi

if [ "$TARGET" == "ci" ] ; then
	luntbuildclean
	transit
	magic
	util
	metro
	depot
	test
	exit 0
fi
