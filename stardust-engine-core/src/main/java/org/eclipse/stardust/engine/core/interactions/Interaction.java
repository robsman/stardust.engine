/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.interactions;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class Interaction
{
   private static final Logger trace = LogManager.getLogger(Interaction.class);

   private final String id;

   private final User owner;

   private final ActivityInstance activityInstance;

   private final String contextId;

   private Map<String, ? extends Serializable> inDataValues;

   private Map<String, Serializable> outDataValues;

   private final Model model;

   private Status status;

   private ServiceFactory serviceFactory;

   private ModelResolver resolver;

   public static String getInteractionId(ActivityInstance ai)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(ai.getOID()) //
            .append("|")
            .append(ai.getLastModificationTime().getTime());

      String token = new String(Base64.encode(buffer.toString().getBytes()));

      if (trace.isInfoEnabled())
      {
         trace.info("Resolved interaction ID for " + ai + " to " + token);
      }

      return token;
   }

   public Interaction(User owner, Model model, ActivityInstance activityInstance,
         String contextId, ServiceFactory serviceFactory)
   {
      this.owner = owner;
      this.model = model;
      this.activityInstance = activityInstance;
      this.contextId = contextId;
      this.serviceFactory = serviceFactory;


      this.id = getInteractionId(activityInstance);

      this.status = Status.Active;
   }

   public Interaction(User owner, ActivityInstance activityInstance, String contextId, ModelResolver resolver)
   {
      this(owner, resolver.getModel(activityInstance.getModelOID()), activityInstance, contextId, resolver.getServiceFactory());
      this.resolver = resolver;
   }

   public String getId()
   {
      return id;
   }

   public User getOwner()
   {
      return owner;
   }

   public Model getModel()
   {
      return model;
   }

   public ApplicationContext getDefinition()
   {
      return activityInstance.getActivity().getApplicationContext(contextId);
   }

   public Reference getCrossModelReference()
   {
      return activityInstance.getActivity().getReference();
   }

   public ModelResolver getResolver()
   {
      return resolver;
   }

   public Map<String, ? extends Serializable> getInDataValues()
   {
      return inDataValues;
   }

   public ServiceFactory getServiceFactory()
   {
      return serviceFactory;
   }

   public Serializable getInDataValue(String parameterId)
   {
      return (null != inDataValues) ? inDataValues.get(parameterId) : null;
   }

   public void setInDataValues(Map<String, ? extends Serializable> inDataValues)
   {
      this.inDataValues = inDataValues;
   }

   public Map<String, Serializable> getOutDataValues()
   {
      return outDataValues;
   }

   public <V extends Serializable> void setOutDataValues(Map<String, V> outDataValues)
   {
      this.outDataValues = newHashMap();
      this.outDataValues.putAll(outDataValues);
   }

   public void setOutDataValue(String parameterId, Serializable value)
   {
      if (null == outDataValues)
      {
         this.outDataValues = newHashMap();
      }

      outDataValues.put(parameterId, value);
   }

   public Status getStatus()
   {
      return status;
   }

   public void setStatus(Status status)
   {
      this.status = status;
   }

   public static enum Status
   {
      Active, Incomplete, Complete, Aborted
   }

}
