#!/bin/bash
### BEGIN INIT INFO
# Provides:          tunnels
# Required-Start:    $remote_fs $syslog $all
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: SSH tunnels for OGEMA gateway
# Description:       SSH tunnels for OGEMA gateway
### END INIT INFO
 
# run autossh as this user
runas=pi
 
PATH=$PATH:/usr/local/bin
# First port: 22192
PORT=22194
ID=user
VNZ=88.198.127.166
 
case "$1" in
  start)
    AUTOSSH_POLL=120
    AUTOSSH_GATETIME=30
    AUTOSSH_LOGLEVEL=7
    AUTOSSH_LOGFILE=/var/log/autossh.log
    #export AUTOSSH_POLL AUTOSSH_LOGFILE AUTOSSH_LOGLEVEL AUTOSSH_GATETIME
    sudo -u $runas autossh -2 -fN -M 0 -o ServerAliveInterval=300 -R $PORT:localhost:8080 ${ID}@${VNZ}
    echo "Started tunnel to $VNZ, remote server port $PORT"
    ;;
  stop)
    echo "Closing tunnel"
    sudo -u $runas pkill -9 autossh
    ;;
  *)
    echo "Usage: " $(readlink -f $0) "{start|stop}"
    exit 1
    ;;
  esac
   
exit 0
