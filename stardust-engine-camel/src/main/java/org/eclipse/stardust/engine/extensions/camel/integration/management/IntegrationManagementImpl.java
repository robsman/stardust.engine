package org.eclipse.stardust.engine.extensions.camel.integration.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.util.CastUtils;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.CamelContextModel;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.RouteCamelModel;
import org.eclipse.stardust.engine.extensions.camel.util.IntegrationManagementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IntegrationManagementImpl implements IntegrationManagement, ApplicationContextAware{

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationManagementImpl.class);
	List<CamelContextModel> contexts;
	ApplicationContext applicationContext;

	private void initCamelContextModel() {
		contexts = new ArrayList<CamelContextModel>();
		Map<String, CamelContext> beansOfType;
		beansOfType = CastUtils.cast(applicationContext.getBeansOfType(CamelContext.class));
		for (CamelContext context : beansOfType.values()) {
			contexts.add(new CamelContextModel(context.getName(), context));
		}
	}

	public String contextsList() {
		initCamelContextModel();
		return convertObjectToJsonString(contexts);
	}

	public String allRoutesList(String contextId) {
		initCamelContextModel();
		CamelContextModel ccm = IntegrationManagementUtils.searchCamelContextModel(contexts,
				contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				routeCamelModelList.add(data);
			}
		}
		return convertObjectToJsonString(routeCamelModelList);
	}

	public void startRouteService( String contextId, String routeId) {
		initCamelContextModel();
		LOGGER.info("--> Starting route : " + routeId);
		CamelContextModel ccm = IntegrationManagementUtils.searchCamelContextModel(contexts,
				contextId);
		if ((ccm != null) && (ccm.getCamelContext().getRouteDefinition(routeId)!= null)) {
			ccm.getCamelContext().getRouteDefinition(routeId);
			new RouteCamelModel(ccm.getCamelContext(), ccm.getCamelContext()
					.getRouteDefinition(routeId)).startRoute();
		} else {
			LOGGER.warn("route '" + routeId
					+ "' doesn't exist in camel context '" + contextId + "'");
		}
	}


	public void stopRoute( String contextId, String routeId) {
		initCamelContextModel();
		LOGGER.info("--> Stopping route : " + routeId);
		CamelContextModel ccm = IntegrationManagementUtils.searchCamelContextModel(contexts,
				contextId);
		if ((ccm != null) && (ccm.getCamelContext().getRouteDefinition(routeId)!= null)) {
			ccm.getCamelContext().getRouteDefinition(routeId);
			new RouteCamelModel(ccm.getCamelContext(), ccm.getCamelContext()
					.getRouteDefinition(routeId)).stopRoute();
		} else {
			LOGGER.warn("route '" + routeId
					+ "' doesn't exist in camel context '" + contextId + "'");
		}

	}
	
	private String convertObjectToJsonString(Object obj) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		return gson.toJson(obj);
	}
	
	public List<CamelContextModel> getContexts() {
		return contexts;
	}

	public void setContexts(List<CamelContextModel> contexts) {
		this.contexts = contexts;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		initCamelContextModel();
	}
}
