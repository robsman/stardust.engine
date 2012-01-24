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
package org.eclipse.stardust.engine.core.model.convert.aris;

import java.util.Iterator;
import java.util.ArrayList;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.model.gui.ActivitySymbol;
import org.eclipse.stardust.engine.core.model.gui.AnnotationSymbol;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author fherinean
 * @version $Revision$
 */
public class Model extends ArisElement
{
   public static final String TAG_NAME = "Model";

   public static final int UNKNOWN = 0;
   public static final int PROCESS = 1;
   public static final int ORGANIZATIONAL_CHART = 2;
   public static final int TECHNICAL_TERM_DIAGRAM = 3;

   public static final String[] types = {
      "Unknown", "Process Definition", "Organizational Chart", "Technical Term Diagram"
   };

   private static final String[] arisTypes = {
      "MT_EEPC", "MT_ORG_CHRT", "MT_TECH_TRM_MDL"
   };

   private static final int[] ORG_CHART_TYPES = {
      ObjectDefinition.ROLE, ObjectDefinition.ORGANIZATION, ObjectDefinition.PERSON
   };

   private static final String MODEL_ID_ATT = TAG_NAME + "." + ID_ATT;
   private static final String MODEL_TYPE_ATT = TAG_NAME + "." + TYPE_ATT;
   private static final String DEFAULT_PERFORMER_ID = "default_performer";
   private static final String DEFAULT_TRIGGER_ID = "default_trigger";
   private static final String DEFAULT_STARTER_ID = "default_starter";

   private int type;
   private String typeString;
   private Diagram diagram;
   private boolean reverse = false;

   /**
    * An ARIS Model, which can be either a Process Definition or a diagram (Organizational
    * Chart or Technical Diagram.
    *
    * The following items are processed in the modelling environment:
    * - ObjOcc (0..n): object occurences. Specifies which of the defined objects are
    *      present in the process definition or diagram.
    * - FFTextOcc (0..n): freeform texts (annotations).
    * - AttrDef (0..n): dynamic attributes.
    *
    * The Model item contains 2 attribute, the Id and the Type, which includes:
    * - MT_EEPC: Process Definition
    * - MT_ORG_CHRT: Organizational Chart
    * - MT_TECH_TRM_MDL: Technical Diagram
    *
    * @param root the AML object
    * @param group the xml element containing the Model definition
    */
   public Model(AML root, Element group)
   {
      super(root, group.getAttribute(MODEL_ID_ATT));
      typeString = group.getAttribute(MODEL_TYPE_ATT);
      type = getType(typeString, arisTypes);
      NodeList nodes = group.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node child = nodes.item(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (ATTRIBUTE_TAG_NAME.equals(element.getTagName()))
            {
               addAttribute(element);
            }
            else if (ObjectOccurence.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new ObjectOccurence(root, element));
            }
            /* FFTextOcc
            else if (Model.TAG_NAME.equals(element.getTagName()))
            {
               addArisElement(new Model(root, element));
            }*/
         }
      }
      // trick to make it all horizontal, not sure about the real meaning of the attribute
      reverse = "4".equals(getAttribute("AT_UA_TXT_11"));
   }

   public String toString()
   {
      return (type == 0 ? typeString : types[type]) + ": " + getName();
   }

   public void resolveReferences()
   {
   }

   public void create(IModel model)
   {
      switch (type)
      {
         case PROCESS:
            IProcessDefinition process = model.createProcessDefinition(
                  getId(), getName(), getDescription());
            diagram = (Diagram) process.getAllDiagrams().next();
            createDiagram(model, null);
            break;
         case ORGANIZATIONAL_CHART:
            diagram = model.createDiagram(getName());
            createDiagram(model, ORG_CHART_TYPES);
            break;
         case TECHNICAL_TERM_DIAGRAM:
            diagram = model.createDiagram(getName());
            createDiagram(model, null);
            break;
         default:
            addError(new ConvertWarningException("Unsupported model type: " + typeString));
      }
   }

   private void createDiagram(IModel model, int[] objectTypes)
   {
      createNodeSymbols(model, objectTypes);
      createTopology();
      createExecutesConnections(model, objectTypes);
      createPerformsConnections(model, objectTypes);
      createDataMappings(model, objectTypes);
      createTransitions(model, objectTypes);
      createAnnotationLinks(model, objectTypes);
      if (isPrototype())
      {
         adjustForExecutability(model);
      }
   }

   private void adjustForExecutability(IModel model)
   {
      IdentifiableElement parent = (IdentifiableElement) diagram.getParent();
      if (parent instanceof IProcessDefinition)
      {
         fixDefaultPerformer(model, (IProcessDefinition) parent);
         fixMultipleStartActivities((IProcessDefinition) parent);
      }
   }

   private void fixMultipleStartActivities(IProcessDefinition pd)
   {
      ArrayList list = new ArrayList();
      for (Iterator i = pd.getAllActivities(); i.hasNext();)
      {
         IActivity activity = (IActivity) i.next();
         if (!activity.getAllInTransitions().hasNext())
         {
            list.add(activity);
         }
      }
      if (list.size() > 1)
      {
         IActivity starter = pd.createActivity(DEFAULT_STARTER_ID, "Default Starter Activity", null, 0);
         starter.setSplitType(JoinSplitType.And);
         for (int i = 0; i < list.size(); i++)
         {
            IActivity activity = (IActivity) list.get(i);
            String id = pd.getDefaultTransitionId();
            pd.createTransition(id, id, null, starter, activity, 0);
         }
      }
   }

   private void fixDefaultPerformer(IModel model, IProcessDefinition pd)
   {
      IModelParticipant defperf = model.findParticipant(DEFAULT_PERFORMER_ID);
      if (defperf == null)
      {
         defperf = model.createRole(DEFAULT_PERFORMER_ID, "Default Performer", null, 0);
      }
      ITrigger trigger = pd.createTrigger(DEFAULT_TRIGGER_ID, "Default Manual Trigger",
            model.findTriggerType(PredefinedConstants.MANUAL_TRIGGER), 0);
      trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT,
            DEFAULT_TRIGGER_ID);
      for (Iterator i = pd.getAllActivities(); i.hasNext();)
      {
         IActivity activity = (IActivity) i.next();
         if (activity.isInteractive() && activity.getPerformer() == null)
         {
            activity.setPerformer(defperf);
         }
      }
   }

   private void createTopology()
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getReference().getType() == ObjectDefinition.RULE)
            {
               occurence.processRule(diagram);
            }
         }
      }
   }

   private void createNodeSymbols(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            occurence.setReverse(reverse);
            occurence.create(model, diagram, objectTypes);
         }
      }
   }

   private void createExecutesConnections(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getReference().getType() == ObjectDefinition.APPLICATION)
            {
               occurence.connect(model, diagram, objectTypes);
            }
         }
      }
   }

   private void createPerformsConnections(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getReference().getType() == ObjectDefinition.ROLE ||
                occurence.getReference().getType() == ObjectDefinition.ORGANIZATION ||
                occurence.getReference().getType() == ObjectDefinition.GROUP)
            {
               occurence.connect(model, diagram, objectTypes);
            }
         }
      }
   }

   private void createDataMappings(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getReference().getType() == ObjectDefinition.DATA)
            {
               occurence.connect(model, diagram, objectTypes);
            }
         }
      }
   }

   private void createTransitions(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getSymbol() instanceof ActivitySymbol)
            {
               occurence.connect(model, diagram, objectTypes);
            }
         }
      }
   }

   private void createAnnotationLinks(IModel model, int[] objectTypes)
   {
      for (Iterator i = elements(); i.hasNext();)
      {
         ArisElement element = (ArisElement) i.next();
         if (element instanceof ObjectOccurence)
         {
            ObjectOccurence occurence = (ObjectOccurence) element;
            if (occurence.getSymbol() instanceof AnnotationSymbol)
            {
               occurence.connect(model, diagram, objectTypes);
            }
         }
      }
   }
}
