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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.model.XMLReader;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.compatibility.diagram.ConnectionSymbol;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.compatibility.diagram.NodeSymbol;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.gui.*;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.IConfigurationVariableDefinition;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.xsd.*;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.w3c.dom.*;
import org.xml.sax.*;



// @todo (france, ub): (reader and writer): homogenously treat the common attributes:
// id, oid, name, description, predefined??

// @todo (france, ub): implement two pass reading for setting references

/**
 * Converts a model representation in CARNOT XML format to the internal representation.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultXMLReader implements XMLReader, XMLConstants
{
   private static final String DEFAULT_NS_PREFIX = "";

   private static final Logger trace = LogManager.getLogger(DefaultXMLReader.class);

   private static final String TNS_PREFIX = "tns"; //$NON-NLS-1$
   private static final String BASE_INFINITY_NAMESPACE = "http://www.infinity.com/bpm/model/"; //$NON-NLS-1$
   private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$
   private static final String STANDARD_CARNOT_WORKSPACE = "http://www.carnot.ag/workflowmodel/3.1/struct"; //$NON-NLS-1$
   private static final String MODEL_LOAD_FILTER = "carnot.log.load.filter";

   private static boolean showModelLoadMessages = Parameters.instance().getBoolean(MODEL_LOAD_FILTER, false);

   private boolean includeDiagrams;

   private IModel model;
   private ElementFactory elementFactory;

   private Map subprocesses = new HashMap();
   private List eventHandlers = CollectionUtils.newList();

   private IConfigurationVariablesProvider confVarProvider;

   private Long modelOid;

   public static EntityResolver getCarnotModelEntityResolver()
   {
      final URL xsdURL = DefaultXMLReader.class.getResource(WORKFLOWMODEL_XSD);
      if (xsdURL == null)
      {
         throw new InternalException("Unable to find " + WORKFLOWMODEL_XSD);
      }
      final URL dtdURL = DefaultXMLReader.class.getResource(DTD_NAME);
      if (dtdURL == null)
      {
         throw new InternalException("Unable to find " + DTD_NAME);
      }

      return new RecordingEntityResolver(xsdURL, dtdURL);
   }

   public DefaultXMLReader(boolean includeDiagrams)
   {
      this(includeDiagrams, new DefaultConfigurationVariablesProvider(), null);
   }

   public DefaultXMLReader(boolean includeDiagrams, IConfigurationVariablesProvider confVarProvider)
   {
      this(includeDiagrams, confVarProvider, null);
   }

   public DefaultXMLReader(boolean includeDiagrams, IConfigurationVariablesProvider confVarProvider, Long modelOid)
   {
      this.includeDiagrams = includeDiagrams;
      this.confVarProvider = confVarProvider;
      this.modelOid = modelOid;

      elementFactory = (ElementFactory) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{ElementFactory.class},
            new InvocationManager());
   }

   public void addActivity(IProcessDefinition process, Element node)
   {
      IActivity activity = elementFactory.createActivity(node, process, model, subprocesses);

      NodeList childNodes = node.getChildNodes();
      Set<IQualityAssuranceCode> qualityAssuranceCodes = new HashSet<IQualityAssuranceCode>();
      for (int i = 0, nChildNodes = childNodes.getLength(); i < nChildNodes; i++)
      {
         Node childNode = childNodes.item(i);

         if (EVENT_HANDLER.equals(childNode.getNodeName()))
         {
            // we read the event handlers later, because they may contain
            // references to other model elements
            eventHandlers.add(new Pair(childNode, activity));
         }
         else if (DATA_MAPPING.equals(childNode.getNodeName()))
         {
            elementFactory.createDataMapping(childNode, activity);
         }
         else if (EXTERNAL_REFERENCE.equals(childNode.getNodeName()))
         {
            IReference externalReference = elementFactory.createExternalReference(childNode, (IModel) process.getModel());
            ((ActivityBean) activity).setExternalReference(externalReference);
         }
         else if (QUALITY_ASSURANCE_CODES.equals(childNode.getNodeName()))
         {
            IQualityAssuranceCode code = elementFactory.createQualityAssuranceCode(childNode, activity, model);
            if(code != null)
            {
               qualityAssuranceCodes.add(code);
            }
         }
         else if (ATTRIBUTE.equals(childNode.getNodeName()))
         {
            elementFactory.createQualityAssuranceAttributes(childNode, activity, model);
         }
      }
      activity.setQualityAssuranceCodes(qualityAssuranceCodes);
   }

   public void addApplication(IModel model, Node node)
   {
      IApplication application = elementFactory.createApplication(node, model);

      if (application.isInteractive())
      {
         NodeList children = node.getChildNodes();
         for (int i = 0, nChildren = children.getLength(); i < nChildren; i++)
         {
            Node contextNode = children.item(i);
            if (CONTEXT.equals(contextNode.getNodeName()))
            {
               IApplicationContext context = elementFactory.createApplicationContext(contextNode, application);
               addAccessPoints(context, contextNode);
            }
         }
      }
      else
      {
         addAccessPoints(application, node);
      }
   }

   private void addAccessPoints(AccessPointOwner element, Node node)
   {
      NodeList children = node.getChildNodes();
      for (int i = 0, nChildren = children.getLength(); i < nChildren; i++)
      {
         Node child = children.item(i);
         if (ACCESS_POINT.equals(child.getNodeName()))
         {
            elementFactory.createAccessPoint(child, element);
         }
      }
   }

   protected Collection addDiagramSymbols(Node node,
         Diagram diagram, IModel model, IProcessDefinition process)
   {
      // hint: the process is null if the diagram is associated to the model

      NodeList nodeChildren = node.getChildNodes();

      List newSymbols = new ArrayList(nodeChildren.getLength());

      // Read the Symbols first cause a connection has references to symbols
      // (read the groupsymbols as last symbols cause they contains references)

      for (int i = 0, nNodeChildren = nodeChildren.getLength(); i < nNodeChildren; i++)
      {
         Node child = null;
         try
         {
            child = nodeChildren.item(i);
            String childName = child.getNodeName();
            NodeSymbol symbol;

            if (ACTIVITY_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createActivitySymbol(child, process, diagram);
            }
            else if (ANNOTATION_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createAnnotationSymbol(child, diagram);
            }
            else if (APPLICATION_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createApplicationSymbol(child, model, diagram);
            }
            else if (CONDITIONAL_PERFORMER_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createConditionalPerformerSymbol(child, model, diagram);
            }
            else if (DATA_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createDataSymbol(child, model, diagram);
            }
            else if (MODELER_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createModelerSymbol(child, model, diagram);
            }
            else if (ORGANIZATION_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createOrganizationSymbol(child, model, diagram);
            }
            else if (PROCESS_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createProcessSymbol(child, model, diagram);
            }
            else if (ROLE_SYMBOL.equals(childName))
            {
               symbol = elementFactory.createRoleSymbol(child, model, diagram);
            }
            else
            {
               // @todo (france, ub): warn?!
               symbol = null;
            }

            if (symbol != null)
            {
               newSymbols.add(symbol);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM, "Couldn't create the symbol for node '"
                  + child + "', process definition = '" + process + "':", e, diagram);
         }
      }

      // read the groupsymbols after the other symbols

      for (int i = 0, nNodeChildren = nodeChildren.getLength(); i < nNodeChildren; i++)
      {
         Node child = null;
         try
         {
            child = nodeChildren.item(i);
            String childName = child.getNodeName();

            if (GROUP_SYMBOL.equals(childName))
            {
               Collection childSymbols = addDiagramSymbols(child, diagram, model, process);

               NodeSymbol symbol = elementFactory.createGroupSymbol(child, diagram, model, process, childSymbols);
               newSymbols.add(symbol);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM, "Couldn't create group symbol for node '"
                  + child + "', process definition = '" + process + "':", e, diagram);
         }
      }

      ConnectionSymbol connection;
      // Read all connections except refer-to connections:
      // refer-to connections can refer to other connections
      for (int i = 0, nNodeChildren = nodeChildren.getLength(); i < nNodeChildren; i++)
      {
         Node child = null;
         try
         {
            child = nodeChildren.item(i);
            String childName = child.getNodeName();
            NodeReader reader = new NodeReader(child, DefaultXMLReader.this.confVarProvider);
            if (DATA_MAPPING_CONNECTION.equals(childName))
            {
               connection =
                     elementFactory.createDataMappingConnection(child, diagram, process);
            }
            else if (EXECUTED_BY_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new ExecutedByConnection(), diagram,
                     APPLICATION_SYMBOL_ID, ACTIVITY_SYMBOL_REF);
            }
            else if (PART_OF_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new PartOfConnection(), diagram,
                     SUB_ORGANIZATION_SYMBOL_ID, ORGANIZATION_SYMBOL_ID);
            }
            else if (PERFORMS_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new PerformsConnection(), diagram,
                     PARTICIPANT_SYMBOL_ID, ACTIVITY_SYMBOL_REF);
            }
            else if (WORKS_FOR_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new WorksForConnection(), diagram,
                     PARTICIPANT_SYMBOL_ID, ORGANIZATION_SYMBOL_ID);
            }
            else if (GENERIC_LINK_CONNECTION.equals(childName))
            {

               String linkTypeId = reader.getRawAttribute(LINK_TYPE_REF);
               ILinkType linkType = model.findLinkType(linkTypeId);

               if (linkType == null)
               {
                  warn(ConversionWarning.MEDIUM,
                        "Couldn't lookup link type for generic link connection '."
                        + linkTypeId + "', process definition = '" + process + "':", null, diagram);

                  connection = null;
               }
               else
               {
                  connection = elementFactory.attachConnection(child, new GenericLinkConnection(linkType),
                        diagram, SOURCE_SYMBOL_ID, TARGET_SYMBOL_ID);
               }
            }
            else if (TRANSITION_CONNECTION.equals(childName))
            {
               ITransition transition = process.findTransition(
                     reader.getRawAttribute(TRANSITION_REF));
               connection = elementFactory.attachConnection(child,
                     new TransitionConnection(transition),
                     diagram, SOURCE_ACTIVITY_SYMBOL_ID, TARGET_ACTIVITY_SYMBOL_ID);
            }
            else if (SUBPROCESS_OF_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new SubProcessOfConnection(),
                     diagram, SUB_PROCESS_SYMBOL_ID, PROCESS_SYMBOL_ID);
            }
            else
            {
               connection = null;
            }
            // collect the collection-symbols with its old ID for the refers-to-connections
            if (connection != null)
            {
               newSymbols.add(connection);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM, "Couldn't create connection for node '"
                  + child + "', process definition = '" + process + "':", e, diagram);
         }
      }

      for (int i = 0, nNodeChildren = nodeChildren.getLength(); i < nNodeChildren; i++)
      {
         Node child = null;
         try
         {
            child = nodeChildren.item(i);
            String childName = child.getNodeName();
            if (REFERS_TO_CONNECTION.equals(childName))
            {
               connection = elementFactory.attachConnection(child, new RefersToConnection(),
                     diagram, FROM_ATT, TO);

               if (connection != null)
               {
                  newSymbols.add(connection);
               }
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM, "Couldn't create the symbol for node '"
                  + child + "', process definition = '" + process + "':", e, diagram);
         }
      }
      return newSymbols;
   }

   public void addTrigger(IModel model, IProcessDefinition process, Node node)
   {
      ITrigger trigger = elementFactory.createTrigger(node, process);

      addAccessPoints(trigger, node);

      NodeList children = node.getChildNodes();
      for (int i = 0, nChildren = children.getLength(); i < nChildren; i++)
      {
         Node child = children.item(i);
         if (PARAMETER_MAPPING.equals(child.getNodeName()))
         {
            elementFactory.createParameterMapping(child, trigger);
         }
      }
   }

   public IProcessDefinition addProcess(IModel model, Node node)
   {
      IProcessDefinition process = elementFactory.createProcess(node, model);

      NodeList nodeList = node.getChildNodes();

      int length = nodeList.getLength();

      for (int i = 0; i < length; i++)
      {
         Node child = null;
         try
         {
            child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && ACTIVITY.equals(child.getNodeName()))
            {
               addActivity(process, (Element) child);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM,
                  "Couldn't create activity, node = " + child, e, process);
         }
      }

      for (int i = 0; i < length; i++)
      {
         Node child = null;
         try
         {
            child = nodeList.item(i);
            if (TRANSITION.equals(child.getNodeName()))
            {
               elementFactory.createTransition(child, process);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM,
                  "Couldn't create transition, node = " + child, e, process);
         }
      }

      for (int i = 0; i < length; i++)
      {
         Node child = null;
         try
         {
            child = nodeList.item(i);

            if (DATA_PATH.equals(child.getNodeName()))
            {
               elementFactory.createDataPath(child, process);
            }
            else if (TRIGGER.equals(child.getNodeName()))
            {
               addTrigger(model, process, child);
            }
            else if (EVENT_HANDLER.equals(child.getNodeName()))
            {
               // we read the event handlers later, because they may contain
               // references to other model elements
               eventHandlers.add(new Pair(child, process));
            }
            else if (DIAGRAM.equals(child.getNodeName()) && includeDiagrams)
            {
               Diagram diagram = elementFactory.createDiagram(child, process);
               addDiagramSymbols(child, diagram, (IModel) process.getModel(), process);
            }
            else if (EXTERNAL_REFERENCE.equals(child.getNodeName()))
            {
               IReference externalReference = elementFactory.createExternalReference(child, (IModel) process.getModel());
               ((ProcessDefinitionBean) process).setExternalReference(externalReference);
            }
         }
         catch (Exception e)
         {
            warn(ConversionWarning.MEDIUM,
                  "Couldn't create element, node = " + child, e, process);
         }
      }

      // process interface details
      Map<String, String> mappings = CollectionUtils.newMap();
      NodeList fpms = ((Element) node).getElementsByTagNameNS(NS_CARNOT_XPDL_31, CARNOT_FORMAL_PARAMETER_MAPPINGS);
      for (int i = 0; i < fpms.getLength(); i++)
      {
         Element params = (Element) fpms.item(i);
         NodeList fpm = params.getElementsByTagNameNS(NS_CARNOT_XPDL_31, CARNOT_FORMAL_PARAMETER_MAPPING);
         for (int j = 0; j < fpm.getLength(); j++)
         {
            Element item = (Element) fpm.item(j);
            String fp = item.getAttribute("FormalParameter");
            if (fp.length() > 0)
            {
               mappings.put(fp, item.getAttribute("Data"));
            }
         }
      }
      ((ProcessDefinitionBean) process).setFormalParameterMappings(mappings.isEmpty()
            ? Collections.<String, String>emptyMap() : mappings);

      // since it was introduced with 6.0 there's no need to check for xpdl 1.0 namespace
      NodeList nl1 = ((Element) node).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_FORMAL_PARAMETERS);
      ((ProcessDefinitionBean) process).setDeclaresInterface(nl1.getLength() > 0 && process.getExternalReference() == null);
      for (int i = 0; i < nl1.getLength(); i++)
      {
         Element params = (Element) nl1.item(i);
         NodeList nl2 = params.getElementsByTagNameNS(NS_XPDL_2_1, XPDL_FORMAL_PARAMETER);
         for (int j = 0; j < nl2.getLength(); j++)
         {
            IFormalParameter param = elementFactory.createFormalParameters(nl2.item(j), process);
            ((ProcessDefinitionBean) process).addToFormalParameters(param);
         }
      }

      return process;
   }

   public void addView(IModel model, IView parentView, Node node)
   {
      IView view = elementFactory.createView(node, model, parentView);

      NodeList nodeList = node.getChildNodes();
      if (nodeList != null)
      {
         int length = nodeList.getLength();
         for (int i = 0; i < length; i++)
         {
            Node child = null;
            try
            {
               child = nodeList.item(i);
               if (VIEW.equals(child.getNodeName()))
               {
                  addView(model, view, child);
               }
               else if (VIEWABLE.equals(child.getNodeName()))
               {
                  elementFactory.attachViewable(child, model, view);
               }
            }
            catch (Exception e)
            {
               warn(ConversionWarning.MEDIUM,
                     "Couldn't create element, node = " + child, e, parentView);
            }
         }
      }
   }

   public IModel loadModel(Element node)
   {
      return loadModel(node, false);
   }

   public IModel loadModel(Element node, boolean injectPredefinedConstants)
   {
      // Load model shell and extract configuration variable definitions from it.
      model = elementFactory.createModel(node);

      Set<IConfigurationVariableDefinition> configurationVariableDefinitions = model
            .getConfigurationVariableDefinitions();
      if ( !configurationVariableDefinitions.isEmpty())
      {
         confVarProvider.resetModelId(model.getId());
         for (IConfigurationVariableDefinition def : configurationVariableDefinitions)
         {
            confVarProvider.register(def);
         }

         model = elementFactory.createModel(node);
      }

      if (modelOid != null)
      {
         model.setModelOID(modelOid.intValue());
      }

      try
      {
         NodeList qualityAssurance = node.getElementsByTagNameNS(NS_CARNOT_XPDL_31, QUALITY_ASSURANCE);
         for (int i = 0, nQualityAssurance = qualityAssurance.getLength(); i < nQualityAssurance; i++)
         {
            Element qualityAssuranceNode = (Element) qualityAssurance.item(i);

            IQualityAssurance createQualityAssurance = elementFactory.createQualityAssurance(qualityAssuranceNode, model);
            NodeList nodeList = qualityAssuranceNode.getElementsByTagNameNS(NS_CARNOT_XPDL_31, QUALITY_ASSURANCE_CODE);
            int length = nodeList.getLength();

            for (int j = 0; j < length; j++)
            {
               Node child = nodeList.item(j);
               elementFactory.createQualityAssuranceCode(child, createQualityAssurance);
            }
         }

         NodeList appTypes = node.getOwnerDocument().getElementsByTagName(APPLICATION_TYPE);
         for (int i = 0, nAppTypes = appTypes.getLength(); i < nAppTypes; i++)
         {
            elementFactory.createApplicationType(appTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedApplicationTypes(model);
         }

         NodeList contextTypes = node.getOwnerDocument().getElementsByTagName(APPLICATION_CONTEXT_TYPE);
         for (int i = 0, nContextTypes = contextTypes.getLength(); i < nContextTypes; i++)
         {
            elementFactory.createApplicationContextType(contextTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedApplicationContextTypes(model);
         }

         NodeList dataTypes = node.getElementsByTagName(DATA_TYPE);
         for (int i = 0, nDataTypes = dataTypes.getLength(); i < nDataTypes; i++)
         {
            elementFactory.createDataType(dataTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedDataTypes(model);
         }

         NodeList triggerTypes = node.getOwnerDocument().getElementsByTagName(TRIGGER_TYPE);
         for (int i = 0, nTriggerTypes = triggerTypes.getLength(); i < nTriggerTypes; i++)
         {
            elementFactory.createTriggerType(triggerTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedTriggerTypes(model);
         }

         NodeList eventConditionTypes = node.getOwnerDocument().getElementsByTagName(EVENT_CONDITION_TYPE);
         for (int i = 0, nEventConditionTypes = eventConditionTypes.getLength(); i < nEventConditionTypes; i++)
         {
            elementFactory.createEventConditionType(eventConditionTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedEventConditionTypes(model);
         }

         NodeList eventActionTypes = node.getOwnerDocument().getElementsByTagName(EVENT_ACTION_TYPE);
         for (int i = 0, nEventActionTypes = eventActionTypes.getLength(); i < nEventActionTypes; i++)
         {
            elementFactory.createEventActionType(eventActionTypes.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedEventActionTypes(model);
         }

         addExternalPackages(model, node);

         // there is a great confusion here regarding the namespaces so we'll just ignore namespaces for the moment.
         boolean needsPatching = true;
         List typeDeclarationWrappers = getElementsByLocalName(node, XPDL_TYPE_DECLARATIONS);
         if (typeDeclarationWrappers.size() == 0)
         {
            String oldStyleDeclarations = (String) model.getAttribute("carnot:engine:typeDeclarations");
            if (oldStyleDeclarations != null)
            {
               Document doc = XmlUtils.parseString(oldStyleDeclarations);
               typeDeclarationWrappers = getElementsByLocalName(doc, XPDL_TYPE_DECLARATIONS);
            }
         }
         for (int i = 0; i < typeDeclarationWrappers.size(); i++)
         {
            Element typeDeclarationsWrapper = (Element) typeDeclarationWrappers.get(i);
            List typeDeclarations = getElementsByLocalName(typeDeclarationsWrapper, XPDL_TYPE_DECLARATION);
            for (int j = 0; j < typeDeclarations.size(); j++)
            {
               IXpdlType xpdlType = null;
               NodeList tdNodeChildren = ((Element) typeDeclarations.get(j)).getChildNodes();
               for (int k = 0; k < tdNodeChildren.getLength(); k++)
               {
                  Node child = tdNodeChildren.item(k);
                  if (child instanceof Element)
                  {
                     if (XPDL_EXTERNAL_REFERENCE.equals(child.getLocalName()))
                     {
                        String location = ((Element) child).getAttribute(XPDL_LOCATION_ATT);
                        String ns = ((Element) child).getAttribute(XPDL_NAMESPACE_ATT);
                        String xref = ((Element) child).getAttribute(XPDL_XREF_ATT);

                        Element externalAnnotationsElement = findExternalAnnotationsElement((Node) typeDeclarations.get(j));
                        xpdlType = new ExternalReferenceBean(intern(location), intern(ns), intern(xref), externalAnnotationsElement);

                        // no patching if any other type then schema types are present
                        needsPatching = false;
                     }
                     else if (XPDL_SCHEMA_TYPE.equals(child.getLocalName()))
                     {
                        // there is a great confusion here regarding the namspace in which the schema is declared
                        // depending on the source of the element.
                        List embeddedSchemas = getElementsByLocalName(child, XSDConstants.SCHEMA_ELEMENT_TAG);
                        if (1 == embeddedSchemas.size())
                        {
                           Element embeddedSchemaElement = (Element)embeddedSchemas.get(0);

                           // clone embedded XSD into separate DOM to avoid excessive memory use
                           Element schemaElement;
                           Document xsdDoc = XmlUtils.newDocument();
                           if (NS_XSD_2001.equals(embeddedSchemaElement.getNamespaceURI()))
                           {
                              schemaElement = (Element) xsdDoc.importNode(embeddedSchemaElement, true);
                           }
                           else
                           {
                              // manually clone the schema node and bring it to the correct namespace
                              schemaElement = cloneToSchema(xsdDoc, embeddedSchemaElement);
                              schemaElement.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns", NS_XSD_2001);
                           }

                           // transfer namespace declarations
                           Set nsPrefixes = CollectionUtils.newSet();
                           // collect existing namespaces
                           NamedNodeMap attributes = schemaElement.getAttributes();
                           for (int m = 0, nAttributes = attributes.getLength(); m < nAttributes; m++)
                           {
                              Attr attr = (Attr) attributes.item(m);
                              if (XSDConstants.XMLNS_URI_2000.equals(attr.getNamespaceURI()))
                              {
                                 String nsPrefix = attr.getLocalName();
                                 if (nsPrefix.equals(attr.getName()))
                                 {
                                    nsPrefix = DEFAULT_NS_PREFIX;
                                 }
                                 nsPrefixes.add(nsPrefix);
                              }
                              else if (attr.getNamespaceURI() == null && "xmlns".equals(attr.getLocalName()))
                              {
                                 // treat default xmlns declaration as empty prefix
                                 nsPrefixes.add(DEFAULT_NS_PREFIX);
                              }
                           }
                           // if schema doesn't define a targetNamespace, make sure that default xmlns declarations are not propagated.
                           Attr tnsNode = (Attr) attributes.getNamedItem("targetNamespace");
                           if (tnsNode == null || StringUtils.isEmpty(tnsNode.getValue()))
                           {
                              nsPrefixes.add(DEFAULT_NS_PREFIX);
                           }
                           // traverse the source parent tree and transfer namespace declarations
                           Node nsContext = embeddedSchemaElement.getParentNode();
                           while (nsContext != null)
                           {
                              attributes = nsContext.getAttributes();
                              if (attributes != null)
                              {
                                 for (int m = 0, nAttributes = attributes.getLength(); m < nAttributes; m++)
                                 {
                                    Attr attr = (Attr) attributes.item(m);
                                    if (XSDConstants.XMLNS_URI_2000.equals(attr.getNamespaceURI()))
                                    {
                                       String nsPrefix = attr.getLocalName();
                                       if (nsPrefix.equals(attr.getName()))
                                       {
                                          nsPrefix = DEFAULT_NS_PREFIX;
                                       }
                                       if ( !nsPrefixes.contains(nsPrefix))
                                       {
                                          schemaElement.setAttributeNS(XSDConstants.XMLNS_URI_2000,
                                                nsPrefix.length() == 0 ? "xmlns" : intern("xmlns:" + nsPrefix), intern(attr.getValue()));
                                          nsPrefixes.add(nsPrefix);
                                       }
                                    }
                                    // also consider default xmlns declarations
                                    else if (attr.getNamespaceURI() == null && "xmlns".equals(attr.getLocalName()) && !nsPrefixes.contains(DEFAULT_NS_PREFIX))
                                    {
                                       schemaElement.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns", intern(attr.getValue()));
                                       nsPrefixes.add(DEFAULT_NS_PREFIX);
                                    }
                                 }
                              }
                              nsContext = nsContext.getParentNode();
                           }
                           xsdDoc.appendChild(schemaElement);

                           XSDSchema xsdSchema = XSDFactory.eINSTANCE.createXSDSchema();
                           xsdSchema.setElement(schemaElement);
                           String targetNamespace = xsdSchema.getTargetNamespace();
                           if (targetNamespace == null || !targetNamespace.equals(STANDARD_CARNOT_WORKSPACE))
                           {
                              needsPatching = false;
                           }
                           xpdlType = new SchemaTypeBean(xsdSchema);
                        }
                     }
                     else if (XPDL_BASIC_TYPE.equals(child.getLocalName()))
                     {
                        String type = ((Element) child).getAttribute(XPDL_TYPE_ATT);

                        xpdlType = new BasicTypeBean(XpdlBasicType.fromId(type));

                        // no patching if any other type then schema types are present
                        needsPatching = false;
                     }
                     else if (XPDL_DECLARED_TYPE.equals(child.getLocalName()))
                     {
                        String id = ((Element) child).getAttribute(XPDL_ID_ATT);

                        xpdlType = new DeclaredTypeBean(intern(id));

                        // no patching if any other type then schema types are present
                        needsPatching = false;
                     }
                     if (xpdlType != null)
                     {
                        elementFactory.createTypeDeclaration((Node) typeDeclarations.get(j),
                           model, xpdlType);
                     }
                  }
               }
            }
         }

         XSDResourceImpl schemaResource = new XSDResourceImpl(URI.createURI(NS_CARNOT_WORKFLOWMODEL_31));
         ResourceSetImpl resourceSet = new ResourceSetImpl();
         resourceSet.getResources().add(schemaResource);

         Map<XSDSchema, String> schemas2namespace = newHashMap();

         // temporarily install schema locator to allow resolution of cross-schema references
         SchemaLocatorAdapter schemaLocatorAdapter = new SchemaLocatorAdapter(model);
         schemaResource.eAdapters().add(schemaLocatorAdapter);

         // patch namespaces
         ModelElementList declarations = model.getTypeDeclarations();
         for (int i = 0; i < declarations.size(); i++)
         {
            TypeDeclarationBean decl = (TypeDeclarationBean) declarations.get(i);
            IXpdlType type = decl.getXpdlType();
            XSDSchema xsdSchema = null;
            if(type instanceof IExternalReference)
            {
               String location = ((IExternalReference) type).getLocation();
               if(location != null && (location.endsWith(".xsd") || location.endsWith(".xml")))
               {
                  xsdSchema = ((IExternalReference) type).getSchema(model);
               }
            }
            else if(type instanceof SchemaTypeBean)
            {
               xsdSchema = ((SchemaTypeBean) type).getSchema();
            }

               if (xsdSchema != null)
               {
                  if (needsPatching)
                  {
                     // patch namespaces
                     xsdSchema.getQNamePrefixToNamespaceMap().put(XSDPackage.eNS_PREFIX, XMLResource.XML_SCHEMA_URI);
                     xsdSchema.setSchemaForSchemaQNamePrefix(XSDPackage.eNS_PREFIX);
                     xsdSchema.getQNamePrefixToNamespaceMap().remove(TNS_PREFIX);
                     String targetNamespace = intern(BASE_INFINITY_NAMESPACE + model.getId() + PATH_SEPARATOR + decl.getId());
                     String prefix = computePrefix(decl.getId(), xsdSchema.getQNamePrefixToNamespaceMap().keySet());
                     xsdSchema.getQNamePrefixToNamespaceMap().put(intern(prefix), targetNamespace);
                     xsdSchema.setTargetNamespace(targetNamespace);
                     schemas2namespace.put(xsdSchema, targetNamespace);
                  }
                  ((InternalEObject) xsdSchema).eSetResource(schemaResource, null);
                  xsdSchema.setSchemaLocation(intern(StructuredDataConstants.URN_INTERNAL_PREFIX + decl.getId()));
               }
            }

         if (needsPatching)
         {
            // now traverse the structure and resolve types
            for (int i = 0; i < declarations.size(); i++)
            {
               TypeDeclarationBean decl = (TypeDeclarationBean) declarations.get(i);
               IXpdlType type = decl.getXpdlType();
               XSDSchema xsdSchema = null;
               if(type instanceof IExternalReference)
               {
                  String location = ((IExternalReference) type).getLocation();
                  if(location != null && (location.endsWith(".xsd") || location.endsWith(".xml")))
                  {
                     xsdSchema = ((IExternalReference) type).getSchema(model);
                  }
               }
               else if(type instanceof SchemaTypeBean)
               {
                  xsdSchema = ((SchemaTypeBean) type).getSchema();
               }

                  if (xsdSchema != null)
                  {
                     resolveTypes(decl, xsdSchema, schemas2namespace);
                     xsdSchema.reset();
                     // test
                     /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     XSDResourceImpl.serialize(baos, xsdSchema.getElement());
                     System.err.println("----");
                     System.out.println(baos.toString());*/
                  }
               }
            }

         // remove adapter after type resolution to avoid a permanent reference to model
         schemaResource.eAdapters().remove(schemaLocatorAdapter);

         NodeList data = node.getOwnerDocument().getElementsByTagName(DATA);
         for (int i = 0, nData = data.getLength(); i < nData; i++)
         {
            IData theData = elementFactory.createData(data.item(i), model);
            if (theData != null && model == theData.getModel())
            {
               IDataType type = (IDataType) theData.getType();
               if (needsPatching && "struct".equals(getSafeTypeId(type)))
               {
                  String oldValue = (String) theData.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
                  if (oldValue != null && oldValue.indexOf('<') >= 0)
                  {
                     // xml snippet.
                     Document doc = XmlUtils.parseString(oldValue);
                     List wrappers = getElementsByLocalName(doc, XPDL_DECLARED_TYPE);
                     if (wrappers.size() > 0)
                     {
                        Element item = (Element) wrappers.get(0);
                        String typeId = item.getAttribute(XPDL_ID_ATT);
                        if (!StringUtils.isEmpty(typeId))
                        {
                           theData.setAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT, intern(typeId));
                        }
                     }
                  }
               }
            }
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedData(model);
         }

         NodeList applications = node.getOwnerDocument().getElementsByTagName(APPLICATION);
         for (int i = 0, nApplications = applications.getLength(); i < nApplications; i++)
         {
            addApplication(model, applications.item(i));
         }

         NodeList modelers = node.getOwnerDocument().getElementsByTagName(MODELER);
         for (int i = 0, nModelers = modelers.getLength(); i < nModelers; i++)
         {
            elementFactory.createModeler(modelers.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedModelers(model);
         }

         NodeList conditionalperformers = node.getOwnerDocument().getElementsByTagName(CONDITIONAL_PERFORMER);

         for (int i = 0, nConditionalPerformers = conditionalperformers.getLength(); i < nConditionalPerformers; i++)
         {
            elementFactory.createConditionalPerformer(conditionalperformers.item(i), model);
         }

         NodeList roles = node.getOwnerDocument().getElementsByTagName(ROLE);
         for (int i = 0, nRoles = roles.getLength(); i < nRoles; i++)
         {
            elementFactory.createRole(roles.item(i), model);
         }
         if (injectPredefinedConstants)
         {
            DefaultModelBuilder.createPredefinedRoles(model);
         }

         NodeList organizations = node.getOwnerDocument().getElementsByTagName(ORGANIZATION);
         for (int i = 0, nOrganizations = organizations.getLength(); i < nOrganizations; i++)
         {
            elementFactory.createOrganization(organizations.item(i), model);
         }

         // Read the participant hierarchy

         for (int i = 0, nOrganizations = organizations.getLength(); i < nOrganizations; i++)
         {
            Node organizationNode = organizations.item(i);

            elementFactory.attachTeamLead(organizationNode, model);

            NodeList organizationChildren = organizationNode.getChildNodes();

            for (int j = 0, nOrganizationChildren = organizationChildren.getLength(); j < nOrganizationChildren; j++)
            {
               Node child = organizationChildren.item(j);

               if (PARTICIPANT.equals(child.getNodeName()))
               {
                  elementFactory.attachParticipant(organizationNode, child, model);
               }
            }
         }

         NodeList linkTypes = node.getOwnerDocument().getElementsByTagName(LINK_TYPE);

         for (int i = 0, nLinkTypes = linkTypes.getLength(); i < nLinkTypes; i++)
         {
            elementFactory.createLinkType(linkTypes.item(i), model);
         }

         // Read the process definitions after participants
         Map<QName, List<IProcessDefinition>> implementations = CollectionUtils.newMap();
         NodeList processes = node.getOwnerDocument().getElementsByTagName(PROCESS);
         for (int i = 0, nProcesses = processes.getLength(); i < nProcesses; i++)
         {
            IProcessDefinition pd = addProcess(model, processes.item(i));
            IReference ref = pd.getExternalReference();
            if (ref != null)
            {
               QName qname = new QName(ref.getExternalPackage().getHref(), ref.getId());
               List<IProcessDefinition> list = implementations.get(qname);
               if (list == null)
               {
                  list = new ArrayList<IProcessDefinition>();
                  implementations.put(qname, list);
               }
               list.add(pd);
            }
         }
         for (Map.Entry<QName, List<IProcessDefinition>> entry : implementations.entrySet())
         {
            entry.setValue(ModelUtils.trim((ArrayList<IProcessDefinition>) entry.getValue()));
         }
         ((ModelBean) model).setImplementations(ModelUtils.trim(implementations));

         // create the process-subprocess relations after all process definitions are read
         for (Iterator i = subprocesses.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            IActivity activity = (IActivity) entry.getKey();
            activity.setImplementationProcessDefinition(model.findProcessDefinition((String) entry.getValue()));
         }

         // create the Event handlers after all process definitions and all participants are read
         for (Iterator i = eventHandlers.iterator(); i.hasNext();)
         {
            Pair entry = (Pair) i.next();

            Node handlerNode = (Node) entry.getFirst();
            IEventHandler handler = null;
            if (entry.getSecond() instanceof IProcessDefinition)
            {
               handler  = elementFactory.createEventHandler(
                     handlerNode, (IProcessDefinition) entry.getSecond());

            }
            else if (entry.getSecond() instanceof IActivity)
            {
               handler = elementFactory.createEventHandler(
                     handlerNode, (IActivity) entry.getSecond());
            }
            else
            {
               warn(ConversionWarning.MEDIUM,
                     "Unexpected type in event handler list: " + entry.getSecond().getClass(),
                     null, null);
            }
            if (handler != null)
            {
               NodeList handlerChildren = handlerNode.getChildNodes();
               for (int j = 0, nHandlerChildren = handlerChildren.getLength(); j < nHandlerChildren; j++)
               {
                  Node actionNode = handlerChildren.item(j);
                  if(node instanceof Element && node.getNamespaceURI().equals(NS_CARNOT_WORKFLOWMODEL_31))
                  {
                     if (EVENT_ACTION.equals(actionNode.getLocalName()))
                     {
                        elementFactory.createEventAction(actionNode, handler);
                     }
                     else if (BIND_ACTION.equals(actionNode.getLocalName()))
                     {
                        elementFactory.createBindAction(actionNode, handler);
                     }
                     else if (UNBIND_ACTION.equals(actionNode.getLocalName()))
                     {
                        elementFactory.createUnbindAction(actionNode, handler);
                     }
                  }
               }
            }
         }

         if (includeDiagrams)
         {
            NodeList diagrams = node.getOwnerDocument().getElementsByTagName(DIAGRAM);

            for (int i = 0, nDiagrams = diagrams.getLength(); i < nDiagrams; i++)
            {
               Node child = diagrams.item(i);

               if (node.equals(child.getParentNode()))
               {
                  Diagram diagram = elementFactory.createDiagram(child, model);

                  addDiagramSymbols(child, diagram, model, null);
               }
            }
         }

         // Read the views last, because they are references to other modelelements
         NodeList views = node.getChildNodes();
         for (int i = 0, nViews = views.getLength(); i < nViews; i++)
         {
            Node viewNode = views.item(i);
            if (VIEW.equals(viewNode.getNodeName()))
            {
               addView(model, null, viewNode);
            }
         }
      }
      finally
      {
         if (model != null)
         {
            if (model instanceof ModelBean)
            {
               ModelBean theModel = (ModelBean) model;
               theModel.setConfigurationVariableReferences(confVarProvider
                     .getConVarCandidateNames());
            }
         }
      }

      return model;
   }

   private String getSafeTypeId(IDataType type)
   {
      return type == null ? null : type.getId();
   }

   private void addExternalPackages(IModel model, Element node)
   {
      NodeList children = node.getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTERNAL_PACKAGES);
      if (children.getLength() == 0)
      {
         children = node.getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTERNAL_PACKAGES);
      }
      for (int i = 0, l1 = children.getLength(); i < l1; i++)
      {
         Element externalPackagesWrapper = (Element) children.item(i);
         NodeList externalPackages = externalPackagesWrapper.getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTERNAL_PACKAGE);
         if (externalPackages.getLength() == 0)
         {
            externalPackages = externalPackagesWrapper.getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTERNAL_PACKAGE);
         }
         for (int j = 0, l2 = externalPackages.getLength(); j < l2; j++)
         {
            IExternalPackage pkg = elementFactory.createExternalPackage(
                  externalPackages.item(j), model);
            ((ModelBean) model).addToExternalPackages(pkg);
         }
      }
   }

   private String intern(String s)
   {
      if (s != null)
      {
         s = s.trim().intern();
      }
      return s;
   }

   private List getElementsByLocalName(Node node, String localName)
   {
      NodeList children = node.getChildNodes();
      List result = new ArrayList(children.getLength());
      for (int i = 0, nChildren = children.getLength(); i < nChildren; i++)
      {
         Node child = children.item(i);
         if (child instanceof Element)
         {
            Element element = (Element) child;
            if (localName.equals(element.getLocalName()))
            {
               result.add(child);
            }
         }
      }
      return result;
   }

   private Element cloneToSchema(Document xsdDoc, Element schemaElement)
   {
      Element clone = xsdDoc.createElementNS(NS_XSD_2001, schemaElement.getLocalName());
      NamedNodeMap attributes = schemaElement.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++)
      {
         Attr attribute = (Attr) attributes.item(i);
         clone.setAttribute(attribute.getLocalName(), attribute.getValue());
      }
      NodeList children = schemaElement.getChildNodes();
      for (int i = 0, nChildren = children.getLength(); i < nChildren; i++)
      {
         Node node = children.item(i);
         if (node instanceof Element)
         {
            clone.appendChild(cloneToSchema(xsdDoc, (Element) node));
         }
         else
         {
            clone.appendChild(xsdDoc.importNode(node, true));
         }
      }
      return clone;
   }

   private void resolveTypes(TypeDeclarationBean decl, XSDSchema schema, Map schemas2namespace)
   {
      List elements = schema.getElementDeclarations();
      for (int i = 0; i < elements.size(); i++)
      {
         XSDElementDeclaration element = (XSDElementDeclaration) elements.get(i);
         // we need to be sure that the element is defined in this schema and not in an imported one
         if (CompareHelper.areEqual(schema, element.getSchema()))
         {
            patchElement(decl, element, schemas2namespace);
         }
      }
      List types = schema.getTypeDefinitions();
      for (int i = 0; i < types.size(); i++)
      {
         XSDTypeDefinition type = (XSDTypeDefinition) types.get(i);
         // we need to be sure that the type is defined in this schema and not in an imported one
         if (type instanceof XSDComplexTypeDefinition && CompareHelper.areEqual(schema, type.getSchema()))
         {
            patchType(decl, (XSDComplexTypeDefinition) type, schemas2namespace);
         }
      }
   }

   private void patchType(TypeDeclarationBean declaration, XSDComplexTypeDefinition complexType, Map schemas2namespace)
   {
      XSDComplexTypeContent content = complexType.getContent();
      if (content instanceof XSDParticle)
      {
         patchParticle(declaration, (XSDParticle) content, schemas2namespace);
      }
   }

   private void patchParticle(TypeDeclarationBean declaration, XSDParticle particle, Map schemas2namespace)
   {
      XSDTerm term = particle.getTerm();
      if (term != null)
      {
         patchTerm(declaration, term, schemas2namespace);
      }
   }

   private void patchTerm(TypeDeclarationBean declaration, XSDTerm term, Map schemas2namespace)
   {
      if (term instanceof XSDElementDeclaration)
      {
         patchElement(declaration, (XSDElementDeclaration) term, schemas2namespace);
      }
      // TODO: use instanceof XSDModelGroup once we switch to java 5
      else if ("XSDModelGroup".equals(term.eClass().getName()))
      {
         // must use ecore reflection and not cast to XSDModelGroup to avoid java 5 dependencies
         EStructuralFeature feature = term.eClass().getEStructuralFeature("contents");
         List particles = (List) term.eGet(feature);
         for (int i = 0; i < particles.size(); i++)
         {
            patchParticle(declaration, (XSDParticle) particles.get(i), schemas2namespace);
         }
      }
   }

   private void patchElement(TypeDeclarationBean declaration, XSDElementDeclaration element, Map schemas2namespace)
   {
      XSDTypeDefinition type = element.getAnonymousTypeDefinition();
      if (type instanceof XSDComplexTypeDefinition)
      {
         patchType(declaration, (XSDComplexTypeDefinition) type, schemas2namespace);
      }
      else if (type == null)
      {
         type = element.getType();
         if (type != null && type.getSchema() == null)
         {
            // unresolved type
            DeclarationAndTypeHolder holder = resolve(declaration, element, type);
            if (holder != null)
            {
               type = holder.type;
            }
            if (type != null && type.getSchema() != null)
            {
               updateImports(element.getSchema(), type.getSchema(), holder.declaration, schemas2namespace);
               element.setTypeDefinition(type);
            }
         }
      }
   }

   private DeclarationAndTypeHolder resolve(TypeDeclarationBean declaration,
         XSDElementDeclaration element, XSDTypeDefinition type)
   {
      String name = type.getName();
      String targetNamespace = type.getTargetNamespace();
      if (name != null)
      {
         if (XMLResource.XML_SCHEMA_URI.equals(targetNamespace))
         {
            XSDSchema schema = element.getSchema();
            XSDTypeDefinition def = findTypeDefinition(schema, name);
            if (def != null)
            {
               return new DeclarationAndTypeHolder(declaration, def);
            }
         }
         if (STANDARD_CARNOT_WORKSPACE.equals(targetNamespace) // declared in the standard carnot workspace
            || TNS_PREFIX.equals(targetNamespace) // xmlns:tns namespace declaration may be missing
            || XMLResource.XML_SCHEMA_URI.equals(targetNamespace)) // declared in no namespace meaning it inherits the schema namespace
         {
            ModelElementList list = model.getTypeDeclarations();
            for (int i = 0; i < list.size(); i++)
            {
               TypeDeclarationBean decl = (TypeDeclarationBean) list.get(i);
               if (CompareHelper.areEqual(decl.getId(), name))
               {
                  XSDTypeDefinition def = findTypeDefinition(decl);
                  if (def != null)
                  {
                     return new DeclarationAndTypeHolder(declaration, def);
                  }
               }
            }
         }
      }
      return null;
   }

   private static XSDTypeDefinition findTypeDefinition(TypeDeclarationBean declaration)
   {
      if (declaration != null)
      {
         IXpdlType type = declaration.getXpdlType();
         if (type instanceof SchemaTypeBean)
         {
            XSDSchema xsdSchema = ((SchemaTypeBean) type).getSchema();
            String name = declaration.getId();
            return findTypeDefinition(xsdSchema, name);
         }
      }
      return null;
   }

   private static XSDTypeDefinition findTypeDefinition(XSDSchema schema, String name)
   {
      if (schema != null)
      {
         List types = schema.getTypeDefinitions();
         for (int i = 0; i < types.size(); i++)
         {
            XSDTypeDefinition def = (XSDTypeDefinition) types.get(i);
            // we need to be sure that the type is defined in this schema and not in an imported one
            if (name.equals(def.getName()) && CompareHelper.areEqual(schema, def.getSchema()))
            {
               return def;
            }
         }
      }
      return null;
   }

   private void updateImports(XSDSchema schema, XSDSchema schema2Import, TypeDeclarationBean decl,
         Map schemas2namespace)
   {
      if (schema == schema2Import)
      {
         return;
      }
      List contents = schema.getContents();
      for (int i = 0; i < contents.size(); i++)
      {
         XSDSchemaContent item = (XSDSchemaContent) contents.get(i);
         if (item instanceof XSDImport)
         {
            XSDImport imported = (XSDImport) item;
            if (schema2Import == imported.getResolvedSchema())
            {
               // schema already imported
               return;
            }
         }
      }
      // add new import declaration
      String prefix = intern(computePrefix(decl.getId(), schema.getQNamePrefixToNamespaceMap().keySet()));
      String ns = intern((String) schemas2namespace.get(schema2Import));
      schema.getQNamePrefixToNamespaceMap().put(prefix, ns);
      XSDImport xsdImport = XSDFactory.eINSTANCE.createXSDImport();
      xsdImport.setNamespace(ns);
      xsdImport.setSchemaLocation(intern(schema2Import.getSchemaLocation()));
      schema.getContents().add(0, xsdImport);
   }

   private String computePrefix(String name, Set usedPrefixes)
   {
      String prefix;
      if (name != null)
      {
         name = name.trim();
      }
      if (name == null || name.length() == 0)
      {
         prefix = "p";
      }
      else
      {
         // prefix is first 3 character + ending digits if any.
         int pos = name.length();
         while (pos > 0 && Character.isDigit(name.charAt(pos - 1)))
         {
            pos--;
         }
         int nch = 3;
         if (nch > pos)
         {
            nch = pos;
         }
         prefix = name.substring(0, nch) + name.substring(pos);
      }
      if (usedPrefixes.contains(prefix))
      {
         int counter = 1;
         while (usedPrefixes.contains(prefix + '_' + counter))
         {
            counter++;
         }
         prefix = prefix + '_' + counter;
      }
      return prefix.toLowerCase();
   }

   private Element findExternalAnnotationsElement(Node node)
   {
      if (node instanceof Element)
      {
         NodeList nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTES);
         if (nlAttributes.getLength() == 0)
         {
            nlAttributes = ((Element)node).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTES);
         }
         if (nlAttributes.getLength() == 1)
         {
            NodeList nlAttribute = ((Element)nlAttributes.item(0)).getElementsByTagNameNS(NS_XPDL_2_1, XPDL_EXTENDED_ATTRIBUTE);
            if (nlAttribute.getLength() == 0)
            {
               nlAttribute = ((Element)node).getElementsByTagNameNS(NS_XPDL_1_0, XPDL_EXTENDED_ATTRIBUTE);
            }
            for (int i = 0, nNlAttribute = nlAttribute.getLength(); i < nNlAttribute; i++)
            {
               Element attributeElement = (Element)nlAttribute.item(i);
               if (StructuredDataConstants.EXTERNAL_ANNOTATIONS_ATT.equals(attributeElement.getAttribute(XPDL_EXTENDED_ATTRIBUTE_NAME)))
               {
                  NodeList nlAnnotation = attributeElement.getElementsByTagNameNS(NS_XSD_2001, XSDConstants.ANNOTATION_ELEMENT_TAG);
                  if (nlAnnotation.getLength() == 1)
                  {
                     return (Element) nlAnnotation.item(0);
                  }
               }
            }
         }
      }
      return null;
   }

   private void warn(int severity, String message, Exception exception, Object scope)
   {
      if (exception != null)
      {
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message, exception);
      }
      else
      {
         trace.debug(DEFAULT_NS_PREFIX, new Exception());
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message);
      }
   }

   static void fail(ErrorCase errorCase, Exception ex)
   {
      if (ex instanceof ModelParsingException)
      {
         throw (ModelParsingException) ex;
      }
      if (ex != null)
      {
         trace.warn(errorCase == null ? null : errorCase.toString(), ex);
         if (errorCase == null)
         {
            errorCase = BpmRuntimeError.MDL_INVALID_IPP_MODEL_FILE_GENERAL_PARSE_ERROR
                  .raise(ex.getMessage());
         }
      }
      throw new ModelParsingException(errorCase);
   }

   /**
    * Imports a model from the file using XML-Format.
    * <tt>isRoot</tt> indicates, wether the model to be imported is the
    * root version of a model.
    */
   public IModel importFromXML(File file)
   {
      try
      {
         return importFromXML(new InputSource(new FileInputStream(file)));
      }
      catch (FileNotFoundException x)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_CANNOT_CREATE_MODEL_FROM_FILE.raise(file.getPath()), x);
      }
   }

   public IModel importFromXML(InputStream inputStream)
   {
      return importFromXML(new InputSource(inputStream));
   }

   public IModel importFromXML(Reader reader)
   {
      return importFromXML(new InputSource(reader));
   }

   /**
    * Imports a model from the file using XML-Format.
    * <tt>isRoot</tt> indicates, wether the model to be imported is the
    * root version of a model.
    */
   public IModel importFromXML(InputSource inputSource)
   {
      try
      {
         final URL xsdURL = DefaultXMLReader.class.getResource(WORKFLOWMODEL_XSD);
         if (xsdURL == null)
         {
            throw new InternalException("Unable to find " + WORKFLOWMODEL_XSD);
         }
         final URL dtdURL = DefaultXMLReader.class.getResource(DTD_NAME);
         if (dtdURL == null)
         {
            throw new InternalException("Unable to find " + DTD_NAME);
         }

         DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true, NS_CARNOT_WORKFLOWMODEL_31);
         ParseErrorHandler errorHandler = new ParseErrorHandler();
         domBuilder.setErrorHandler(errorHandler);

         RecordingEntityResolver entityResolver = new RecordingEntityResolver(xsdURL,
               dtdURL);
         domBuilder.setEntityResolver(entityResolver);

         Document document = domBuilder.parse(inputSource);

         Set usedUrls = entityResolver.getUsedUrls();
         if (usedUrls.contains(dtdURL))
         {
            // TODO (sb): ensure that every model XML source uses XSD-validation
            // by upgrading models to version 3.1.0 (needs to be implemented).
            trace.info("XML source will be re-validated with " + DTD_NAME + ".");

            domBuilder = XmlUtils.newDomBuilder(true);

            errorHandler = new ParseErrorHandler();
            domBuilder.setErrorHandler(errorHandler);

            entityResolver = new RecordingEntityResolver(xsdURL, dtdURL);
            domBuilder.setEntityResolver(entityResolver);

            Properties transformProperties = new Properties();
            transformProperties.put(OutputKeys.DOCTYPE_SYSTEM, WORKFLOWMODEL_31_DTD_URL);

            // TODO (sb): find a way for re-validating in-memory DOM documents with DTD
            String stringifiedDocument = XmlUtils.toString(document, transformProperties);
            domBuilder.parse(new InputSource(new StringReader(stringifiedDocument)));
         }

         if (showModelLoadMessages)
         {
            errorHandler.doTracing();
         }

         NodeList elements = document.getElementsByTagName(MODEL);

         Element rootNode = (Element) elements.item(0);
         if (null == rootNode)
         {
            fail(BpmRuntimeError.MDL_INVALID_IPP_MODEL_FILE.raise(MODEL), null);
         }

         trace.info("Reading model with id '" + rootNode.getAttribute("id") + "'.");

         return loadModel(rootNode);
      }
      catch (Exception x)
      {
         x.printStackTrace();
         fail(null, x);
      }
      return null;
   }

   private static final class DeclarationAndTypeHolder
   {
      private XSDTypeDefinition type;
      private TypeDeclarationBean declaration;

      public DeclarationAndTypeHolder(TypeDeclarationBean decl,
            XSDTypeDefinition typeDefinition)
      {
         type = typeDefinition;
         declaration = decl;
      }
   }

   private final class ParseErrorHandler implements ErrorHandler
   {
      private static final String WARNING = "Warning";

      private List parseMessages = new ArrayList();

      public void warning(SAXParseException exception) throws SAXException
      {
         parseMessages.add(formatParseException(WARNING, exception));
      }

      public void error(SAXParseException exception) throws SAXException
      {
         parseMessages.add(formatParseException("Error", exception));
      }

      public void fatalError(SAXParseException exception) throws SAXException
      {
         parseMessages.add(formatParseException("Fatal Error", exception));
      }

      public void doTracing()
      {
         for (Iterator i = parseMessages.iterator(); i.hasNext();)
         {
            String parseMessage = (String) i.next();

            if (parseMessage.startsWith(WARNING))
            {
               trace.warn(parseMessage);
            }
            else
            {
               trace.error(parseMessage);
            }
         }
      }

      private String formatParseException(String label,
            SAXParseException exception)
      {
         StringBuffer buffer = new StringBuffer();

         buffer.append(label).append(" (").append(exception.getLineNumber())
               .append(", ").append(exception.getColumnNumber()).append(") ");

         buffer.append(exception.getMessage());

         return buffer.toString();
      }
   }

   private static final class RecordingEntityResolver implements EntityResolver
   {
      private URL xsdUrl;
      private URL dtdUrl;
      private Set usedUrls = new HashSet();

      public RecordingEntityResolver(URL xsdUrl, URL dtdUrl)
      {
         this.xsdUrl = xsdUrl;
         this.dtdUrl = dtdUrl;
      }

      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
      {
         if (systemId != null)
         {
            if (WORKFLOWMODEL_31_XSD_URL.equals(systemId)
                        || NS_CARNOT_WORKFLOWMODEL_31.equals(systemId)
                        || systemId.endsWith(WORKFLOWMODEL_XSD))
            {
               usedUrls.add(xsdUrl);
               return new InputSource(xsdUrl.openStream());
            }
            else if (WORKFLOWMODEL_30_DTD_URL.equals(systemId)
                        || WORKFLOWMODEL_31_DTD_URL.equals(systemId)
                        || systemId.endsWith(DTD_NAME))
            {
               usedUrls.add(dtdUrl);
               return new InputSource(dtdUrl.openStream());
            }
            else if (XpdlUtils.XPDL_1_0_XSD_URL.equals(systemId)
                        || systemId.endsWith(XpdlUtils.XPDL_1_0_XSD))
            {
               URL xpdlUrl = XpdlUtils.getXpdl_10_Schema();
               usedUrls.add(xpdlUrl);
               return new InputSource(xpdlUrl.openStream());
            }
            else if (XpdlUtils.XPDL_2_1_XSD_URL.equals(systemId)
                        || systemId.endsWith(XpdlUtils.XPDL_2_1_XSD))
            {
               URL xpdlUrl = XpdlUtils.getXpdl_21_Schema();
               usedUrls.add(xpdlUrl);
               return new InputSource(xpdlUrl.openStream());
            }
            else if (XpdlUtils.XPDL_XSD.equals(systemId) || systemId
                        .endsWith(XpdlUtils.XPDL_XSD))
            {
               URL xpdlUrl = XpdlUtils.getXpdlSchema();
               usedUrls.add(xpdlUrl);
               return new InputSource(xpdlUrl.openStream());
            }
            else if (XpdlUtils.XPDL_EXTENSIONS_XSD.equals(systemId) || systemId
                  .endsWith(XpdlUtils.XPDL_EXTENSIONS_XSD))
            {
               URL xpdlUrl = XpdlUtils.getXpdlExtensionsSchema();
               usedUrls.add(xpdlUrl);
               return new InputSource(xpdlUrl.openStream());
            }
            else if (XpdlUtils.CARNOT_XPDL_XSD_URL.equals(systemId)
                  || systemId.endsWith(XpdlUtils.CARNOT_XPDL_XSD))
            {
               URL carnotXpdlUrl = XpdlUtils.getCarnotXpdlSchema();
               usedUrls.add(carnotXpdlUrl);
               return new InputSource(carnotXpdlUrl.openStream());
            }
         }
         return null;
      }

      public Set getUsedUrls()
      {
         return Collections.unmodifiableSet(usedUrls);
      }
   }

   private class InvocationManager implements InvocationHandler
   {
      ElementFactoryImpl factory = new ElementFactoryImpl(DefaultXMLReader.this.confVarProvider);

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         Object result = null;
         try
         {
            Element node = (Element) args[0];
            factory.setNode(node);
            result = method.invoke(factory, args);
            if (method.getName().startsWith("create"))
            {
               NodeList children = ((Node) args[0]).getChildNodes();
               for (int j = 0, nChildren = children.getLength(); j < nChildren; j++)
               {
                  Node child = children.item(j);
                  if (ATTRIBUTE.equals(child.getNodeName()) && result instanceof ModelElement)
                  {

                     NodeReader reader = new NodeReader(child, DefaultXMLReader.this.confVarProvider);

                     String name = reader.getAttribute(NAME_ATT);
                     String classname = reader.getRawAttribute(CLASS_ATT);

                     if (PredefinedConstants.XPDL_EXTENDED_ATTRIBUTES.equals(name))
                     {
                        DocumentFragment extFragment = child.getOwnerDocument().createDocumentFragment();

                        NodeList extElements = child.getChildNodes();
                        for (int i = 0, nExtElements = extElements.getLength(); i < nExtElements; ++i)
                        {
                           Node extElement = extElements.item(i);
                           extFragment.appendChild(extElement.cloneNode(true));
                        }
                        extFragment.normalize();
                        ((ModelElement) result).setAttribute(name, extFragment);
                     }
                     else
                     {
                        Object value = null;
                        String valueString = reader.getRawAttribute(VALUE_ATT);
                        if (valueString == null)
                        {
                           valueString = reader.getRawChildValue(VALUE);
                        }
                        if (!StringUtils.isEmpty(valueString))
                        {
                           if (StringUtils.isEmpty(classname))
                           {
                              value = valueString.intern();
                           }
                           else
                           {
                              try
                              {
                                 value = Reflect.convertStringToObject(classname, valueString);
                              }
                              catch (Exception e)
                              {
                                 warn(ConversionWarning.MEDIUM,
                                       "Cannot retrieve attribute value for attribute '"
                                          + name + "'.", e, result);
                              }
                           }
                        }
                        ((ModelElement) result).setAttribute(name, value);
                     }
                  }
               }
               if (trace.isDebugEnabled())
               {
                  trace.debug("Created '" + result + "'");
               }
            }
         }
         catch (Exception e)
         {
            // @todo (france, ub): rework
            if (e instanceof InvocationTargetException)
            {
               Throwable e1 =  ((InvocationTargetException) e).getTargetException();
               if (e1 instanceof ModelParsingException)
               {
                  trace.warn(e1.getMessage());
                  throw e1;
               }
            }
            LogUtils.traceException(e, false);
         }
         return result;
      }
   }

   private static class SchemaLocatorAdapter implements Adapter, XSDSchemaLocator
   {
      private Notifier target;
      private IModel model;

      public SchemaLocatorAdapter(IModel model)
      {
         this.model = model;
      }

      public Notifier getTarget()
      {
         return target;
      }

      public boolean isAdapterForType(Object type)
      {
         return type == XSDSchemaLocator.class;
      }

      public void notifyChanged(Notification notification)
      {
         // ignore
      }

      public void setTarget(Notifier newTarget)
      {
         target = newTarget;
      }

      public XSDSchema locateSchema(XSDSchema xsdSchema, String namespaceURI,
            String rawSchemaLocationURI, String resolvedSchemaLocationURI)
      {
         if (!StringUtils.isEmpty(rawSchemaLocationURI) && rawSchemaLocationURI.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
         {
            String typeId = rawSchemaLocationURI.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
            if (typeId != null && typeId.length() > 0)
            {
               IModel model = this.model;
               QName qname = QName.valueOf(typeId);
               String refModelId = qname.getNamespaceURI();
               if (refModelId != null && refModelId.length() > 0 && !refModelId.equals(model.getId()))
               {
                  IExternalPackage pkg = model.findExternalPackage(refModelId);
                  if (pkg != null)
                  {
                     IModel refModel = pkg.getReferencedModel();
                     if (refModel != null)
                     {
                        model = refModel;
                     }
                  }
               }
               ITypeDeclaration declaration = model.findTypeDeclaration(qname.getLocalPart());
               if (declaration != null)
               {
                  IXpdlType type = declaration.getXpdlType();
                  if (type instanceof ISchemaType)
                  {
                     return ((ISchemaType) type).getSchema();
                  }
                  if (type instanceof IExternalReference)
                  {
                     String location = ((IExternalReference) type).getLocation();
                     if(location != null && (location.endsWith(".xsd") || location.endsWith(".xml")))
                     {
                        return ((IExternalReference) type).getSchema(model);
                     }
                  }
                  return null;
               }
            }
         }
         return null;
      }
   }
}