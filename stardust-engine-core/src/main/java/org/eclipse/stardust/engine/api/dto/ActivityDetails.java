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
package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ActivityDetails extends AuditTrailModelElementDetails implements Activity
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace = LogManager.getLogger(ActivityDetails.class);

   private final ImplementationType implementationType;
   private final boolean abortable;
   private final boolean interactive;
   private final String processDefinitionId;
   private final Application application;
   private final String implementationProcessDefinitionId;
   private final String qualifiedImplementationProcessDefinitionId;

   private final List<EventHandler> eventHandlers;
   private List<ApplicationContext> contexts = CollectionUtils.newArrayList();

   private final ModelParticipant defaultPerformer;
   private final Reference reference;

   private Set<QualityAssuranceCode> qualityAssuranceCodes = new HashSet<QualityAssuranceCode>();
   private boolean qualityAssuranceEnabled = false;
   private ModelParticipant qualityAssuranceParticipant = null;
   private String qualityAssuranceFormula;
   private int qualityAssuranceProbability;

   private final GatewayType joinType;

   private final GatewayType splitType;

   public ActivityDetails(IActivity activity)
   {
      this(activity, null);
   }

   public ActivityDetails(IActivity activity, IActivityInstance activityInstance)
   {
      super(activity);

      this.implementationType = activity.getImplementationType();
      this.abortable = activity.getAllowsAbortByPerformer();
      this.interactive = activity.isInteractive();

      this.processDefinitionId = activity.getProcessDefinition().getId();

      IApplication app = activity.getApplication();
      if ((null != app) && !app.isInteractive())
      {
         this.application = (Application) DetailsFactory.create(app, IApplication.class,
               ApplicationDetails.class);
      }
      else
      {
         this.application = null;
      }

      if (activity.isInteractive())
      {
         this.defaultPerformer = resolvePerformer(activity, activityInstance, activity.getPerformer());
      }
      else
      {
         this.defaultPerformer = null;
      }

      this.eventHandlers = DetailsFactory.<EventHandler, EventHandlerDetails>
         createCollection(activity.getEventHandlers(),
            IEventHandler.class, EventHandlerDetails.class);

      for (Iterator i = activity.getAllContexts();i.hasNext();)
      {
         this.contexts.add(new ApplicationContextDetails((IApplicationContext) i.next(),
               activity));
      }

      reference = DetailsFactory.create(activity.getExternalReference(), IReference.class, ReferenceDetails.class);

      qualifiedImplementationProcessDefinitionId =
         ImplementationType.SubProcess.equals(implementationType) &&
         activity.getImplementationProcessDefinition() != null
            ? reference == null
               ? '{' + getNamespace() + '}' + activity.getImplementationProcessDefinition().getId()
               : reference.getQualifiedId()
            : null;
      implementationProcessDefinitionId = qualifiedImplementationProcessDefinitionId != null
            ? reference == null
               ? qualifiedImplementationProcessDefinitionId.substring(getNamespace().length() + 2)
               : reference.getId()
            : null;


      qualityAssuranceEnabled = activity.isQualityAssuranceEnabled();
      qualityAssuranceFormula = activity.getQualityAssuranceFormula();
      qualityAssuranceProbability = activity.getQualityAssuranceProbability();
      if(qualityAssuranceEnabled)
      {
         qualityAssuranceParticipant = resolvePerformer(activity, activityInstance, activity.getQualityAssurancePerformer());
      }

      Set<IQualityAssuranceCode> codes = activity.getQualityAssuranceCodes();
      for(IQualityAssuranceCode code : codes)
      {
         QualityAssuranceCode qualityAssuranceCodeDetails = DetailsFactory.create(code, IQualityAssuranceCode.class, QualityAssuranceCodeDetails.class);
         qualityAssuranceCodes.add(qualityAssuranceCodeDetails);
      }

      joinType = toGatewayType(activity.getJoinType());
      splitType = toGatewayType(activity.getSplitType());
   }

   private GatewayType toGatewayType(JoinSplitType type)
   {
      if (type == JoinSplitType.And)
      {
         return GatewayType.And;
      }
      if (type == JoinSplitType.Xor)
      {
         return GatewayType.Xor;
      }
      if (type == JoinSplitType.Or)
      {
         return GatewayType.Or;
      }
      return null;
   }

   private ModelParticipant resolvePerformer(IActivity activity, IActivityInstance activityInstance, IModelParticipant performer)
   {
      ModelParticipant participant = null;
      if (performer instanceof IOrganization)
      {
         participant = (Organization) DetailsFactory.create(performer,
               IOrganization.class, OrganizationDetails.class);
      }
      else if (performer instanceof IRole)
      {
         participant = (Role) DetailsFactory.create(performer, IRole.class,
               RoleDetails.class);
      }
      else if (performer instanceof IConditionalPerformer)
      {
         IParticipant resolvedPerformer = null;
         if (null != activityInstance)
         {
            try
            {
               resolvedPerformer = ((IConditionalPerformer) performer)
                     .retrievePerformer(activityInstance.getProcessInstance());
            }
            catch (InternalException e)
            {
               // ignoring invalid default performer information
            }
            catch (PublicException e)
            {
               // ignoring invalid default performer information
            }
         }

         ConditionalPerformer condPerformer = (ConditionalPerformer) DetailsFactory.create(
               performer, IConditionalPerformer.class,
               ConditionalPerformerDetails.class);
         if (null != resolvedPerformer)
         {
            participant = new ResolvedConditionalPerformerDetails(
                  condPerformer, DetailsFactory.createParticipantDetails(resolvedPerformer));
         }
         else
         {
            participant = condPerformer;
         }
      }
      else
      {
         trace.warn("Creating details for model participants of type "
               + (performer != null ? performer.getClass() : null) + " is currently not supported.");
      }

      return participant;
   }

   public ImplementationType getImplementationType()
   {
      return implementationType;
   }

   public boolean isAbortable()
   {
      return abortable;
   }

   public boolean isInteractive()
   {
      return interactive;
   }

   public List<EventHandler> getAllEventHandlers()
   {
      return Collections.unmodifiableList(eventHandlers);
   }

   public EventHandler getEventHandler(String id)
   {
      return (EventHandler) ModelApiUtils.firstWithId(eventHandlers.iterator(), id);
   }

   public List<ApplicationContext> getAllApplicationContexts()
   {
      return Collections.unmodifiableList(contexts);
   }

   public ApplicationContext getApplicationContext(String id)
   {
      return (ApplicationContext) ModelApiUtils.firstWithId(contexts.iterator(), id);
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

   public Application getApplication()
   {
      return application;
   }

   public ModelParticipant getDefaultPerformer()
   {
      return defaultPerformer;
   }

   public String getDefaultPerformerID()
   {
      String id = null;
      if (null != defaultPerformer)
      {
         if (defaultPerformer instanceof ConditionalPerformer)
         {
            Participant resolvedPerformer = ((ConditionalPerformer) defaultPerformer).getResolvedPerformer();
            if (null != resolvedPerformer)
            {
               id = resolvedPerformer.getId();
            }
         }
         else
         {
            id = defaultPerformer.getId();
         }
      }
      return id;
   }

   public String getDefaultPerformerName()
   {
      String name = null;
      if (null != defaultPerformer)
      {
         if (defaultPerformer instanceof ConditionalPerformer)
         {
            Participant resolvedPerformer = ((ConditionalPerformer) defaultPerformer).getResolvedPerformer();
            if (null != resolvedPerformer)
            {
               name = resolvedPerformer.getName();
            }
         }
         else
         {
            name = defaultPerformer.getName();
         }
      }
      return name;
   }

   public String getImplementationProcessDefinitionId()
   {
      return implementationProcessDefinitionId;
   }

   public String getQualifiedImplementationProcessDefinitionId()
   {
      return qualifiedImplementationProcessDefinitionId;
   }

   public Reference getReference()
   {
      return reference;
   }

   public boolean isQualityAssuranceEnabled()
   {
      return qualityAssuranceEnabled;
   }

   public ModelParticipant getQualityAssurancePerformer()
   {
      return qualityAssuranceParticipant;
   }

   public int getDefaultQualityAssuranceProbability()
   {
      return qualityAssuranceProbability;
   }

   public String getQualityAssuranceFormula()
   {
      return qualityAssuranceFormula;
   }

   public Set<QualityAssuranceCode> getAllQualityAssuranceCodes()
   {
      return qualityAssuranceCodes;
   }

   @Override
   public GatewayType getJoinType()
   {
      return joinType;
   }

   @Override
   public GatewayType getSplitType()
   {
      return splitType;
   }
}