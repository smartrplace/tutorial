@ECHO OFF
setlocal
REM Start OGEMA with activated security

set POLICY=config\all.policy
set VMOPTS=%VMOPTS% -Dorg.osgi.framework.security=osgi -Djava.security.policy=%POLICY% -Dorg.ogema.security=on

start.cmd --security config\ogema.policy --use-rundir-only %*
endlocal