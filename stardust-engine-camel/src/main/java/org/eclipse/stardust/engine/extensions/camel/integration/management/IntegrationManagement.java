package org.eclipse.stardust.engine.extensions.camel.integration.management;

public interface IntegrationManagement {

	String contextsList();

	String allRoutesList(String contextId);
	
	void startAllRoutes(String contextId);
	
	void stopAllRoutes(String contextId);
	
	void startCamelContext(String contextId);

	void stopCamelContext(String contextId);

	String getProducerRoutesList(String contextId);

	String getConsumerRoutesList(String contextId);

	String getTriggerConsumerRoutesList(String contextId);

	String getApplicationConsumerRoutesList(String contextId);

	void startRouteService(String contextId, String routeId);

	void stopRoute(String contextId, String routeId);

}
