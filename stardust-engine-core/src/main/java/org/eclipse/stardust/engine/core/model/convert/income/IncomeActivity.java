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
package org.eclipse.stardust.engine.core.model.convert.income;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.JoinSplitType;


public class IncomeActivity extends IncomeElement
{
   public static final Logger log = Logger.getLogger(IncomeActivity.class);

   public static final String LOAD_QUERY = "select distinct a.act_id, att.actt_short_name, att.actt_description, raa.raa_rol_id, a.act_dia_ref_id from inc4_activities a, inc4_activity_texts att, inc4_role_act_assigns raa where a.act_dia_id = ? and a.act_id = att.actt_act_id and a.act_id = raa.raa_act_id (+)";

   public final static String ID_FIELD = "ACT_ID";

   public final static String NAME_FIELD = "ACTT_SHORT_NAME";

   public final static String DESCRIPTION_FIELD = "ACTT_DESCRIPTION";

   public final static String ROLE_FIELD = "RAA_ROL_ID";

   public final static String SUBPROCESS_ID_FIELD = "ACT_DIA_REF_ID";

   public final static String _START_ACTIVITY_ID = "_START_ACTIVITY";

   public final static String _ROUTE_ACTIVITY_NAME = "Route";

   private Set inConnections;

   private Set outConnections;

   private boolean processed;

   private IncomeRole role;

   private IncomeDocument document;

   private Integer subprocess;

   public Integer getSubprocess()
   {
      return subprocess;
   }

   public void setSubprocess(Integer subprocess)
   {
      this.subprocess = subprocess;
   }

   public IncomeDocument getDocument()
   {
      return document;
   }

   public void setDocument(IncomeDocument document)
   {
      this.document = document;
   }

   public IncomeRole getRole()
   {
      return role;
   }

   public void setRole(IncomeRole role)
   {
      this.role = role;
   }

   public IncomeActivity(String id, String name, String description)
   {
      super(id, name, description);
      this.inConnections = new HashSet();
      this.outConnections = new HashSet();
      this.processed = false;
   }

   public Object create(IProcessDefinition processDefinition)
   {
      IModel model = (IModel) processDefinition.getModel();

      IActivity activity = null;

      if (!this.processed)
      {

         if (log.isDebugEnabled())
         {
            log.debug("Processing activity : " + this.getName());
         }

         activity = processDefinition.createActivity(this.getId(), this.getName(), this
               .getDescription(), 0); // TODO description

         if (this.role != null)
         {

            IModelParticipant participant = model.findParticipant(role.getId());
            activity.setPerformer(participant);

            // the activity is an interactive activtiy and is defined as manual
            // implemented
            activity.setImplementationType(ImplementationType.Manual);
         }

         if (this.getDocument() != null)
         {
            IApplication application = model.findApplication(this.getId());
            if (application != null)
            {
               activity.setApplication(application);
               activity.setImplementationType(ImplementationType.Application);
            }
            else
            {
               log.error("No application defined for non-interactive activity.");
            }
         }

         if (this.inConnections.size() > 1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Activity " + this.getName() + " with AND Join.");
            }

            activity.setJoinType(JoinSplitType.And);

            // check if one of the objectstore has an objecttype assigned
            for (Iterator connectionIterator = this.inConnections.iterator(); connectionIterator
                  .hasNext();)
            {
               IncomeConnection connection = (IncomeConnection) connectionIterator.next();
               IncomeObjectstore objectstore = connection.getObjectstore();
               if (objectstore != null && objectstore.getObjecttypes().size() > 0)
               {
                  if (log.isDebugEnabled())
                  {
                     log
                           .debug("One of the incomming objectstore has an objecttype assigned.");
                  }

                  this.createDataMappings(processDefinition, activity, objectstore
                        .getObjecttypes(), Direction.IN);

                  break;
               }
            }

         }
         else if (this.inConnections.size() == 1)
         {
            IncomeConnection connection = (IncomeConnection) this.inConnections
                  .iterator().next();
            IncomeObjectstore objectstore = connection.getObjectstore();

            if (objectstore.getInConnections().size() > 1)
            {
               if (log.isDebugEnabled())
               {
                  log.debug("Activity " + this.getName() + " with OR Join.");
               }
               activity.setJoinType(JoinSplitType.Xor);
            }

            // InData Handling
            if (objectstore != null && objectstore.getObjecttypes().size() > 0)
            {
               this.createDataMappings(processDefinition, activity, objectstore
                     .getObjecttypes(), Direction.IN);
            }

         }
         else
         {
            if (log.isDebugEnabled())
            {
               log.debug("Activity " + this.getName() + " with no incoming transition.");
            }
            activity.setJoinType(JoinSplitType.None);
         }

         if (this.outConnections.size() > 1)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Activity " + this.getName() + " with AND Split.");
            }
            activity.setSplitType(JoinSplitType.And);

            // check if one of the objectstore has an objecttype assigned
            for (Iterator connectionIterator = this.outConnections.iterator(); connectionIterator
                  .hasNext();)
            {
               IncomeConnection connection = (IncomeConnection) connectionIterator.next();
               IncomeObjectstore objectstore = connection.getObjectstore();
               if (objectstore != null && objectstore.getObjecttypes().size() > 0)
               {
                  if (log.isDebugEnabled())
                  {
                     log
                           .debug("One of the outgoing objectstore has an objecttype assigned.");
                  }

                  this.createDataMappings(processDefinition, activity, objectstore
                        .getObjecttypes(), Direction.OUT);
                  break;
               }
            }
         }
         else if (this.outConnections.size() == 1)
         {
            IncomeConnection connection = (IncomeConnection) this.outConnections
                  .iterator().next();
            IncomeObjectstore objectstore = connection.getObjectstore();

            if (objectstore.getOutConnections().size() > 1)
            {
               if (log.isDebugEnabled())
               {
                  log.debug("Activity " + this.getName() + " with OR Split.");
               }
               activity.setSplitType(JoinSplitType.Xor);
            }

            // OutData Handling
            if (objectstore != null && objectstore.getObjecttypes().size() > 0)
            {
               this.createDataMappings(processDefinition, activity, objectstore
                     .getObjecttypes(), Direction.OUT);
            }

         }
         else
         {
            if (log.isDebugEnabled())
            {
               log.debug("Activity " + this.getName() + " with no outgoing transition.");
            }
            activity.setSplitType(JoinSplitType.None);
         }

         this.processed = true;
      }
      return activity;
   }

   public Set getInConnections()
   {
      return this.inConnections;
   }

   public Set getOutConnections()
   {
      return this.outConnections;
   }

   public void addConnection(IncomeConnection connection)
   {
      // for activities the connections are reverse
      if (connection.getType().equals(IncomeConnection.IN_CONNECTION_TYPE))
      {
         this.inConnections.add(connection);
      }
      else if (connection.getType().equals(IncomeConnection.OUT_CONNECTION_TYPE))
      {
         this.outConnections.add(connection);
      }
      else
      {
         log.error("Unsupported connection type.");
         throw new RuntimeException("Unsupported connection type.");
      }
   }

   public boolean isProcessStartActivity()
   {
      return this.inConnections.isEmpty();
   }

   private void createDataMappings(IProcessDefinition processDefinition,
         IActivity activity, Set objecttypes, Direction direction)
   {
      if (activity.getPerformer() != null || activity.getApplication() != null)
      {
         for (Iterator objecttypeIterator = objecttypes.iterator(); objecttypeIterator
               .hasNext();)
         {
            IncomeObjecttype objecttype = (IncomeObjecttype) objecttypeIterator.next();

            for (Iterator attributeIterator = objecttype.getAttributes().iterator(); attributeIterator
                  .hasNext();)
            {
               IncomeObjecttype.IncomeAttribute attribute = (IncomeObjecttype.IncomeAttribute) attributeIterator
                     .next();
               IModel model = (IModel) processDefinition.getModel();
               IData data = model.findData(objecttype.getName() + "_"
                     + attribute.getName());

               if (activity.getImplementationType().equals(ImplementationType.Route))
               {
                  activity.setImplementationType(ImplementationType.Manual);
                  activity.createDataMapping(objecttype.getName() + " "
                        + attribute.getName(), objecttype.getName() + " "
                        + attribute.getName(), data, direction);
               }
               else
               {
                  activity.createDataMapping(objecttype.getName() + " "
                        + attribute.getName(), objecttype.getName() + " "
                        + attribute.getName(), data, direction);
               }
            }
         }
      }
   }
}