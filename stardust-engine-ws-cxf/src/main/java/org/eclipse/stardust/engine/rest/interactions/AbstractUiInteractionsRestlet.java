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
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.rest.interactions;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.rest.interactions.InteractionDataFlowUtils.marshalInteractionInDataValues;
import static org.eclipse.stardust.engine.rest.interactions.InteractionXmlAdapterUtils.toXto;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalInDataValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalInDataValueAsJson;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalOutDataValue;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.toWs;

import java.io.Serializable;
import java.util.List;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.ws.ActivityDefinitionXto;
import org.eclipse.stardust.engine.api.ws.InteractionContextXto;
import org.eclipse.stardust.engine.api.ws.ParameterXto;
import org.eclipse.stardust.engine.api.ws.ParametersXto;
import org.eclipse.stardust.engine.api.ws.UserXto;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.engine.ws.DataFlowUtils;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public abstract class AbstractUiInteractionsRestlet
{

   @PathParam("interactionId")
   protected String interactionId;

   protected abstract InteractionRegistry getInteractionRegistry();

   public String getOverview(UriInfo uriInfo)
   {
      // verify interaction URI is valid
      findInteraction();

      StringBuilder buffer = new StringBuilder();

      buffer.append("<html>")
            .append("<head></head>")
            .append("<body>")
            .append("<p><a href='").append(uriInfo.getAbsolutePath()).append("/definition'>Show Interaction Definition</a></p>")
            .append("<p><a href='").append(uriInfo.getAbsolutePath()).append("/owner'>Show Interaction Owner</a></p>")
            .append("<p><a href='").append(uriInfo.getAbsolutePath()).append("/inData'>Show In Data Values</a></p>")
            .append("<p><a href='").append(uriInfo.getAbsolutePath()).append("/outData'>Submit Out Data Values</a></p>")
            .append("</body></html>");

      return buffer.toString();
   }

   protected InteractionDefinition getDefinition() throws WebApplicationException
   {
      Interaction interaction = findInteraction();

      return toXto(interaction.getDefinition(), new InteractionDefinition(),
            interaction.getModel());
   }

   protected InteractionOwner getOwner() throws WebApplicationException
   {
      Interaction interaction = findInteraction();

      return toWs(interaction.getOwner(), new InteractionOwner());
   }

   protected InDataValues getInDataValues() throws WebApplicationException
   {
      Interaction interaction = findInteraction();

      InDataValues result = new InDataValues();

      marshalInteractionInDataValues(interaction.getModel(), interaction.getDefinition(),
            interaction.getInDataValues(), result);

      return result;
   }

   protected ParameterXto getInDataValue(String parameterId)
         throws WebApplicationException
   {
      Interaction interaction = findInteraction();
      DataMapping dm = findDataFlow(interaction, parameterId, Direction.IN);

      ParameterXto result = marshalInDataValue(interaction.getModel(), dm,
            interaction.getInDataValue(parameterId));

      if (null == result)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return result;
   }

   protected String getInDataValueAsJson(String parameterId, String callback)
         throws WebApplicationException
   {
      Interaction interaction = findInteraction();
      DataMapping dm = findDataFlow(interaction, parameterId, Direction.IN);

      String result = marshalInDataValueAsJson(interaction.getModel(), dm,
            interaction.getInDataValue(parameterId));

      if ( !isEmpty(callback))
      {
         // provide JSONP
         result = callback + "(" + result + ");";
      }

      return result;
   }

   protected void setOutDataValues(OutDataValues outDataValues)
   {
      Interaction interaction = findInteraction();

      interaction.setOutDataValues(InteractionDataFlowUtils.unmarshalDataValues(
            interaction.getModel(), interaction.getDefinition(), outDataValues));

   }

   protected void setOutDataValue(String parameterId, Object value)
   {
      Interaction interaction = findInteraction();

      AccessPoint outParam = findParameterDefinition(interaction, parameterId, Direction.OUT);
      if (null == outParam)
      {
         if (InteractionDataFlowUtils.supportDataMappingIds())
         {
            setOutDataValueByMappingId(parameterId, value);
            return;
         }
         else
         {
            throw new WebApplicationException(Status.NOT_FOUND);
         }
      }

      Serializable decodedValue = unmarshalOutDataValue(interaction.getModel(), outParam, value);
      if (null != decodedValue)
      {
         interaction.setOutDataValue(parameterId, decodedValue);
      }
      else
      {
         throw new WebApplicationException(Status.BAD_REQUEST);
      }
   }

   protected void setOutDataValueByMappingId(String parameterId, Object value)
   {
      Interaction interaction = findInteraction();

      DataMapping dm = findDataFlow(interaction, parameterId, Direction.OUT);

      UiInteractionsRestlet.trace.warn("Writing OUT data using data mapping ID \"" + parameterId
            + "\", this is only supported for a transition period.");

      Model model = interaction.getModel();
      if (DataFlowUtils.isStructuredType(model, dm))
      {
         Data data = model.getData(dm.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = interaction.getServiceFactory()
                  .getQueryService()
                  .getModel(data.getModelOID());
         }
      }
      Serializable decodedValue = unmarshalOutDataValue(model, dm, value);
      if (null != decodedValue)
      {
         String outParamId = dm.getApplicationAccessPoint().getId();
         interaction.setOutDataValue(outParamId, decodedValue);
      }
      else
      {
         throw new WebApplicationException(Status.BAD_REQUEST);
      }
   }

   protected Interaction findInteraction() throws WebApplicationException
   {
      InteractionRegistry registry = getInteractionRegistry();
      if ((null != registry) && (null != registry.getInteraction(interactionId)))
      {
         return registry.getInteraction(interactionId);
      }
      else
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

   }

   protected AccessPoint findParameterDefinition(Interaction interaction,
         String parameterId, Direction direction) throws WebApplicationException
   {
      ApplicationContext definition = interaction.getDefinition();

      if ((null != definition) && !isEmpty(parameterId))
      {
         for (AccessPoint ap : (List<AccessPoint>) definition.getAllAccessPoints())
         {
            if ((direction == ap.getDirection()) && parameterId.equals(ap.getId()))
            {
               return ap;
            }
         }
      }

      return null;
   }

   protected DataMapping findDataFlow(Interaction interaction, String parameterId,
         Direction direction) throws WebApplicationException
   {
      DataMapping ret = null;
      ApplicationContext definition = interaction.getDefinition();

      if ((null != definition) && !isEmpty(parameterId))
      {
         AccessPoint outParam = findParameterDefinition(interaction, parameterId,
               direction);
         if (null == outParam)
         {
            if (InteractionDataFlowUtils.supportDataMappingIds())
            {
               ret = definition.getDataMapping(direction, parameterId);
            }
         }
         else
         {
            List<DataMapping> allDataMappings = definition.getAllDataMappings();
            for (DataMapping dataMapping : allDataMappings)
            {
               if (outParam.equals(dataMapping.getApplicationAccessPoint()))
               {
                  ret = dataMapping;
                  break;
               }
            }
         }
      }

      if (ret == null)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return ret;
   }

   @XmlRootElement(name="activityDefinition", namespace="http://eclipse.org/stardust/ws/v2012a/api")
   public static class ActivityDefinition extends ActivityDefinitionXto
   {

   }

   @XmlRootElement(name="interactionDefinition", namespace="http://eclipse.org/stardust/ws/v2012a/api")
   public static class InteractionDefinition extends InteractionContextXto
   {

   }

   @XmlRootElement(name="owner", namespace="http://eclipse.org/stardust/ws/v2012a/api")
   public static class InteractionOwner extends UserXto
   {

   }

   @XmlRootElement(name="inDataValues", namespace="http://eclipse.org/stardust/ws/v2012a/api")
   public static class InDataValues extends ParametersXto
   {

   }

   @XmlRootElement(name="outDataValues", namespace="http://eclipse.org/stardust/ws/v2012a/api")
   public static class OutDataValues extends ParametersXto
   {

   }

}
