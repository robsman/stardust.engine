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
package org.eclipse.stardust.engine.ws.interactions;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultCodeXto.UNKNOWN_INTERACTION;
import static org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultCodeXto.UNSUPPORTED_PARAMETER_VALUE;
import static org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultCodeXto.WRONG_PARAMETER;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isStructuredType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalInDataValues;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalPrimitiveValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalStructValue;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.toWs;

import java.io.Serializable;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.ws.InteractionContextXto;
import org.eclipse.stardust.engine.api.ws.ParameterXto;
import org.eclipse.stardust.engine.api.ws.ParametersXto;
import org.eclipse.stardust.engine.api.ws.UserXto;
import org.eclipse.stardust.engine.api.ws.interactions.BpmFault;
import org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultCodeXto;
import org.eclipse.stardust.engine.api.ws.interactions.BpmInteractionFaultXto;
import org.eclipse.stardust.engine.api.ws.interactions.IBpmInteractionsService;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IBpmInteractionsService", serviceName = "BpmInteractionsService", portName = "BpmInteractionsServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/v2012a/integration/ui", endpointInterface = "org.eclipse.stardust.engine.api.ws.interactions.IBpmInteractionsService")
@HandlerChain(file="epm-interactions-service-handlers.xml")
public class InteractionsServiceFacade implements IBpmInteractionsService
{

   private static final ThreadLocal<InteractionRegistry> REGISTRY = new ThreadLocal<InteractionRegistry>();

   public static void bindRegistry(InteractionRegistry interactionsRegistry)
   {
      REGISTRY.set(interactionsRegistry);
   }
   
   public static InteractionRegistry unbindRegistry()
   {
      InteractionRegistry current = REGISTRY.get();
      
      REGISTRY.remove();
      
      return current;
   }
   
   public InteractionContextXto getDefinition(String interactionId) throws BpmFault
   {
      Interaction interaction = findInteraction(interactionId);
      
      return toWs(interaction.getDefinition(), interaction.getModel());
   }

   public UserXto getOwner(String interactionId) throws BpmFault
   {
      Interaction interaction = findInteraction(interactionId);
      
      return toWs(interaction.getOwner());
   }
   
   public ParametersXto getInputParameters(String interactionId) throws BpmFault
   {
      Interaction interaction = findInteraction(interactionId);
      
      return marshalInDataValues(interaction.getModel(), interaction.getDefinition(),
            interaction.getInDataValues());
   }

   public void setOutputParameters(String interactionId, ParametersXto outputParameters)
         throws BpmFault
   {
      Interaction interaction = findInteraction(interactionId);
      
      if (null != outputParameters)
      {
         for (ParameterXto param : outputParameters.getParameter())
         {
            DataMapping dm = findDataFlow(interaction, param.getName(), Direction.OUT);

            if (isPrimitiveType(interaction.getModel(), dm))
            {
               Serializable decodedValue = unmarshalPrimitiveValue(
                     interaction.getModel(), dm, param.getPrimitive());
               if (null != decodedValue)
               {
                  interaction.setOutDataValue(param.getName(), decodedValue);
               }
               else
               {
                  throw newBpmInteractionFault(UNSUPPORTED_PARAMETER_VALUE);
               }
            }
            else if (isStructuredType(interaction.getModel(), dm)
                  && (null != param.getXml()))
            {
               Serializable decodedValue = unmarshalStructValue(interaction.getModel(),
                     dm, param.getXml().getAny());
               if (null != decodedValue)
               {
                  interaction.setOutDataValue(param.getName(), decodedValue);
               }
               else
               {
                  throw newBpmInteractionFault(UNSUPPORTED_PARAMETER_VALUE);
               }
            }
            else
            {
               throw newBpmInteractionFault(UNSUPPORTED_PARAMETER_VALUE);
            }
         }
      }
   }

   protected Interaction findInteraction(String interactionId) throws BpmFault
   {
      InteractionRegistry interactionsRegistry = REGISTRY.get();
      
      if ((null != interactionsRegistry)
            && (null != interactionsRegistry.getInteraction(interactionId)))
      {
         return interactionsRegistry.getInteraction(interactionId);
      }
      else
      {
         throw newBpmInteractionFault(UNKNOWN_INTERACTION);
      }
   }
   
   protected DataMapping findDataFlow(Interaction interaction, String parameterId,
         Direction direction) throws BpmFault
   {
      ApplicationContext definition = interaction.getDefinition();
      
      if ((null != definition) && !isEmpty(parameterId)
            && (null != definition.getDataMapping(direction, parameterId)))
      {
         return definition.getDataMapping(direction, parameterId);
      }
      else
      {
         throw newBpmInteractionFault(WRONG_PARAMETER, parameterId);
      }
   }
   
   public static BpmFault newBpmInteractionFault(BpmInteractionFaultCodeXto faultCode)
         throws BpmFault
   {
      return newBpmInteractionFault(faultCode, null);
   }

   public static BpmFault newBpmInteractionFault(BpmInteractionFaultCodeXto faultCode,
         String description) throws BpmFault
   {
      BpmInteractionFaultXto fault = new BpmInteractionFaultXto();
      
      fault.setFaultCode(faultCode);
      fault.setFaultDescription(description);

      return new BpmFault("", fault);
   }

}
