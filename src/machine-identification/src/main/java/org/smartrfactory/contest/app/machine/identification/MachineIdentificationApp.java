package org.smartrfactory.contest.app.machine.identification;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.ogema.core.application.Application;
import org.ogema.core.application.ApplicationManager;
import org.ogema.core.model.Resource;
import org.ogema.model.metering.ElectricityMeter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartrfactory.contest.app.machine.identification.algo.MatchingAlgorithm;
import org.smartrfactory.contest.app.machine.identification.algo.impl.MatchingAlgorithm1;

@Component(specVersion = "1.2", immediate = true)
@Service(Application.class)
public class MachineIdentificationApp implements Application {
	
	public final static Logger logger = LoggerFactory.getLogger(MachineIdentificationApp.class);
	private final static MatchingAlgorithm algo = new MatchingAlgorithm1();
	private static final String servletPath = "/org/smartrfactory/app/identificiation";
	// synchronized on itself
	private final Map<ElectricityMeter, MeterController> controllers = new HashMap<>();
	private ApplicationManager am;
	private ExecutorService exec;
	private ServiceRegistration<ShellCommands> shell;
	private HttpServlet servlet;
	
	@Reference
	HttpService http;

    /*
     * This is the entry point to the application.
     */
 	@Override
    public void start(ApplicationManager appManager) {
 		this.am = appManager;
 		this.exec = Executors.newFixedThreadPool(3);
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("osgi.command.scope", "factory");
		props.put("osgi.command.function", new String[] { "startIdentification", "identificationResults" });
		shell = ctx.registerService(ShellCommands.class, new ShellCommands(this), props);
		servlet = new IdentificationServlet(this);
		try {
			http.registerServlet(servletPath, servlet, null, null);
		} catch (ServletException | NamespaceException e) {
			throw new RuntimeException(e);
		}
     }

     /*
     * Callback called when the application is going to be stopped.
     */
    @Override
    public void stop(AppStopReason reason) {
    	if (servlet != null) {
    		try {
    			http.unregister(servletPath);
    		} catch (Exception e) {}
    	}
    	servlet = null;
    	synchronized (controllers) {
    		controllers.clear();
    	}
    	if (shell != null)
    		shell.unregister();
    	shell = null;
    	am = null;
    }
    
    String startIdentification(String meterId) {
    	final ApplicationManager am = this.am;
    	if (am == null)
    		return "App not active";
    	Resource resource = am.getResourceAccess().getResource(meterId);
    	if (!(resource instanceof ElectricityMeter))
    		return "Invalid meter id";
    	final ElectricityMeter meter  = (ElectricityMeter) resource;
    	final MeterController controller = getOrCreateController(meter);
    	if (controller.isRunning()) 
    		return "Identification is already running";
    	return controller.start();
    }
    
    private MeterController getOrCreateController(ElectricityMeter meterIn) {
    	final ElectricityMeter meter = meterIn.getLocationResource();
    	synchronized (controllers) {
    		MeterController c = controllers.get(meter);
    		if (c == null) {
    			c = new MeterController(meter,am,exec, algo);
    			controllers.put(meter, c);
    		}
    		return c;
    	}
    }
    
    MeterController getController(String meterIn) {
    	ElectricityMeter meter = am.getResourceAccess().getResource(meterIn);
    	if (meter == null)
    		return null;
    	meter= meter.getLocationResource();
    	synchronized (controllers) {
    		MeterController c = controllers.get(meter);
    		return c;
    	}
    }
    
}
