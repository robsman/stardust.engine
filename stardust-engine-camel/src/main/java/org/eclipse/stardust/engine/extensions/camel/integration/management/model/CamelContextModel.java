package org.eclipse.stardust.engine.extensions.camel.integration.management.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;

import com.google.gson.annotations.Expose;

public class CamelContextModel {
	

	@Expose
	private String contextId;
	@Expose
	private boolean started;
	@Expose
	private boolean stopped;
	@Expose
	private String status = "";
	@Expose
	private String uptime;
	@Expose
	private String version;

	private ModelCamelContext camelContext;
	
	public CamelContextModel(String contextId, ModelCamelContext camelContext) {
		this.contextId = contextId;
		this.camelContext = camelContext;
		this.started = camelContext.getStatus().isStarted();
		this.stopped = camelContext.getStatus().isStopped();
		this.uptime = camelContext.getUptime();
		this.version = camelContext.getVersion();
		if (camelContext.getStatus().isStarted()){
			this.status = "Started";
		}else if (camelContext.getStatus().isStopped()){
			this.status = "Stopped";
		}else if (camelContext.getStatus().isSuspended()){
			this.status = "Suspended";
		}else {
			this.status = "";
		}
	
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getContextName() {
		return camelContext.getName();
	}


	public boolean isStarted() {
		return camelContext.getStatus().isStarted();
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean isStopped() {
		return camelContext.getStatus().isStopped();
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getUptime() {
		return camelContext.getUptime();
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getVersion() {
		return camelContext.getVersion();
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ModelCamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(ModelCamelContext camelContext) {
		this.camelContext = camelContext;
	}

	public void stop() {
		try {
			camelContext.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			camelContext.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<RouteCamelModel> getRoutes() {
		return toRouteCamelModel(this.camelContext);
	}
	
	public static List<RouteCamelModel> toRouteCamelModel(ModelCamelContext context) {
		List<RouteCamelModel> pojos = new ArrayList<RouteCamelModel>();
		if (context.getStatus().isStarted()){
			for (RouteDefinition route : context.getRouteDefinitions())
				pojos.add(new RouteCamelModel(context, route));
		}
		return pojos;
	}

}
