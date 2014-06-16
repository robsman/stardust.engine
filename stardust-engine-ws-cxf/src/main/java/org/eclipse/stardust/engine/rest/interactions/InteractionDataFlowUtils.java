/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
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
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalDataValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.ws.ParameterXto;
import org.eclipse.stardust.engine.api.ws.ParametersXto;
import org.eclipse.stardust.engine.core.interactions.ModelResolver;

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
         Reference ref, Map<String, ? extends Serializable> params, ParametersXto res, ModelResolver resolver)
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
                  res.getParameter().add(marshalStructValue(model, ap, ref, params.get(ap.getId()), resolver));
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
            @SuppressWarnings("unchecked")
            List<DataMapping> allInDataMappings = (List<DataMapping>) context.getAllInDataMappings();
            for (DataMapping dm : allInDataMappings)
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
                        res.getParameter().add(marshalStructValue(model, dm, params.get(paramId), resolver));
                     }
                     else if (isDmsType(model, dm))
                     {
                        res.getParameter().add(marshalDmsValue(model, dm, params.get(paramId), resolver));
                     }
                  }
               }
            }
         }
      }
   }

   public static Map<String, Serializable> unmarshalDataValues(Model model,
         ApplicationContext context, ParametersXto params, ModelResolver resolver)
   {
      Map<String, Serializable> res = null;
      if ((null != context) && (null != model) && (null != params))
      {
         res = new HashMap<String, Serializable>();
         @SuppressWarnings("unchecked")
         List<DataMapping> dataMappings = context.getAllOutDataMappings();
         for (DataMapping dm : dataMappings)
         {
            ParameterXto param = null;
            // Iterator through params to find a matching one
            for (int i = 0; i < params.getParameter().size(); ++i)
            {
               param = params.getParameter().get(i);
               if (param.getName().equals(dm.getApplicationAccessPoint().getId()))
               {
                  if (null != dm)
                  {
                     Data data = model.getData(dm.getDataId());
                     if (data != null)
                     {
                        res.put(param.getName(),
                              unmarshalDataValue(model, data, dm.getDataPath(),
                                    dm.getMappedType(), param, resolver));
                     }
                     else
                     {
                        throw new NullPointerException("Data not found in model for id: " + param.getName());
                     }
                     break;
                  }
               }
            }
         }
      }
      return res;
   }
}
