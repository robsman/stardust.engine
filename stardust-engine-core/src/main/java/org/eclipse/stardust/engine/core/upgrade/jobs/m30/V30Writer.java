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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.xml.XmlUtils;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class V30Writer
{
   private static final Logger trace = LogManager.getLogger(V30Writer.class);
   
   private INodeFactory nodeFactory;

   public Node convertDiagram(Diagram diagram)
   {
      Node node = nodeFactory.createDiagramElement(diagram);
      attachSymbols(node, diagram);
      return node;
   }

   public Node convertToNode(GroupSymbol symbol)
   {
      Node node = nodeFactory.createGroupSymbolElement(symbol);
      attachSymbols(node, symbol);
      return node;
   }

   protected void attachSymbols(Node parent, SymbolOwner owner)
   {
      Iterator symbolList = owner.getAllSymbols();

      while (symbolList.hasNext())
      {
         Symbol symbol = (Symbol) symbolList.next();

         if (symbol instanceof GroupSymbol)
         {
            parent.appendChild(convertToNode((GroupSymbol) symbol));
         }
         else if (symbol instanceof AnnotationSymbol)
         {
            parent.appendChild(nodeFactory.createAnnotationSymbolElement((AnnotationSymbol) symbol));
         }
         else if (symbol instanceof RoleSymbol)
         {
            parent.appendChild(nodeFactory.createRoleSymbolElement((RoleSymbol) symbol));
         }
         else if (symbol instanceof ProcessDefinitionSymbol)
         {
            parent.appendChild(nodeFactory.createProcessDefinitionSymbolElement((ProcessDefinitionSymbol) symbol));
         }
         else if (symbol instanceof OrganizationSymbol)
         {
            parent.appendChild(nodeFactory.createOrganizationSymbolElement((OrganizationSymbol) symbol));
         }
         else if (symbol instanceof ModelerSymbol)
         {
            parent.appendChild(nodeFactory.createModelerSymbolElement((ModelerSymbol) symbol));
         }
         else if (symbol instanceof ConditionalPerformerSymbol)
         {
            parent.appendChild(nodeFactory.createConditionalPerformerSymbolElement((ConditionalPerformerSymbol) symbol));
         }
         else if (symbol instanceof DataSymbol)
         {
            parent.appendChild(nodeFactory.createDataSymbolElement((DataSymbol) symbol));
         }
         else if (symbol instanceof ApplicationSymbol)
         {
            parent.appendChild(nodeFactory.createApplicationSymbolElement((ApplicationSymbol) symbol));
         }
         else if (symbol instanceof ActivitySymbol)
         {
            parent.appendChild(nodeFactory.createActivitySymbolElement((ActivitySymbol) symbol));
         }
         else if (symbol instanceof DataMappingConnection)
         {
            parent.appendChild(nodeFactory.createDataMappingConnectionElement((DataMappingConnection) symbol));
         }
         else if (symbol instanceof TransitionConnection)
         {
            parent.appendChild(nodeFactory.createTransitionConnectionElement((TransitionConnection) symbol));
         }
         else if (symbol instanceof GenericLinkConnection)
         {
            parent.appendChild(nodeFactory.createGenericLinkConnectionElement((GenericLinkConnection) symbol));
         }
         else if (symbol instanceof Connection)
         {
            String name = ((Connection) symbol).getName();
            if (name.equals(XMLConstants.EXECUTED_BY_CONNECTION))
            {
               parent.appendChild(nodeFactory.createExecutedByConnectionElement((Connection) symbol));
            }
            else if (name.equals(XMLConstants.PART_OF_CONNECTION))
            {
               parent.appendChild(nodeFactory.createPartOfConnectionElement((Connection) symbol));
            }
            else if (name.equals(XMLConstants.PERFORMS_CONNECTION))
            {
               parent.appendChild(nodeFactory.createPerformsConnectionElement((Connection) symbol));
            }
            else if (name.equals(XMLConstants.WORKS_FOR_CONNECTION))
            {
               parent.appendChild(nodeFactory.createWorksForConnectionElement((Connection) symbol));
            }
            else if (name.equals(XMLConstants.SUBPROCESS_OF_CONNECTION))
            {
               parent.appendChild(nodeFactory.createSubProcessOfConnectionElement((Connection) symbol));
            }
            else if (name.equals(XMLConstants.REFERS_TO_CONNECTION))
            {
               parent.appendChild(nodeFactory.createRefersToConnectionElement((Connection) symbol));
            }
            else
            {
               // @todo (france, ub): warn
               trace.warn("Unknown connection: " + name);
            }
         }
         else
         {
            // @todo (france, ub): warn
            trace.warn("Unknown symbol: " + symbol);
         }
      }
   }

   public Node convertToNode(Activity activity)
   {
      Node node = nodeFactory.createActivityElement(activity);
      for (Iterator i = activity.getAllEventHandlers(); i.hasNext();)
      {
         node.appendChild(convertToNode((EventHandler) i.next()));
      }

      for (Iterator i = activity.getAllDataMappings(); i.hasNext();)
      {
         node.appendChild(nodeFactory.createDataMappingElement((DataMapping) i.next()));
      }

      return node;
   }

   public Node convertToNode(EventHandler handler)
   {
      Node node = nodeFactory.createEventHandlerElement(handler);
      for (Iterator i = handler.getAllActions(); i.hasNext();)
      {
         EventAction action = (EventAction) i.next();
         node.appendChild(nodeFactory.createEventActionElement(action));
      }
      return node;
   }

   public Node convertToNode(Application application)
   {

      Node applicationElement = nodeFactory.createApplicationElement(application);

      if (application.isInteractive())
      {
         Iterator contexts = application.getAllContexts();
         while (contexts.hasNext())
         {
            ApplicationContext context = (ApplicationContext) contexts.next();
            applicationElement.appendChild(nodeFactory.createApplicationContextElement(context));
         }
      }
      else
      {

         Iterator iterator = application.getAllAccessPoints();
         while (iterator.hasNext())
         {
            AccessPoint accessPoint = (AccessPoint) iterator.next();
            applicationElement.appendChild(nodeFactory.createAccessPointElement(accessPoint));
         }
      }

      return applicationElement;
   }

   public Node convertToNode(Trigger trigger)
   {
      Node triggerElement = nodeFactory.createTriggerElement(trigger);

      for (Iterator i = trigger.getAllAccessPoints(); i.hasNext();)
      {
         AccessPoint accessPoint = (AccessPoint) i.next();
         triggerElement.appendChild(nodeFactory.createAccessPointElement(accessPoint));
      }

      for (Iterator i = trigger.getAllParameterMappings(); i.hasNext();)
      {
         ParameterMapping mapping = (ParameterMapping) i.next();
         Node mappingNode = nodeFactory.createParameterMappingElement(mapping);
         triggerElement.appendChild(mappingNode);
      }
      return triggerElement;
   }

   public Document write(Model model)
   {
      Document document = XmlUtils.newDocument();

      nodeFactory = (INodeFactory) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{INodeFactory.class},
            new InvocationManager(document));

      document.appendChild(convertToNode(model));
      return document;
   }

   public Node convertToNode(Model model)
   {
      Node modelNode = nodeFactory.createModelElement(model);

      for (Iterator i = model.getAllDataTypes(); i.hasNext();)
      {
         DataType type = (DataType) i.next();
         modelNode.appendChild(nodeFactory.createDataTypeElement(type));
      }

      for (Iterator i = model.getAllApplicationTypes(); i.hasNext();)
      {
         ApplicationType type = (ApplicationType) i.next();
         modelNode.appendChild(nodeFactory.createApplicationTypeElement(type));
      }

      for (Iterator i = model.getAllApplicationContextTypes(); i.hasNext();)
      {
         ApplicationContextType type = (ApplicationContextType) i.next();
         modelNode.appendChild(nodeFactory.createApplicationContextTypeElement(type));
      }

      for (Iterator i = model.getAllTriggerTypes(); i.hasNext();)
      {
         TriggerType type = (TriggerType) i.next();
         modelNode.appendChild(nodeFactory.createTriggerTypeElement(type));
      }

      for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
      {
         EventConditionType type = (EventConditionType) i.next();
         modelNode.appendChild(nodeFactory.createEventConditionTypeElement(type));
      }

      for (Iterator i = model.getAllEventActionTypes(); i.hasNext();)
      {
         EventActionType type = (EventActionType) i.next();
         modelNode.appendChild(nodeFactory.createEventActionTypeElement(type));
      }

      for (Iterator i = model.getAllData(); i.hasNext();)
      {
         modelNode.appendChild(nodeFactory.createDataElement((Data) i.next()));
      }

      for (Iterator i = model.getAllApplications(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((Application) i.next()));
      }

      for (Iterator i = model.getAllModelers(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((Modeler) i.next()));
      }

      for (Iterator i = model.getAllRoles(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((Role) i.next()));
      }

      for (Iterator i = model.getAllOrganizations(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((Organization) i.next()));
      }

      for (Iterator i = model.getAllConditionalPerformsers(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((ConditionalPerformer) i.next()));
      }

      for (Iterator i = model.getAllProcessDefinitions(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((ProcessDefinition) i.next()));
      }

      for (Iterator i = model.getAllDiagrams(); i.hasNext();)
      {
         modelNode.appendChild(convertDiagram((Diagram) i.next()));
      }

      for (Iterator i = model.getAllLinkTypes(); i.hasNext();)
      {
         modelNode.appendChild(nodeFactory.createLinkTypeElement((LinkType) i.next()));
      }

      for (Iterator i = model.getAllTopLevelViews(); i.hasNext();)
      {
         modelNode.appendChild(convertToNode((View) i.next()));
      }

      return modelNode;
   }

   public Node convertToNode(Role participant)
   {
      return nodeFactory.createRoleElement(participant);
   }

   public Node convertToNode(Organization participant)
   {

      Node node = nodeFactory.createOrganizationElement(participant);

      for (Iterator i = participant.getAllParticipants(); i.hasNext();)
      {
         node.appendChild(nodeFactory.attachAssociatedParticipantElement((String) i.next()));
      }

      return node;
   }

   public Node convertToNode(Modeler participant)
   {
      return nodeFactory.createModelerElement(participant);
   }

   public Node convertToNode(ConditionalPerformer participant)
   {

      return nodeFactory.createConditionalPerformerElement(participant);
   }

   public Node convertToNode(ProcessDefinition process)
   {
      Node node = nodeFactory.createProcessDefinitionElement(process);

      for (Iterator i = process.getAllActivities(); i.hasNext();)
      {
         node.appendChild(convertToNode((Activity) i.next()));
      }

      for (Iterator i = process.getAllTransitions(); i.hasNext();)
      {
         node.appendChild(nodeFactory.createTransitionElement((Transition) i.next()));
      }

      for (Iterator i = process.getAllTriggers(); i.hasNext();)
      {
         node.appendChild(convertToNode((Trigger) i.next()));
      }

      for (Iterator i = process.getAllDataPaths(); i.hasNext();)
      {
         node.appendChild(nodeFactory.createDataPathElement((DataPath) i.next()));
      }

      for (Iterator i = process.getAllEventHandlers(); i.hasNext();)
      {
         node.appendChild(convertToNode((EventHandler) i.next()));
      }

      for (Iterator i = process.getAllDiagrams(); i.hasNext();)
      {
         node.appendChild(convertDiagram((Diagram) i.next()));
      }

      return node;
   }

   public Node convertToNode(View view)
   {
      Node node = nodeFactory.createViewElement(view);

      for (Iterator i = view.getAllSubViews(); i.hasNext();)
      {
         node.appendChild(convertToNode((View) i.next()));
      }

      for (Iterator i = view.getAllViewables(); i.hasNext();)
      {
         node.appendChild(nodeFactory.attachViewableElement((ModelElement) i.next()));
      }

      return node;
   }

   private class InvocationManager implements InvocationHandler
   {
      NodeFactory factory;
      Document document;

      public InvocationManager(Document document)
      {
         factory = new NodeFactory(document);
         this.document = document;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         Object result = null;
         try
         {
            result = method.invoke(factory, args);
            if (method.getName().startsWith("create"))
            {
               ModelElement modelElement = (ModelElement) args[0];
               Element element = ((Element) result);
               int elementOID = ((ModelElement) args[0]).getElementOID();
               element.setAttribute(XMLConstants.OID_ATT, String.valueOf(elementOID));
               if (modelElement.isPredefined())
               {
                  element.setAttribute(XMLConstants.PREDEFINED_ATT, "true");
               }
               if (modelElement instanceof IdentifiableElement)
               {
                  element.setAttribute(XMLConstants.ID_ATT, ((IdentifiableElement)modelElement).getId());
                  element.setAttribute(XMLConstants.NAME_ATT, ((IdentifiableElement)modelElement).getName());
               }
               NodeWriter writer = new NodeWriter(element);
               writer.appendChildElement(XMLConstants.DESCRIPTION, modelElement.getDescription());
               addAttributes((Node) result, (ModelElement) args[0]);
            }
         }
         catch (Exception e)
         {
            trace.warn("", e);
         }
         return result;
      }

      private void addAttributes(Node parent, ModelElement element)
      {

         for (Iterator i = element.getAllAttributes(); i.hasNext();)
         {
            Attribute attribute = (Attribute) i.next();
            appendPropertyNode(parent, XMLConstants.ATTRIBUTE,
                  attribute.getName(), attribute.getClassName(), attribute.getValue());
         }
      }

      private void appendPropertyNode(Node parent,
            String elementName, String name, String type, String value)
      {
         if (StringUtils.isEmpty(value))
         {
            return;
         }

         Element node = document.createElement(elementName);
         NodeWriter writer = new NodeWriter(node);

         writer.writeAttribute(XMLConstants.NAME_ATT, name);
         writer.writeAttribute(XMLConstants.CLASS_ATT, type);
         writer.writeAttribute(XMLConstants.VALUE_ATT, value);

         parent.appendChild(node);
      }
   }

}
