/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.camel.trigger.validation;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CAMEL_CONTEXT_ID_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ROUTE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.Util.getModelId;
import static org.eclipse.stardust.engine.extensions.camel.Util.getProcessId;
import static org.eclipse.stardust.engine.extensions.camel.Util.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.stopAndRemoveRunningRoute;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.removeRouteDefinitionWithoutRunningRoute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Route;
import org.apache.camel.model.ModelCamelContext;
import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.EndpointHelper;
import org.eclipse.stardust.engine.extensions.camel.trigger.CamelTriggerLoader;
import org.eclipse.stardust.engine.extensions.camel.util.CreateTriggerRouteAction;

import org.springframework.context.support.AbstractApplicationContext;

public class CamelTriggerValidator implements TriggerValidator,
        TriggerValidatorEx {

    private static final transient Logger logger = LogManager
            .getLogger(CamelTriggerValidator.class);

    final String PRP_APPLICATION_CONTEXT = "org.eclipse.stardust.engine.api.spring.applicationContext";

//  private ITrigger trigger;

    private String ctu;
    private String ctp;
    private  ModelCamelContext camelContext;
    @SuppressWarnings("rawtypes")
    public Collection validate(Map attributes, Iterator accessPoints) {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates a trigger and returns the list of inconsistencies if they exist
     * 
     * @param triggrt a trigger
     * @return the list of inconsistencies
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List validate(ITrigger trigger) {

        BpmRuntimeEnvironment bpmRt = PropertyLayerProviderInterceptor
                .getCurrent();
        
        if (logger.isDebugEnabled()) {
            logger.debug("Validating trigger: " + trigger.getName());
        }

        List inconsistencies = CollectionUtils.newList();

        String camelContextId = (String) trigger
                .getAttribute(CAMEL_CONTEXT_ID_ATT);

        // check for empty camel context ID.
        if (StringUtils.isEmpty(camelContextId)) {
            inconsistencies.add(new Inconsistency(
                    "No camel context ID specified for trigger: "
                            + trigger.getId(), trigger, Inconsistency.ERROR));
        }

        // check if route has been specified
        String routeDefinition = (String) trigger.getAttribute(ROUTE_EXT_ATT);

        if (StringUtils.isEmpty(routeDefinition)) {
            inconsistencies.add(new Inconsistency(
                    "No route definition specified for trigger: "
                            + trigger.getId(), trigger, Inconsistency.ERROR));
        }
        
        // check if route contains the "ipp:direct" directive
        if(!routeDefinition.contains("uri=\"" + CamelConstants.IPP_DIRECT_TAG + "\"")) {
             inconsistencies.add(new Inconsistency(
                     "Missing entry uri=\"" + CamelConstants.IPP_DIRECT_TAG + "\"  in the " +
                            "route definition for trigger: "
                             + trigger.getId(), trigger, Inconsistency.ERROR));
        }
        
        if (!trigger.getAllPersistentAccessPoints().hasNext()) {
                 logger.warn("No Parameter mapping defined for trigger: " + trigger.getId());
//          inconsistencies.add(new Inconsistency(
//                  "No Parameter mapping defined for trigger: "
//                          + trigger.getId(), trigger, Inconsistency.WARNING));
        }

        try
        {
            Object ctuObj = trigger.getAttribute("carnot:engine:camel::username");
            Object ctpObj = trigger.getAttribute("carnot:engine:camel::password");
            
            if (ctuObj != null && ctpObj != null)
            {
                this.ctu = EndpointHelper.sanitizeUri((String) ctuObj);
                this.ctp = EndpointHelper.sanitizeUri((String) ctpObj);
            }
    
            if (StringUtils.isEmpty(ctu) || StringUtils.isEmpty(ctp)
                    || ctu.equals("${camelTriggerUsername}")
                    || ctp.equals("${camelTriggerPassword:Password}")) {
                inconsistencies.add(new Inconsistency(
                        "User ID/ Password is not set for " + trigger.getName(),
                        trigger, Inconsistency.ERROR));
            }
        }
        catch(NullPointerException e)
        {
           logger.error("User ID/ Password is not set for " + trigger.getName(),e);
            inconsistencies.add(new Inconsistency(
                  "User ID/ Password is not set for " + trigger.getName(),
                    trigger, Inconsistency.ERROR));
        }
        String partitionId = SecurityProperties.getPartition().getId();
        String routeId=getRouteId(partitionId, getModelId(trigger), getProcessId(trigger), trigger.getId(), false);
        if (inconsistencies.isEmpty()) {
            
            if (logger.isDebugEnabled()) {
                logger.debug("No inconsistencies found for trigger: " + trigger.getName());
            }

            try {

                AbstractApplicationContext applicationContext = (AbstractApplicationContext) Parameters
                        .instance().get(PRP_APPLICATION_CONTEXT);

                if (applicationContext != null && bpmRt != null && bpmRt.getModelManager() != null)
                {

	                IModel model = (IModel) trigger.getModel();

	                IModel activeModel = bpmRt.getModelManager().findActiveModel(model.getId());

	                // only start the contained routes if this model (the one being validated) is
	                // intended to be active (aka it has the same model OID as the currently
	                // active model with the same ID or is the first version to be deployed (model
	                // OID is 0))
	                if (model.getModelOID() == 0 || model.getModelOID() == activeModel.getModelOID())
	                {
	                    camelContext = (ModelCamelContext) applicationContext
	                            .getBean(camelContextId);

	                    if (logger.isDebugEnabled()) {
	                        logger.debug("Camel Context " + camelContextId
	                                + " used.");
	                    }

	                    List<Route> routesToBeStopped = new ArrayList<Route>();

	                    // select routes that are running in the current partition
	                    for (Route runningRoute : camelContext.getRoutes()) {
	                        if (runningRoute.getId().startsWith(routeId)) {
	                            routesToBeStopped.add(runningRoute);
	                        }
	                    }

	                    // stop running routes to sync up with the deployed model
	                    for (Route runningRoute : routesToBeStopped) {

	                       stopAndRemoveRunningRoute(camelContext, runningRoute.getId());

	                        if (logger.isDebugEnabled()) {
	                            logger.debug("Route " + runningRoute.getId()
	                                    + " is removed from context "
	                                    + camelContext + ".");
	                        }
	                    }

	                    CamelTriggerLoader camelTriggerLoader = (CamelTriggerLoader) applicationContext
	                            .getBean("camelTriggerLoader");

	                    Action<?> action = new CreateTriggerRouteAction(bpmRt,
	                            partitionId, applicationContext, camelTriggerLoader.getDataConverters(), trigger);

	                    action.execute();

	                }
                }
            } catch (Exception e) {
             //using e.getCause() since e is RTE thrown by the Action class
               try{
               removeRouteDefinitionWithoutRunningRoute(camelContext,routeId );
               }
               catch (Exception e1)
               {
                  //throw new RuntimeException(e);
                  inconsistencies.add(new Inconsistency(e1.getCause().getMessage(), trigger, Inconsistency.ERROR));
               }
               
               
               inconsistencies.add(new Inconsistency(e.getCause().getMessage(), trigger, Inconsistency.ERROR));
            }
        }

        return inconsistencies;
    }
}
