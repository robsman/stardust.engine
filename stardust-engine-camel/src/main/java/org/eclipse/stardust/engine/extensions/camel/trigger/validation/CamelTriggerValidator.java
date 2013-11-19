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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.EndpointHelper;

public class CamelTriggerValidator implements TriggerValidator, TriggerValidatorEx
{

   private static final transient Logger logger = LogManager.getLogger(CamelTriggerValidator.class);

   private String ctu;
   private String ctp;

   @SuppressWarnings("rawtypes")
   public Collection validate(Map attributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Validates a trigger and returns the list of inconsistencies if they exist
    * 
    * @param triggrt
    *           a trigger
    * @return the list of inconsistencies
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public List validate(ITrigger trigger)
   {

      if (logger.isDebugEnabled())
      {
         logger.debug("Validating trigger: " + trigger.getName());
      }

      List inconsistencies = CollectionUtils.newList();

      String camelContextId = (String) trigger.getAttribute(CAMEL_CONTEXT_ID_ATT);

      // check for empty camel context ID.
      if (StringUtils.isEmpty(camelContextId))
      {
         inconsistencies.add(new Inconsistency("No camel context ID specified for trigger: " + trigger.getId(),
               trigger, Inconsistency.ERROR));
      }

      // check if route has been specified
      String routeDefinition = (String) trigger.getAttribute(ROUTE_EXT_ATT);

      if (StringUtils.isEmpty(routeDefinition))
      {
         inconsistencies.add(new Inconsistency("No route definition specified for trigger: " + trigger.getId(),
               trigger, Inconsistency.ERROR));
      }

      // check if route contains the "ipp:direct" directive
      if (!routeDefinition.contains("uri=\"" + CamelConstants.IPP_DIRECT_TAG + "\""))
      {
         inconsistencies.add(new Inconsistency("Missing entry uri=\"" + CamelConstants.IPP_DIRECT_TAG + "\"  in the "
               + "route definition for trigger: " + trigger.getId(), trigger, Inconsistency.ERROR));
      }

      if (!trigger.getAllPersistentAccessPoints().hasNext())
      {
         logger.warn("No Parameter mapping defined for trigger: " + trigger.getId());
         // inconsistencies.add(new Inconsistency(
         // "No Parameter mapping defined for trigger: "
         // + trigger.getId(), trigger, Inconsistency.WARNING));
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

         if (StringUtils.isEmpty(ctu) || StringUtils.isEmpty(ctp) || ctu.equals("${camelTriggerUsername}")
               || ctp.equals("${camelTriggerPassword}"))
         {
            inconsistencies.add(new Inconsistency("User ID/ Password is not set for " + trigger.getName(), trigger,
                  Inconsistency.ERROR));
         }
      }
      catch (NullPointerException e)
      {
         inconsistencies.add(new Inconsistency("User ID/ Password is not set for " + trigger.getName(), trigger,
               Inconsistency.ERROR));
      }

      return inconsistencies;
   }
}
