package org.eclipse.stardust.engine.extensions.camel.integration.management;

public interface IntegrationManagement {

	String contextsList();

	String allRoutesList(String contextId);

	void startRouteService(String contextId, String routeId);

	void stopRoute(String contextId, String routeId);

}
