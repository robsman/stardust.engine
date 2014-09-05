package org.eclipse.stardust.engine.extensions.camel.integration.management.model;

import java.util.List;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.engine.extensions.camel.util.IntegrationManagementUtils;

import com.google.gson.annotations.Expose;

public class CamelContextModel {
	

	@Expose
	private String contextId;
	@Expose
	private boolean started;
	@Expose
	private boolean stopped;
	@Expose
	private String uptime;
	@Expose
	private String version;

	private CamelContext camelContext;
	
	public CamelContextModel(String contextId, CamelContext camelContext) {
		this.contextId = contextId;
		this.camelContext = camelContext;
		this.started = camelContext.getStatus().isStarted();
		this.stopped = camelContext.getStatus().isStopped();
		this.uptime = camelContext.getUptime();
		this.version = camelContext.getVersion();
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

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public void setCamelContext(CamelContext camelContext) {
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
		return IntegrationManagementUtils.toRouteCamelModel(this.camelContext);
	}

}
