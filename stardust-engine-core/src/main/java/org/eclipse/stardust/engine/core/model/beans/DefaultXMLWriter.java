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
package org.eclipse.stardust.engine.core.model.beans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.GroupSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Symbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.SymbolOwner;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultXMLWriter implements XMLWriter, XMLConstants
{
   public static final Logger trace = LogManager.getLogger(DefaultXMLWriter.class);

   private boolean includeDiagrams;
   private NodeFactory nodeFactory;
   public static final String CDATA_ELEMENTS = DESCRIPTION + " " + ANNOTATION_SYMBOL
         + " " + EXPRESSION;

   public DefaultXMLWriter(boolean includeDiagrams)
   {
      this.includeDiagrams = includeDiagrams;
   }

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
            appendChild(parent, convertToNode((GroupSymbol) symbol));
         }
         else if (symbol instanceof AnnotationSymbol)
         {
            appendChild(parent, nodeFactory.createAnnotationSymbolElement((AnnotationSymbol) symbol));
         }
         else if (symbol instanceof RoleSymbol)
         {
            appendChild(parent, nodeFactory.createRoleSymbolElement((RoleSymbol) symbol));
         }
         else if (symbol instanceof ProcessDefinitionSymbol)
         {
            appendChild(parent, nodeFactory.createProcessDefinitionSymbolElement((ProcessDefinitionSymbol) symbol));
         }
         else if (symbol instanceof OrganizationSymbol)
         {
            appendChild(parent, nodeFactory.createOrganizationSymbolElement((OrganizationSymbol) symbol));
         }
         else if (symbol instanceof ModelerSymbol)
         {
            appendChild(parent, nodeFactory.createModelerSymbolElement((ModelerSymbol) symbol));
         }
         else if (symbol instanceof ConditionalPerformerSymbol)
         {
            appendChild(parent, nodeFactory.createConditionalPerformerSymbolElement((ConditionalPerformerSymbol) symbol));
         }
         else if (symbol instanceof DataSymbol)
         {
            appendChild(parent, nodeFactory.createDataSymbolElement((DataSymbol) symbol));
         }
         else if (symbol instanceof ApplicationSymbol)
         {
            appendChild(parent, nodeFactory.createApplicationSymbolElement((ApplicationSymbol) symbol));
         }
         else if (symbol instanceof ActivitySymbol)
         {
            appendChild(parent, nodeFactory.createActivitySymbolElement((ActivitySymbol) symbol));
         }
         else if (symbol instanceof DataMappingConnection)
         {
            appendChild(parent, nodeFactory.createDataMappingConnectionElement((DataMappingConnection) symbol));
         }
         else if (symbol instanceof ExecutedByConnection)
         {
            appendChild(parent, nodeFactory.createExecutedByConnectionElement((ExecutedByConnection) symbol));
         }
         else if (symbol instanceof PartOfConnection)
         {
            appendChild(parent, nodeFactory.createPartOfConnectionElement((PartOfConnection) symbol));
         }
         else if (symbol instanceof PerformsConnection)
         {
            appendChild(parent, nodeFactory.createPerformConnectionElement((PerformsConnection) symbol));
         }
         else if (symbol instanceof SubProcessOfConnection)
         {
            appendChild(parent, nodeFactory.createSubProcessOfConnectionElement((SubProcessOfConnection) symbol));
         }
         else if (symbol instanceof RefersToConnection)
         {
            appendChild(parent, nodeFactory.createRefersToConnectionElement((RefersToConnection) symbol));
         }
         else if (symbol instanceof WorksForConnection)
         {
            appendChild(parent, nodeFactory.createWorksForConnectionElement((WorksForConnection) symbol));
         }
         else if (symbol instanceof TransitionConnection)
         {
            appendChild(parent, nodeFactory.createTransitionConnectionElement((TransitionConnection) symbol));
         }
         else if (symbol instanceof GenericLinkConnection)
         {
            appendChild(parent, nodeFactory.createGenericLinkConnectionElement((GenericLinkConnection) symbol));
         }
         else
         {
            // @todo (france, ub): warn
         }
      }
   }

   public Node convertToNode(IActivity activity)
   {
      Node node = nodeFactory.createActivityElement(activity);

      for (Iterator i = activity.getAllDataMappings(); i.hasNext();)
      {
         appendChild(node, nodeFactory.createDataMappingElement((IDataMapping) i.next()));
      }

      for (Iterator i = activity.getAllEventHandlers(); i.hasNext();)
      {
         appendChild(node, convertToNode((IEventHandler) i.next()));
      }

      return node;
   }

   public Node convertToNode(IApplication application)
   {

      Node applicationElement = nodeFactory.createApplicationElement(application);

      if (application.isInteractive())
      {
         Iterator contexts = application.getAllContexts();
         while (contexts.hasNext())
         {
            IApplicationContext context = (IApplicationContext) contexts.next();
            // @todo (france, ub): access points (not used at the moment)
            appendChild(applicationElement, nodeFactory.createApplicationContextElement(context));
         }
      }
      else
      {
         IApplicationType applicationType = (IApplicationType) application.getType();

         if (applicationType != null)
         {

            for (Iterator i = application.getAllPersistentAccessPoints();i.hasNext();)
            {
               AccessPoint accessPoint = (AccessPoint) i.next();
               appendChild(applicationElement, nodeFactory.createAccessPointElement(accessPoint));
            }
         }
      }

      return applicationElement;
   }

   public Node convertToNode(ITrigger trigger)
   {
      Node triggerElement = nodeFactory.createTriggerElement(trigger);

      for (Iterator i = trigger.getAllPersistentAccessPoints(); i.hasNext();)
      {
         IAccessPoint accessPoint = (IAccessPoint) i.next();
         appendChild(triggerElement, nodeFactory.createAccessPointElement(accessPoint));
      }

      for (Iterator i = trigger.getAllParameterMappings(); i.hasNext();)
      {
         IParameterMapping mapping = (IParameterMapping) i.next();
         Node mappingNode = nodeFactory.createParameterMappingElement(mapping);
         appendChild(triggerElement, mappingNode);
      }
      return triggerElement;
   }

   public Node convertToNode(IModel model)
   {
      Node modelNode = nodeFactory.createModelElement(model);

      for (Iterator i = model.getAllDataTypes(); i.hasNext();)
      {
         IDataType type = (IDataType) i.next();
         appendChild(modelNode, nodeFactory.createDataTypeElement(type));
      }

      for (Iterator i = model.getAllApplicationTypes(); i.hasNext();)
      {
         IApplicationType type = (IApplicationType) i.next();
         appendChild(modelNode, nodeFactory.createApplicationTypeElement(type));
      }

      for (Iterator i = model.getAllApplicationContextTypes(); i.hasNext();)
      {
         IApplicationContextType type = (IApplicationContextType) i.next();
         appendChild(modelNode, nodeFactory.createApplicationContextTypeElement(type));
      }

      for (Iterator i = model.getAllTriggerTypes(); i.hasNext();)
      {
         ITriggerType type = (ITriggerType) i.next();
         appendChild(modelNode, nodeFactory.createTriggerTypeElement(type));
      }

      for (Iterator i = model.getAllEventConditionTypes(); i.hasNext();)
      {
         IEventConditionType type = (IEventConditionType) i.next();
         appendChild(modelNode, nodeFactory.createEventConditionTypeElement(type));
      }

      for (Iterator i = model.getAllEventActionTypes(); i.hasNext();)
      {
         IEventActionType type = (IEventActionType) i.next();
         appendChild(modelNode, nodeFactory.createEventActionTypeElement(type));
      }

      for (Iterator i = model.getAllData(); i.hasNext();)
      {
         appendChild(modelNode, nodeFactory.createDataElement((IData) i.next()));
      }

      for (Iterator i = model.getAllApplications(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IApplication) i.next()));
      }

      for (Iterator i = model.getAllModelers(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IModelParticipant) i.next()));
      }

      for (Iterator i = model.getAllRoles(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IModelParticipant) i.next()));
      }

      for (Iterator i = model.getAllOrganizations(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IModelParticipant) i.next()));
      }

      for (Iterator i = model.getAllConditionalPerformers(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IModelParticipant) i.next()));
      }

      for (Iterator i = model.getAllProcessDefinitions(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IProcessDefinition) i.next()));
      }

      appendChild(modelNode, nodeFactory.createScripting(model.getScripting()));

      if (includeDiagrams)
      {
         for (Iterator i = model.getAllDiagrams(); i.hasNext();)
         {
            appendChild(modelNode, convertDiagram((Diagram) i.next()));
         }
      }

      for (Iterator i = model.getAllLinkTypes(); i.hasNext();)
      {
         appendChild(modelNode, nodeFactory.createLinkTypeElement((ILinkType) i.next()));
      }

      for (Iterator i = model.getAllViews(); i.hasNext();)
      {
         appendChild(modelNode, convertToNode((IView) i.next()));
      }

      return modelNode;
   }

   public Node convertToNode(IModelParticipant participant)
   {

      Node node = null;

      if (participant instanceof IModeler)
      {
         node = nodeFactory.createModelerElement((IModeler) participant);
      }
      else if (participant instanceof IRole)
      {
         node = nodeFactory.createRoleElement((IRole) participant);
      }
      else if (participant instanceof IConditionalPerformer)
      {
         node = nodeFactory.createConditionalPerformerElement((IConditionalPerformer) participant);
      }
      else if (participant instanceof IOrganization)
      {
         node = nodeFactory.createOrganizationElement((IOrganization) participant);

         // Write the associated perticipants
         for (Iterator i = participant.getAllParticipants(); i.hasNext();)
         {
            appendChild(node, nodeFactory.attachParticipantElement((IModelParticipant) i.next()));
         }
      }
      else
      {
         // @todo (france, ub): warn
      }

      return node;
   }
   public Node convertToNode(IEventHandler handler)
   {
      Node node = nodeFactory.createEventHandlerElement(handler);
      for (Iterator j = handler.getAllBindActions(); j.hasNext();)
      {
         IBindAction action = (IBindAction) j.next();
         appendChild(node, nodeFactory.createBindActionElement(action));
      }
      for (Iterator j = handler.getAllEventActions(); j.hasNext();)
      {
         IEventAction action = (IEventAction) j.next();
         appendChild(node, nodeFactory.createEventActionElement(action));
      }
      for (Iterator j = handler.getAllUnbindActions(); j.hasNext();)
      {
         IUnbindAction action = (IUnbindAction) j.next();
         appendChild(node, nodeFactory.createUnbindActionElement(action));
      }
      return node;
   }

   public Node convertToNode(IProcessDefinition process)
   {
      Node node = nodeFactory.createProcessDefinitionElement(process);

      for (Iterator i = process.getAllActivities(); i.hasNext();)
      {
         appendChild(node, convertToNode((IActivity) i.next()));
      }

      for (Iterator i = process.getAllTransitions(); i.hasNext();)
      {
         appendChild(node, nodeFactory.createTransitionElement((ITransition) i.next()));
      }

      for (Iterator i = process.getAllTriggers(); i.hasNext();)
      {
         appendChild(node, convertToNode((ITrigger) i.next()));
      }

      for (Iterator i = process.getAllDataPaths(); i.hasNext();)
      {
         appendChild(node, nodeFactory.createDataPathElement((IDataPath) i.next()));
      }

      for (Iterator i = process.getAllEventHandlers(); i.hasNext();)
      {
         IEventHandler eventHandler = (IEventHandler) i.next();
         appendChild(node, convertToNode(eventHandler));
      }

      if (includeDiagrams)
      {
         for (Iterator i = process.getAllDiagrams(); i.hasNext();)
         {
            appendChild(node, convertDiagram((Diagram) i.next()));
         }
      }

      return node;
   }

   public Node convertToNode(IView view)
   {
      Node node = nodeFactory.createViewElement(view);

      for (Iterator i = view.getAllViews(); i.hasNext();)
      {
         appendChild(node, convertToNode((IView) i.next()));
      }

      for (Iterator i = view.getAllViewables(); i.hasNext();)
      {
         IViewable _viewable = (IViewable) i.next();
         appendChild(node, nodeFactory.attachViewableElement(_viewable));
      }

      return node;
   }

   private void appendChild(Node parent, Node child)
   {
      if (child!=null)
      {
         parent.appendChild(child);
      }
   }

   private void warn(int severity, String message, Exception exception, ModelElement scope)
   {
      if (exception != null)
      {
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message, exception);
      }
      else
      {
         trace.debug("", new Exception());
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message);
      }
   }

   /**
    * Writes the model to the file using XML-Format.
    */
   public void exportAsXML(IModel model, File file)
   {
      exportAsXML(model, null, file);
   }

   /**
    * Writes the model to the file using XML-Format.
    */
   public void exportAsXML(IModel model, Transformer transformer, File file)
   {
      try
      {
         FileOutputStream outStream = new FileOutputStream(file);
         exportAsXML(model, transformer, outStream);
         outStream.close();
      }
      catch (IOException x)
      {
         throw new PublicException(BpmRuntimeError.MDL_CANNOT_WRITE_TO_FILE.raise(file
               .getName()));
      }
   }

   /**
    * Writes the model to the OutputStream using XML-Format.
    */
   public void exportAsXML(IModel model, OutputStream stream)
   {
      exportAsXML(model, null, stream);
   }

   /**
    * Writes the model to the OutputStream using XML-Format.
    */
   public void exportAsXML(IModel model, Transformer transformer, OutputStream stream)
   {
      Document document = XmlUtils.newDomBuilder(true, true).newDocument();
      exportAsXML(model, document);
      XmlUtils.serialize(document, transformer, new StreamResult(stream),
            XMLConstants.ENCODING_ISO_8859_1, 3, null, null);
   }

   /**
    * Exports the model elements into the provided document.
    */
   public void exportAsXML(IModel model, Document document)
   {
      nodeFactory = (NodeFactory) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{NodeFactory.class},
            new InvocationManager(document));

      trace.info("Exporting model with id '" + model.getId() + "'.");

      document.appendChild(convertToNode(model));
   }

   private class InvocationManager implements InvocationHandler
   {
      NodeFactory factory;
      Document document;

      public InvocationManager(Document document)
      {
         factory = new NodeFactoryImpl(document);
         this.document = document;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         ModelElement modelElement = null;
         if (args[0] instanceof ModelElement)
         {
            modelElement = (ModelElement) args[0];
            if (modelElement.getElementOID() < 0)
            {
               return null;
            }
         }

         Object result = method.invoke(factory, args);

         if (modelElement != null && method.getName().startsWith("create"))
         {
            Element element = ((Element) result);
            element.setAttribute(XMLConstants.OID_ATT, String.valueOf(modelElement.getElementOID()));
            if (modelElement.isPredefined())
            {
               element.setAttribute(PREDEFINED_ATT, "true");
            }
            if (modelElement instanceof IdentifiableElement)
            {
               element.setAttribute(ID_ATT, ((IdentifiableElement)modelElement).getId());
               element.setAttribute(NAME_ATT, ((IdentifiableElement)modelElement).getName());
            }
            NodeWriter writer = new NodeWriter(element);
            writer.appendChildElement(DESCRIPTION, modelElement.getDescription());

            for (Iterator i = modelElement.getAllAttributes().entrySet().iterator();i.hasNext();)
            {
               Map.Entry attribute = (Map.Entry) i.next();
               Object value = attribute.getValue();
               if (value == null)
               {
                  continue;
               }

               Element node = document.createElementNS(
                     XMLConstants.NS_CARNOT_WORKFLOWMODEL_31, ATTRIBUTE);
               NodeWriter attWriter = new NodeWriter(node);

               String name = (String) attribute.getKey();
               attWriter.writeAttribute(NAME_ATT, name);

               if (PredefinedConstants.XPDL_EXTENDED_ATTRIBUTES.equals(name)
                     && (value instanceof DocumentFragment))
               {
                  node.appendChild(node.getOwnerDocument().importNode(
                        (DocumentFragment) value, true));
               }
               else
               {
                  attWriter.writeAttribute(CLASS_ATT,
                        Reflect.getAbbreviatedName(value.getClass()));
                  String valueString = Reflect.convertObjectToString(value);
                  if (valueString.indexOf("\n") != -1)
                  {
                     attWriter.appendChildElement(VALUE, valueString);
                  }
                  else
                  {
                     attWriter.writeAttribute(VALUE_ATT, valueString);
                  }

               }

               appendChild(element, node);
            }
         }

         return result;
      }

   }

}
