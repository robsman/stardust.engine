package org.eclipse.stardust.engine.extensions.camel.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.CamelContextModel;
import org.eclipse.stardust.engine.extensions.camel.integration.management.model.RouteCamelModel;

public class IntegrationManagementUtils {

	public static List<RouteCamelModel> toRouteCamelModel(CamelContext context) {
		List<RouteCamelModel> pojos = new ArrayList<RouteCamelModel>();
		for (RouteDefinition route : context.getRouteDefinitions())
			pojos.add(new RouteCamelModel(context, route));
		return pojos;
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
