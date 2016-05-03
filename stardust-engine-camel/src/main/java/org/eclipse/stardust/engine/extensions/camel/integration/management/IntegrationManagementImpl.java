package org.eclipse.stardust.engine.extensions.camel.integration.management;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.startRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopRunningRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.util.CastUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.CamelContextModel;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.RouteCamelModel;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IntegrationManagementImpl implements IntegrationManagement, ApplicationContextAware{

   private static final Logger logger = LogManager.getLogger(IntegrationManagementImpl.class);
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
	
	// start all routes
	public void startAllRoutes(String contextId) {
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				startRouteService(contextId, data.getId()); 
			}
		}	
	}
	
	// start all routes
	public void stopAllRoutes(String contextId) {
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				ModelCamelContext camelContext=ccm.getCamelContext();
				
				try {
					camelContext.stopRoute(data.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}	
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
	
	public String getConsumerRoutesList(String contextId) {
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				if (data.getId().startsWith("Consumer")){
					routeCamelModelList.add(data);
				}
			}
		}
		return convertObjectToJsonString(routeCamelModelList);
	}

	public String getOtherRoutesList(String contextId) {
		
		List<CamelContextModel> contexts =initCamelContextModel();
		CamelContextModel ccm = searchCamelContextModel(contexts,contextId);
		List<RouteCamelModel> routeCamelModelList = new ArrayList<RouteCamelModel>();
		if (ccm != null) {
			for (RouteCamelModel data : ccm.getRoutes()) {
				if ((!data.getId().startsWith("Consumer") && (!data.getId().startsWith("Producer")))){
					routeCamelModelList.add(data);
				}
			}
		}
		return convertObjectToJsonString(routeCamelModelList);
	}

	public void startRouteService( String contextId, String routeId) {
	   if(logger.isDebugEnabled())
	      logger.debug("--> Starting route : " + routeId);
		ModelCamelContext camelcontext=(ModelCamelContext) applicationContext.getBean(contextId);
		try {
			startRoute(camelcontext, routeId);
		} catch (Exception e) {
		   logger.error("An error occured while starting route'" + routeId
					+ "' from camel context '" + contextId + "'", e);
		}
	}


	public void stopRoute( String contextId, String routeId) {
	   if(logger.isDebugEnabled())
	      logger.debug("--> Stopping route : " + routeId);
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
