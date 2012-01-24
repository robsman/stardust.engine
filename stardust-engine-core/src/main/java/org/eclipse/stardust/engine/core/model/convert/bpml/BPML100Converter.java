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
package org.eclipse.stardust.engine.core.model.convert.bpml;

import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.model.convert.Converter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author kberberich
 * @version $Revision$
 */
public class BPML100Converter extends Converter
{
   private static final Logger trace = LogManager.getLogger(BPML100Converter.class);

   private static final String STD_DESCR = "Created by Infinity BPML Import.";
   private static final String PROCESS_NODE_NAME = "process";

   // these are all bpml activity types allowed by the BPML 1.0 specification
   // simple activity types
   private static final String ACTIVITY_ACTION_TYPE = "action";
   private static final String ACTIVITY_ASSIGN_TYPE = "assign";
   private static final String ACTIVITY_CALL_TYPE = "call";
   private static final String ACTIVITY_COMPENSATE_TYPE = "compensate";
   private static final String ACTIVITY_DELAY_TYPE = "delay";
   private static final String ACTIVITY_EMPTY_TYPE = "empty";
   private static final String ACTIVITY_FAULT_TYPE = "fault";
   private static final String ACTIVITY_RAISE_TYPE = "raise";
   private static final String ACTIVITY_SPAWN_TYPE = "spawn";
   private static final String ACTIVITY_SYNCH_TYPE = "synch";
   // complex activity types
   private static final String ACTIVITY_ALL_TYPE = "all";
   private static final String ACTIVITY_CHOICE_TYPE = "choice";
   private static final String ACTIVITY_FOREACH_TYPE = "foreach";
   private static final String ACTIVITY_SEQUENCE_TYPE = "sequence";
   private static final String ACTIVITY_SWITCH_TYPE = "switch";
   private static final String ACTIVITY_UNTIL_TYPE = "until";
   private static final String ACTIVITY_WHILE_TYPE = "while";

   private static final String PROCESS_ATTRIBUTE = "process";

   private int mode;
   private IActivity lastActivity;
   private Hashtable nameToProcess;
   private Collection subProcessActivities;

   public BPML100Converter(int mode)
   {
      super();
      this.mode = mode;
      nameToProcess = new Hashtable();
      subProcessActivities = new Vector();
   }

   public IModel convert(InputStream inputStream)
   {
      try
      {
         Document document = getDocumentFromInputStream(inputStream);
         model = DefaultModelBuilder.create().createModel("name", "name", "description");
         trace.debug("Processing roles/organizations.");

         NodeList elements = document.getElementsByTagName(PROCESS_NODE_NAME);

         for (int n = 0; n < elements.getLength(); n++)
         {
            lastActivity = null;
            Node node = elements.item(n);
            IProcessDefinition process = getProcessFromNode(node);
         }

         for (Iterator iterator = subProcessActivities.iterator(); iterator.hasNext();)
         {
            IActivity activity = (IActivity) iterator.next();
            // @todo find out how bpml references processes (tns:...?)
            IProcessDefinition subProcess = (IProcessDefinition) nameToProcess.get(
                  activity.getName());

            if (null == subProcess)
            {
               activity.setImplementationType(ImplementationType.Route);
               ConvertWarningException warning = new ConvertWarningException(
                     "Could not find subprocess " + activity.getName() + ". Treating " +
                     "activity as route activity.");
               warning.setProcessDefinition(activity.getProcessDefinition());
               converterWarnings.addLast(warning);
            }
            else
            {
               activity.setImplementationProcessDefinition(subProcess);
            }
         }

         populateDefaultDiagrams();
         return model;
      }
      catch (Exception x)
      {
         throw new PublicException("Cannot convert model. ", x);
      }
   }

   private IProcessDefinition getProcessFromNode(Node processNode)
   {
      String name = XMLUtil.getName(processNode);
      IProcessDefinition processDefinition = model.createProcessDefinition(
            name, name, STD_DESCR);
      nameToProcess.put(name, processDefinition);
      NodeList childNodes = processNode.getChildNodes();
      parseActivities(childNodes, processDefinition);
      return processDefinition;
   }

   private void parseActivities(NodeList nodeList, IProcessDefinition processDefinition)
   {
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Node childNode = nodeList.item(i);
         String type = childNode.getNodeName();

         if ((type == null) || ("".equals(type)))
         {
            continue;
         }

         trace.info("Found activiy type " + type);

         try
         {
            if (ACTIVITY_ACTION_TYPE.equals(type))
            {
               parseActionActivity(childNode, processDefinition);
            }
            else if (ACTIVITY_ASSIGN_TYPE.equals(type))
            {
               parseAssignActivity(childNode, processDefinition);
               //trace.info("The activity type " + ACTIVITY_ASSIGN_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_CALL_TYPE.equals((type)))
            {
               parseCallActivity(childNode, processDefinition);
            }
            else if (ACTIVITY_COMPENSATE_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_COMPENSATE_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_DELAY_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_DELAY_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_EMPTY_TYPE.equals((type)))
            {
               parseEmptyActivity(childNode, processDefinition);
            }
            else if (ACTIVITY_FAULT_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_FAULT_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_RAISE_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_RAISE_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_SPAWN_TYPE.equals((type)))
            {

            }
            else if (ACTIVITY_SYNCH_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_SYNCH_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_ALL_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_ALL_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_CHOICE_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_CHOICE_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_FOREACH_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_FOREACH_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_SEQUENCE_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_SEQUENCE_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_UNTIL_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_UNTIL_TYPE + " is not supported yet");
            }
            else if (ACTIVITY_WHILE_TYPE.equals((type)))
            {
               trace.info("The activity type " + ACTIVITY_WHILE_TYPE + " is not supported yet");
            }
         }
         catch (Exception e)
         {
            String message = "Error parsing node of type " + type + " in process "
                  + processDefinition.getName() + ".";
            ConvertWarningException warning = new ConvertWarningException(message + " "
                  + e.getMessage());
            warning.setProcessDefinition(processDefinition);
            trace.warn(message, e);
         }
      }
   }

   private void parseCallActivity(Node childNode, IProcessDefinition processDefinition)
   {
      String processToCall = XMLUtil.getNamedAttribute(childNode, PROCESS_ATTRIBUTE);
      IActivity activity = processDefinition.createActivity(
            processDefinition.getDefaultActivityId(), processToCall, STD_DESCR, 0);
      activity.setImplementationType(ImplementationType.SubProcess);
      createTransition(processDefinition, activity);

      // referenced subprocess might not be parsed yet. referentiation will be done later.
      subProcessActivities.add(activity);
      lastActivity = activity;
   }

   private void parseAssignActivity(Node childNode, IProcessDefinition processDefinition)
   {
      // not yet properly implemented...redirecting to type EMPTY
      // @todo implement assign activity properly
      parseEmptyActivity(childNode, processDefinition);
   }

   private void parseEmptyActivity(Node childNode, IProcessDefinition processDefinition)
   {
      IActivity activity = createActivityFromNode(childNode, processDefinition);
      activity.setImplementationType(ImplementationType.Route);
      createTransition(processDefinition, activity);
      lastActivity = activity;
   }

   private void parseActionActivity(Node childNode, IProcessDefinition processDefinition)
   {
      // not yet properly implemented...redirecting to type EMPTY
      // @todo implement action activity properly
      parseEmptyActivity(childNode, processDefinition);
   }

   private void createTransition(IProcessDefinition processDefinition, IActivity activity)
   {
      if (lastActivity != null)
      {
         String tId = processDefinition.getDefaultTransitionId();
         ITransition t = processDefinition.createTransition(tId, tId, STD_DESCR,
               lastActivity, activity);
      }
   }

   private IActivity createActivityFromNode(Node childNode, IProcessDefinition processDefinition)
   {
      String name = XMLUtil.getName(childNode);
      if (name == null)
      {
         name = "Undefined";
      }
      String id = processDefinition.getDefaultActivityId();
      IActivity activity = processDefinition.createActivity(id, name, STD_DESCR, 0);
      //processDefinition.addToActivities(activity);
      return activity;
   }
}
