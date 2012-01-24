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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
class Model extends IdentifiableElement
{
   private int currentElementOID;
   private final long modelOid;

   private Vector applications = new Vector();
   private Vector data = new Vector();
   private Vector diagrams = new Vector();
   private Vector modelers = new Vector();
   private Vector linkTypes = new Vector();
   private Vector conditionalPerformers = new Vector();
   private Vector organizations = new Vector();
   private Vector processDefinitions = new Vector();
   private Vector roles = new Vector();
   private Vector views = new Vector();
   private Vector dataTypes = new Vector();
   private Vector applicationTypes = new Vector();
   private Vector applicationContextTypes = new Vector();
   private Vector triggerTypes = new Vector();
   private Vector conditionTypes = new Vector();
   private Vector actionTypes = new Vector();

   public Model(long modelOid, String id, String name, String description)
   {
      super(id, name, description);
      
      this.modelOid = modelOid;
   }

   public Application createApplication(String id, String name, String description, int oid)
   {
      Application result = new Application(id, name, description);
      applications.add(result);
      register(result, oid);
      return result;
   }

   public Data createData(String id, String name, String description, int elementOID)
   {
      Data result = new Data(id, name, description);
      data.add(result);
      register(result, elementOID);
      return result;
   }

   public Diagram createDiagram(String name, int elementOID)
   {
      Diagram result = new Diagram(name, elementOID, this);
      diagrams.add(result);
      return result;
   }

   public Modeler createModeler(String id, String name, String description, String password, int elementOID)
   {
      Modeler result = new Modeler(id, name, description, password, elementOID, this);
      modelers.add(result);
      return result;
   }

   public LinkType createLinkType(String name,
         String sourceClassName, String targetClassName,
         String sourceRoleName, String targetRoleName,
         String sourceCardinality, String targetCardinality,
         String sourceSymol, String targetSymbol,
         String lineColor, String lineType,
         boolean showLinkTypeName, boolean showRoleNames, int oldElementOID)
   {
      LinkType result = new LinkType(name, sourceClassName, targetClassName,
            sourceRoleName, targetRoleName, sourceCardinality, targetCardinality,
            sourceSymol, targetSymbol, lineColor, lineType,
            showLinkTypeName, showRoleNames, oldElementOID, this);
      linkTypes.add(result);
      return result;
   }

   public ConditionalPerformer createConditionalPerformer(String id, String name,
         String description, String dataId, boolean isUser, int elementOID)
   {
      ConditionalPerformer result =
            new ConditionalPerformer(id, name, description, dataId, isUser, elementOID, this);
      conditionalPerformers.add(result);
      return result;
   }

   public Organization createOrganization(String id, String name, String description, int elementOID)
   {
      Organization result = new Organization(id, name, description, elementOID, this);
      organizations.add(result);
      return result;
   }

   public ProcessDefinition createProcessDefinition(String id, String name, String description, int elementOID)
   {
      ProcessDefinition result = new ProcessDefinition(id, name, description, elementOID, this);
      processDefinitions.add(result);
      return result;
   }

   public Role createRole(String id, String name, String description,
         int cardinality, int elementOID)
   {
      Role result = new Role(id, name, description, cardinality, elementOID, this);
      roles.add(result);
      return result;
   }

   public View createView(String name, String description, int elementOID)
   {
      View result = new View(name, description, elementOID, this);
      views.add(result);
      return result;
   }

   public void setCurrentElementOID(int oid)
   {
      this.currentElementOID = oid;
   }

   public Iterator getAllRoles()
   {
      return roles.iterator();
   }

   public Iterator getAllData()
   {
      return data.iterator();
   }

   public Iterator getAllLinkTypes()
   {
      return linkTypes.iterator();
   }

   public Iterator getAllTopLevelViews()
   {
      return views.iterator();
   }

   public Iterator getAllDiagrams()
   {
      return diagrams.iterator();
   }

   public Iterator getAllProcessDefinitions()
   {
      return processDefinitions.iterator();
   }

   public Iterator getAllApplications()
   {
      return applications.iterator();
   }

   public long getModelOid()
   {
      return modelOid;
   }

   public Application findApplication(String id)
   {
      for (Iterator i = applications.iterator(); i.hasNext();)
      {
         Application application = (Application) i.next();
         if (application.getId().equals(id))
         {
            return application;
         }
      }
      return null;
   }

   public Data findData(String id)
   {
      for (Iterator i = data.iterator(); i.hasNext();)
      {
         Data data = (Data) i.next();
         if (data.getId().equals(id))
         {
            return data;
         }
      }
      return null;
   }

   public ModelElement findParticipant(String id)
   {
      for (Iterator i = roles.iterator(); i.hasNext();)
      {
         Role role = (Role) i.next();
         if (role.getId().equals(id))
         {
            return role;
         }
      }
      for (Iterator i = organizations.iterator(); i.hasNext();)
      {
         Organization organization = (Organization) i.next();
         if (organization.getId().equals(id))
         {
            return organization;
         }
      }
      for (Iterator i = modelers.iterator(); i.hasNext();)
      {
         Modeler modeler = (Modeler) i.next();
         if (modeler.getId().equals(id))
         {
            return modeler;
         }
      }
      for (Iterator i = conditionalPerformers.iterator(); i.hasNext();)
      {
         ConditionalPerformer performer = (ConditionalPerformer) i.next();
         if (performer.getId().equals(id))
         {
            return performer;
         }
      }
      return null;
   }

   public ProcessDefinition findProcessDefinition(String id)
   {
      for (Iterator i = processDefinitions.iterator(); i.hasNext();)
      {
         ProcessDefinition process = (ProcessDefinition) i.next();
         if (process.getId().equals(id))
         {
            return process;
         }
      }
      return null;
   }

   public DataType createDataType(String id, String name, boolean predefined, int elementOID)
   {
      DataType type = new DataType(id, name, predefined);
      dataTypes.add(type);
      register(type, elementOID);
      return type;
   }

   public ApplicationType createApplicationType(String id, String name,
         boolean predefined, boolean synchronous, int elementOID)
   {
      ApplicationType type = new ApplicationType(id, name, predefined, synchronous);
      applicationTypes.add(type);
      register(type, elementOID);
      return type;
   }

   public ApplicationContextType createApplicationContextType(String id, String name,
         boolean predefined, boolean hasMappingId, boolean hasApplicationPath,
         int elementOID)
   {
      ApplicationContextType type = new ApplicationContextType(id, name, predefined,
            hasMappingId, hasApplicationPath);
      applicationContextTypes.add(type);
      register(type, elementOID);

      return type;
   }

   public Iterator getAllDataTypes()
   {
      return dataTypes.iterator();
   }

   public Iterator getAllApplicationTypes()
   {
      return applicationTypes.iterator();
   }

   public Iterator getAllApplicationContextTypes()
   {
      return applicationContextTypes.iterator();
   }

   public void register(ModelElement element, int elementOID)
   {
      if (elementOID > 0)
      {
         element.setElementOID(elementOID);
      }
      else
      {
         element.setElementOID(++currentElementOID);
      }
   }

   public Iterator getAllModelers()
   {
      return modelers.iterator();
   }

   public Iterator getAllOrganizations()
   {
      return organizations.iterator();
   }

   public Iterator getAllConditionalPerformsers()
   {
      return conditionalPerformers.iterator();
   }

   public TriggerType createTriggerType(String id, String name, boolean predefined, boolean pullTrigger, int elementOID)
   {
      TriggerType type = new TriggerType(id, name, pullTrigger, predefined);
      triggerTypes.add(type);
      register(type, elementOID);
      return type;
   }

   public Iterator getAllTriggerTypes()
   {
      return triggerTypes.iterator();
   }

   public EventConditionType createEventConditionType(String id, String name,
         String implementation, boolean processCondition, boolean activityCondition,
         boolean predefined, int elementOID)
   {
      EventConditionType type = new EventConditionType(id, name, implementation, 
            processCondition, activityCondition, predefined);
      conditionTypes.add(type);
      register(type, elementOID);
      return type;
   }

   public Iterator getAllEventConditionTypes()
   {
      return conditionTypes.iterator();
   }

   public EventActionType createEventActionType(String id, String name,
         boolean predefined, boolean processNot, boolean activityNot, int elementOID)
   {
      EventActionType type = new EventActionType(id, name, predefined, processNot, activityNot);
      actionTypes.add(type);
      register(type, elementOID);
      return type;
   }

   public Iterator getAllEventActionTypes()
   {
      return actionTypes.iterator();
   }

   public EventActionType findEventActionType(String type)
   {
      for (Iterator i = actionTypes.iterator(); i.hasNext();)
      {
         EventActionType actionType = (EventActionType) i.next();
         if (actionType.getId().equals(type))
         {
            return actionType;
         }
      }
      return null;
   }

}
