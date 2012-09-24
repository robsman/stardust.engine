/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.rest.interactions;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isDmsType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isStructuredType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalDmsValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalPrimitiveValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalStructValue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.ws.ParametersXto;



public class InteractionDataFlowUtils
{
   private static final Logger trace = LogManager.getLogger(InteractionDataFlowUtils.class);

   /**
    * For a transition period only: support data mappings targeting the full parameter as
    * well.
    */
   public static final String PRP_SUPPORT_DATA_MAPPING_IDS = "Carnot.Compatibility.Interactions.SupportDataMappingIds";

   public static boolean supportDataMappingIds()
   {
      return Parameters.instance().getBoolean(PRP_SUPPORT_DATA_MAPPING_IDS, false);
   }

   public static void marshalInteractionInDataValues(Model model, ApplicationContext context,
         Map<String, ? extends Serializable> params, ParametersXto res)
   {
      if ((null != context) && (null != model) && (null != params))
      {
         @SuppressWarnings("unchecked")
         List<AccessPoint> accessPoints = context.getAllAccessPoints();

         for (AccessPoint ap : accessPoints)
         {
            if ((Direction.IN == ap.getDirection()) && params.containsKey(ap.getId()))
            {
               if (isPrimitiveType(model, ap))
               {
                  res.getParameter().add(marshalPrimitiveValue(model, ap, params.get(ap.getId())));
               }
               else if (isStructuredType(model, ap))
               {
                  res.getParameter().add(marshalStructValue(model, ap, params.get(ap.getId())));
               }
               else
               {
                  trace.warn("Access point of unsupported type: " + ap.getId());
               }
            }
         }

         // legacy support
         if (supportDataMappingIds())
         {
            for (DataMapping dm : (List<DataMapping>) context.getAllInDataMappings())
            {
               if (isEmpty(dm.getApplicationPath()))
               {
                  String paramId = dm.getApplicationAccessPoint().getId();
                  if ( !CompareHelper.areEqual(dm.getId(), paramId))
                  {
                     if (isPrimitiveType(model, dm))
                     {
                        res.getParameter().add(marshalPrimitiveValue(model, dm, params.get(paramId)));
                     }
                     else if (isStructuredType(model, dm))
                     {
                        res.getParameter().add(marshalStructValue(model, dm, params.get(paramId)));
                     }
                     else if (isDmsType(model, dm))
                     {
                        res.getParameter().add(marshalDmsValue(model, dm, params.get(paramId)));
                     }
                  }
               }
            }
         }
      }
   }

}
