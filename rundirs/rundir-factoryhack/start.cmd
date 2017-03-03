@ECHO OFF
REM Start OGEMA using felix as OSGi framework.
setlocal
set LAUNCHER=ogema-launcher.jar
set CONFIG=config/config.xml
set PROPERTIES=config/ogema.properties
REM set OPTIONS="%OPTIONS% -clean"

set EXTENSIONS=bin\ext;bin\ext\jssc-2.8.0.jar;bin\ext\libusb4java-1.2.0-linux-arm.jar;bin\ext\libusb4java-1.2.0-linux-x86.jar;bin\ext\libusb4java-1.2.0-linux-x86_64.jar;bin\ext\libusb4java-1.2.0-windows-x86.jar;bin\ext\libusb4java-1.2.0-windows-x86_64.jar;bin\ext\org.apache.commons.commons-lang3-3.3.2.jar;bin\ext\usb-api-osgi-1.0.2.jar;bin\ext\usb4java-1.2.0.jar;bin\ext\usb4java-javax-1.2.0.jar
set OGEMA_CLASSPATH=%LAUNCHER%;%EXTENSIONS%
set VMOPTS=%VMOPTS% -cp %OGEMA_CLASSPATH%

java %VMOPTS% org.ogema.launcher.OgemaLauncher ^
 --config %CONFIG% --properties %PROPERTIES% ^
 %OPTIONS% %*
endlocal
