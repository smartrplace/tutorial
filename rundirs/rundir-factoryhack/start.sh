#!/bin/bash

#
# Start OGEMA with default configuration (Equinox OSGi and no security)
#

LAUNCHER=${OGEMA_LAUNCHER:-./ogema-launcher.jar}
CONFIG=${OGEMA_CONFIG:-config/config.xml}
PROPERTIES=${OGEMA_PROPERTIES:-config/ogema.properties}
OPTIONS=$OGEMA_OPTIONS
JAVA=${JAVA_HOME:+${JAVA_HOME}/bin/}java
EXTENSIONS=bin/ext$(find bin/ext/ -iname "*jar" -printf :%p)
VMOPTS="$VMOPTS -cp $LAUNCHER:$EXTENSIONS"

if [[ " $@ " =~ " --verbose " ]]; then
VMOPTS="$VMOPTS -Dequinox.ds.debug=true -Dequinox.ds.print=true"
echo $JAVA $VMOPTS org.ogema.launcher.OgemaLauncher --config $CONFIG --properties $PROPERTIES $OPTIONS $*
fi

$JAVA $VMOPTS org.ogema.launcher.OgemaLauncher --config $CONFIG --properties $PROPERTIES $OPTIONS $*
