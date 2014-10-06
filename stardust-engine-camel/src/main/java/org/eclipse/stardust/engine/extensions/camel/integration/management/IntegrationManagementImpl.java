package org.eclipse.stardust.engine.extensions.camel.integration.management;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.startRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopRunningRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.util.CastUtils;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.CamelContextModel;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.RouteCamelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IntegrationManagementImpl implements IntegrationManagement, ApplicationContextAware{

	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationManagementImpl.class);
	private ApplicationContext applicationContext;

	private List<CamelContextModel> initCamelContextModel() {
		List<CamelContextModel> contexts = new ArrayList<CamelContextModel>();
		Map<String, ModelCamelContext> beansOfType;
		beansOfType = CastUtils.cast(applicationContext.getBeansOfType(ModelCamelContext.class));
		for (ModelCamelContext context : beansOfType.values()) {
			contexts.add(new CamelContextModel(context.getName(), context));
		}
		return contexts;
	}

	public String contextsList() {
		List<CamelContextModel> contexts =initCamelContextModel();
		return convertObjectToJsonString(contexts);
	}

	public String allRoutesList(String contextId) {
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				routeCamelModelList.add(data);
			}
		}
		return convertObjectToJsonString(routeCamelModelList);
	}
	
	public void startCamelContext(String contextId) {
		List<CamelContextModel> contexts = initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		ccm.start();
	}
	
	public void stopCamelContext(String contextId) {
		List<CamelContextModel> contexts = initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		ccm.stop();
	}
	
	public String getProducerRoutesList(String contextId) {
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				if (data.getId().startsWith("Producer")){
					routeCamelModelList.add(data);
				}
			}
		}
		return convertObjectToJsonString(routeCamelModelList);
	}
	
	public String getTriggerConsumerRoutesList(String contextId) {

		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				if (data.getId().startsWith("Consumer-")){
					routeCamelModelList.add(data);
				}
			}
		}
		return convertObjectToJsonString(routeCamelModelList);	
		}
	
	public String getApplicationConsumerRoutesList(String contextId) {
		
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				if ((data.getId().startsWith("Consumer") && (!data.getId().startsWith("Consumer-")))){
					routeCamelModelList.add(data);
				}
			}
		}
		return convertObjectToJsonString(routeCamelModelList);

	}
	
	public void startRouteService( String contextId, String routeId) {
		LOGGER.info("--> Starting route : " + routeId);
		ModelCamelContext camelcontext=(ModelCamelContext) applicationContext.getBean(contextId);
		try {
			startRoute(camelcontext, routeId);
		} catch (Exception e) {
			LOGGER.warn("route '" + routeId
					+ "' doesn't exist in camel context '" + contextId + "'");
		}
	}


	public void stopRoute( String contextId, String routeId) {
		LOGGER.info("--> Stopping route : " + routeId);
		ModelCamelContext camelContext=(ModelCamelContext) applicationContext.getBean(contextId);
		stopRunningRoute(camelContext, routeId);
	}
	
	private String convertObjectToJsonString(Object obj) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		return gson.toJson(obj);
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public static CamelContextModel searchCamelContextModel(
			List<CamelContextModel> context, String idCamelContext) {

		for (int i = 0; i < context.size(); i++) {
			if (idCamelContext.equalsIgnoreCase(context.get(i).getContextId())) {
				return context.get(i);
			}
		}
		return null;
	}
}
