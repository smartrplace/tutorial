#!/bin/bash

#
# Start OGEMA with activated security and Equinox as OSGi framework.
#

OGEMA_OPTIONS="$OPTIONS --use-rundir-only"

OGEMA_CONFIG=config/config.xml

VMOPTS="-Dorg.osgi.framework.security=osgi -Djava.security.policy=config/all.policy -Dorg.ogema.security=on"

source start.sh --security config/ogema.policy $*
