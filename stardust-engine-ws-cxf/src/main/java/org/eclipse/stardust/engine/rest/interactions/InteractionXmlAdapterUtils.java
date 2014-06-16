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

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.getStructuredTypeName;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isStructuredType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalPrimitiveType;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.toWs;

import java.util.List;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.ws.DataFlowXto;
import org.eclipse.stardust.engine.api.ws.DataFlowsXto;
import org.eclipse.stardust.engine.api.ws.InteractionContextXto;
import org.eclipse.stardust.engine.core.interactions.ModelResolver;
import org.eclipse.stardust.engine.core.pojo.data.Type;

public class InteractionXmlAdapterUtils
{
   private static final Logger trace = LogManager.getLogger(InteractionXmlAdapterUtils.class);

   public static <T extends InteractionContextXto> T toXto(ApplicationContext ac, T xto,
         Model model, Reference ref, ModelResolver resolver)
   {
      xto.setId(ac.getId());
      xto.setName(ac.getName());

      List<AccessPoint> accessPoints;
      try
      {
         accessPoints = ac.getAllAccessPoints();
      }
      catch (NullPointerException npe)
      {
         // ignore, will most probably happen due to inconsistent model
         trace.info("Failed evaluating application access point.");

         accessPoints = emptyList();
      }

      for (int i = 0; i < accessPoints.size(); ++i)
      {
         AccessPoint ap = accessPoints.get(i);
         if (Direction.IN == ap.getDirection())
         {
            if (null == xto.getInDataFlows())
            {
               xto.setInDataFlows(new DataFlowsXto());
            }

            xto.getInDataFlows().getDataFlow().add(toXto(model, ap, ref, resolver));
         }
         else if (Direction.OUT == ap.getDirection())
         {
            if (null == xto.getOutDataFlows())
            {
               xto.setOutDataFlows(new DataFlowsXto());
            }

            xto.getOutDataFlows().getDataFlow().add(toXto(model, ap, ref, resolver));
         }
      }

      // for a transition period only: support data mappings targeting the full parameter
      // as well
      if (InteractionDataFlowUtils.supportDataMappingIds())
      {
         @SuppressWarnings("unchecked")
         final List<DataMapping> inMappings = ac.getAllInDataMappings();
         if ( !inMappings.isEmpty())
         {
            if (null == xto.getInDataFlows())
            {
               xto.setInDataFlows(new DataFlowsXto());
            }

            marshalFullParameterMappings(inMappings, xto.getInDataFlows(), model, resolver);
         }

         @SuppressWarnings("unchecked")
         final List<org.eclipse.stardust.engine.api.model.DataMapping> outMappings = ac.getAllOutDataMappings();
         if ( !outMappings.isEmpty())
         {
            if (null == xto.getOutDataFlows())
            {
               xto.setOutDataFlows(new DataFlowsXto());
            }

            marshalFullParameterMappings(outMappings, xto.getOutDataFlows(), model, resolver);
         }
      }

      return xto;
   }

   public static DataFlowXto toXto(Model model, AccessPoint accessPoint, Reference ref, ModelResolver resolver)
   {
      DataFlowXto res = new DataFlowXto();

      res.setId(accessPoint.getId());
      res.setName(accessPoint.getName());

      if (null != model)
      {
         if (isPrimitiveType(model, accessPoint))
         {
            Type primitveType = (Type) accessPoint.getAttribute(TYPE_ATT);
            res.setType(marshalPrimitiveType(primitveType));
         }
         else if (isStructuredType(model, accessPoint))
         {
            res.setType(getStructuredTypeName(model, accessPoint, ref, null, resolver));
         }
      }

      res.setDirection(accessPoint.getDirection());

      return res;
   }

   public static void marshalFullParameterMappings(List<DataMapping> dataMappings,
         DataFlowsXto holder, Model model, ModelResolver resolver)
   {
      for (int i = 0; i < dataMappings.size(); ++i)
      {
         DataMapping dm = dataMappings.get(i);

         // only consider mappings of the full parameter (no application path)
         if (isEmpty(dm.getApplicationPath()))
         {
            AccessPoint ap = null;
            try
            {
               ap = dm.getApplicationAccessPoint();
            }
            catch (NullPointerException npe)
            {
               // ignore, will most probably happen due to inconsistent model
               trace.info("Failed evaluating application access point for data mapping "
                     + dm.getId());
            }

            if ((null != ap) && !CompareHelper.areEqual(dm.getId(), ap.getId()))
            {
               holder.getDataFlow().add(toWs(dm, model, resolver));
            }
         }
      }
   }

}
