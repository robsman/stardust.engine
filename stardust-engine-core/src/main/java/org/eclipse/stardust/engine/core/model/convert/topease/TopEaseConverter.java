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
package org.eclipse.stardust.engine.core.model.convert.topease;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.builder.DefaultModelBuilder;
import org.eclipse.stardust.engine.core.model.convert.ConvertWarningException;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.pojo.utils.JavaApplicationTypeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * <p/>
 * needed properties:
 * <ul>
 * <li>Java.JDKHome</li>
 * </ul>
 * </p>
 * 
 * @author Marc Gille
 */
public class TopEaseConverter extends org.eclipse.stardust.engine.core.model.convert.Converter
{
   private static final Logger trace = LogManager.getLogger(TopEaseConverter.class);

   private File codeGenerationSourceDir;
   private File codeGenerationBaseDir;
   private File codeGenerationImplDir;
   private File codeGenerationClassesDir;
   private HashMap objectMap;
   private Vector classTags;
   private int mode;

   /**
    *
    */
   public TopEaseConverter(int mode)
   {
      super();
      this.mode = mode;
      objectMap = new HashMap();
      classTags = new Vector();
      codeGenerationSourceDir = new File(Parameters.instance().getString("TopEase.CodeGenerationSourceDir", "."));

      if (!codeGenerationSourceDir.exists())
      {
         new PublicException("TopEase code generation source directory \""
               + codeGenerationSourceDir.getAbsolutePath() + "\" not found");
      }

      codeGenerationClassesDir = new File(Parameters.instance().getString("TopEase.CodeGenerationClassesDir", "./work/naked-local/etc"));

      if (!codeGenerationClassesDir.exists())
      {
         new PublicException("TopEase code generation target classes directory \""
               + codeGenerationClassesDir.getAbsolutePath() + "\" not found");
      }

      codeGenerationBaseDir = new File(codeGenerationSourceDir, "topease");

      if (!codeGenerationBaseDir.exists())
      {
         codeGenerationBaseDir.mkdir();
      }

      codeGenerationImplDir = new File(codeGenerationBaseDir, "impl");

      if (!codeGenerationImplDir.exists())
      {
         codeGenerationImplDir.mkdir();
      }
   }

   /**
    *
    */
   public IModel convert(InputStream inputStream)
   {
      try
      {
         Document document = getDocumentFromInputStream(inputStream);
         model = DefaultModelBuilder.create().createModel("name", "name", "description");
         trace.debug("Processing roles/organizations.");
         NodeList elements = document.getElementsByTagName("Element");

         for (int n = 0; n < elements.getLength(); ++n)
         {
            try
            {
               parseElementNode(elements.item(n));
            }
            catch (Exception e)
            {
               trace.warn(e);
               ConvertWarningException warning = new ConvertWarningException("Error " +
                     "while trying to parse element: " + e.getMessage());
               converterWarnings.addLast(warning);
            }
         }

         trace.debug("Parsing Class information");
         loadClasses(document);

         trace.debug("Parsing Workproduct information");
         loadWorkproducts(document);

         trace.debug("Processing processes.");
         elements = document.getElementsByTagName("TEProcess");

         for (int n = 0; n < elements.getLength(); ++n)
         {
            parseProcessNode(elements.item(n));
         }

         trace.debug("Parsing Interface information");
         loadInterfaces(document);

         trace.debug("Parsing Business Value Chains.");
         loadBVCs(document);

         trace.debug("Generate default diagrams.");
         populateDefaultDiagrams();
         if (mode == PROTOTYPE_MODE)
         {
            adjustForExecutability();
         }

         return model;
      }
      catch (Exception x)
      {
         throw new PublicException("Cannot convert model. ", x);
      }
   }

   private void loadBVCs(Document document)
   {
      BVCFolderParser bvcParser;
      Node bvcRoot;
      Node bvc;
      NodeList bvcs;

      try
      {
         bvcParser = new BVCFolderParser();
         bvcRoot = bvcParser.searchRootFolder(document);
         bvcs = SimpleTaskUtil.getContains(bvcRoot);

         for (int i = 0; i < bvcs.getLength(); i++)
         {
            bvc = bvcs.item(i);

            if (bvc.getNodeName().equals("BVC"))
            {
               parseBVCEvent(bvc);
            }
         }
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error while " +
               "trying to parse BVCs: " + e.getMessage());
         converterWarnings.addLast(warning);
      }
   }

   private IProcessDefinition parseBVCEvent(Node bvc)
   {
      String[] ind;
      String id;
      String name;
      String identifier;
      String description;
      Node concernedParty;
      String concernedPartyRef;
      IModelParticipant triggerParticipant;
      ITrigger trigger;
      IProcessDefinition processDefinition;
      NodeList bvcActivities;
      IActivity activity;
      IActivity lastActivity = null;

      id = SimpleTaskUtil.getId(bvc);
      ind = SimpleTaskUtil.getIdentNameDescr(bvc);
      identifier = ind[0];
      name = ind[1];
      description = ind[2];
      processDefinition = model.createProcessDefinition(identifier, name, description);

      try
      {
         concernedParty = SimpleTaskUtil.getChildByName(bvc, "hasConcernedPartyElement");
         concernedPartyRef = SimpleTaskUtil.parseReferenceNode(
               SimpleTaskUtil.getChildByName(concernedParty, "reference"));
         triggerParticipant = (IModelParticipant) objectMap.get(concernedPartyRef);
         trigger = processDefinition.createTrigger(id, name, model.findTriggerType(PredefinedConstants.MANUAL_TRIGGER), 0);
         trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT,
               triggerParticipant.getId());
      }
      catch (Exception e)
      {
         ConvertWarningException warning = new ConvertWarningException("Couldn't create" +
               " manual trigger for process " + processDefinition.getName() + ": "
               + e.getMessage());
         warning.setProcessDefinition(processDefinition);
         converterWarnings.addLast(warning);
      }

      bvcActivities = SimpleTaskUtil.getContains(bvc);

      try
      {
         for (int i = 0; i < bvcActivities.getLength(); i++)
         {
            Node bvcActivity = bvcActivities.item(i);

            if (bvcActivity.getNodeName().equals("BVC"))
            {
               activity = parseBVCActvitvity(bvcActivity, processDefinition);

               if ((lastActivity != null) && (activity != null))
               {
                  String tid = processDefinition.getDefaultTransitionId();
                  processDefinition.createTransition(tid, tid, "", lastActivity, activity);
               }

               lastActivity = activity;
            }
         }
      }
      catch (Exception e)
      {
         ConvertWarningException warning = new ConvertWarningException("Could not parse" +
               " activities of BVC " + processDefinition.getName() + ": "
               + e.getMessage());
         warning.setProcessDefinition(processDefinition);
         converterWarnings.addLast(warning);
      }

      objectMap.put(id, processDefinition);
      return processDefinition;
   }

   private IActivity parseBVCActvitvity(Node bvcActivity, IProcessDefinition processDefinition)
   {
      IActivity activity;
      IActivity lastActivity = null;
      Node tmp;
      Node ownsEdge;
      NodeList edges;

      tmp = SimpleTaskUtil.getChildByName(bvcActivity, "ownsModule");

      if (tmp != null)
      {
         tmp = SimpleTaskUtil.getChildByName(tmp, "BVCModule");

         if (tmp != null)
         {
            ownsEdge = SimpleTaskUtil.getChildByName(tmp, "ownsEdge");
            edges = ownsEdge.getChildNodes();

            if (edges != null)
            {
               for (int i = 0; i < edges.getLength(); i++)
               {
                  String type;
                  String pid;

                  Node bvcEdge = edges.item(i);

                  if (bvcEdge.getNodeName().equals("BVCEdge"))
                  {
                     String aid = SimpleTaskUtil.getId(bvcEdge);
                     Node endsIn = SimpleTaskUtil.getChildByName(bvcEdge, "endsIn");
                     Node ref = SimpleTaskUtil.getChildByName(endsIn, "reference");
                     NamedNodeMap attrs = ref.getAttributes();
                     type = attrs.getNamedItem("type").getNodeValue();

                     if ("AMActivity".equals(type))
                     {
                        pid = attrs.getNamedItem("href").getNodeValue();
                        IProcessDefinition subProcess = (IProcessDefinition) objectMap.get(pid);
                        activity = processDefinition.createActivity(aid, subProcess.getName(), "", 0);
                        activity.setImplementationType(ImplementationType.SubProcess);
                        activity.setImplementationProcessDefinition(subProcess);

                        if (lastActivity != null)
                        {
                           String tid = model.getDefaultProcessDefinitionId();
                           processDefinition.createTransition(tid, tid, "", lastActivity, activity);
                        }

                        lastActivity = activity;
                     }
                  }
               }
            }
         }
      }

      return lastActivity;
   }

   private void loadInterfaces(Document document)
   {
      InterfaceFolderParser ifParser;
      Node node;
      Map loadedFolder;

      try
      {
         ifParser = new InterfaceFolderParser(objectMap);
         node = ifParser.searchRootFolder(document);
         loadedFolder = ifParser.loadFolder(node);
         createApplications(loadedFolder);
         objectMap.putAll(loadedFolder);
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error while " +
               "trying to load interfaces: " + e.getMessage());
         converterWarnings.addLast(warning);
      }
   }

   private void createApplications(Map loadedFolder)
   {
      Iterator operations;
      OperationWrapper op;
      String applicationId;
      String applicationName;
      IApplication application;
      IActivity activity;
      Collection activiyIds;
      Method completionMethod;
      Collection accessPoints = null;
      String context;

      operations = loadedFolder.values().iterator();

      while (operations.hasNext())
      {
         op = (OperationWrapper) operations.next();
         applicationId = op.getIdentifier() != null ?
               op.getIdentifier() : model.getDefaultApplicationId();
         applicationName = op.getSystem().getName() + ":" + op.getName();
         application = model.createApplication(applicationId, applicationName,
               op.getDescription(), 0);
         completionMethod = getCompletionMethod(op);
         // @todo (france, ub): should be automatically created
         /*accessPoints = JavaApplicationTypeHelper.calculateMethodAccessPoints(
               completionMethod, "Param", true,
               model.findDataType(PredefinedConstants.SERIALIZABLE_DATA), owwwna);
         */
         if (op.getSystem().isGui())
         {
            HashMap contextParameter = new HashMap();
            String methodName;

            application.setInteractive(true);
            context = PredefinedConstants.JFC_CONTEXT;
            IApplicationContext applicationContext = application.createContext(context, 0);
            methodName = createMethodName(op);
            contextParameter.put(PredefinedConstants.CLASS_NAME_ATT,
                  op.getSystem().getFullName());
            contextParameter.put(PredefinedConstants.METHOD_NAME_ATT, methodName);
            applicationContext.setAllAttributes(contextParameter);
            // @todo (france, ub): broken
            //context.setallacccesspoints(accessPoints.iterator());
         }
         else
         {
            HashMap appParameter = new HashMap();
            String methodName;

            application.setInteractive(false);
            context = PredefinedConstants.ENGINE_CONTEXT;
            IApplicationContext applicationContext = application.createContext(context, 0);
            application.setApplicationType(model.findApplicationType(
                  PredefinedConstants.SESSIONBEAN_APPLICATION));

            methodName = createMethodName(op);
            appParameter.put(PredefinedConstants.REMOTE_INTERFACE_ATT,
                  op.getSystem().getFullName());
            appParameter.put(PredefinedConstants.CREATE_METHOD_NAME_ATT,
                  "create()");
            appParameter.put(PredefinedConstants.HOME_INTERFACE_ATT,
                  op.getSystem().getFullName() + "Home");
            appParameter.put(PredefinedConstants.JNDI_PATH_ATT,
                  op.getSystem().getFullName());
            appParameter.put(PredefinedConstants.IS_LOCAL_ATT,
                  new Boolean(false));
            appParameter.put(PredefinedConstants.METHOD_NAME_ATT,
                  methodName);
            application.setAllAttributes(appParameter);
            // @todo (france, ub): broken
            // context.setallacccesspoints(accessPoints.iterator());
         }

         activiyIds = op.getSupportedActivityIds();
         for (Iterator actIt = activiyIds.iterator(); actIt.hasNext();)
         {
            String id;
            ImplementationType iTypeKey;
            Vector inDataMappings;
            Vector outDataMappings;
            IAccessPoint accPoint;

            id = (String) actIt.next();
            activity = (IActivity) objectMap.get(id);
            iTypeKey = ImplementationType.Application;
            activity.setImplementationType(iTypeKey);
            activity.setApplication(application);
            inDataMappings = new Vector();
            outDataMappings = new Vector();

            for (Iterator inMappings = activity.getAllInDataMappings();
                 inMappings.hasNext();)
            {
               inDataMappings.addElement(inMappings.next());
            }

            for (Iterator outMappings = activity.getAllOutDataMappings();
                 outMappings.hasNext();)
            {
               outDataMappings.addElement(outMappings.next());
            }

            //adjust datamappings
            //@todo use accessPathes
            for (Iterator accPs = accessPoints.iterator(); accPs.hasNext();)
            {
               accPoint = (IAccessPoint) accPs.next();

               if (JavaApplicationTypeHelper.RETURN_VALUE_ACCESS_POINT_NAME.equals(accPoint.getId()))
               {
                  for (int i = 0; i < outDataMappings.size(); i++)
                  {
                     IDataMapping outMapping;

                     outMapping = (IDataMapping) outDataMappings.elementAt(i);
                     if (JavaDataTypeUtils.getReferenceClassName(outMapping.getData()).
                           equals(accPoint.getStringAttribute(PredefinedConstants.CLASS_NAME_ATT)))
                     {
                        outMapping.setActivityAccessPointId(accPoint.getId());
                        outMapping.setContext(context);
                        outDataMappings.remove(outMapping);
                        break;
                     }
                  }
               }
               else
               {
                  for (int i = 0; i < inDataMappings.size(); i++)
                  {
                     IDataMapping inMapping = (IDataMapping) inDataMappings.elementAt(i);
                     if (JavaDataTypeUtils.getReferenceClassName(inMapping.getData()).
                           equals(accPoint.getStringAttribute(PredefinedConstants.CLASS_NAME_ATT)))
                     {
                        inMapping.setActivityAccessPointId(accPoint.getId());
                        inMapping.setContext(context);
                        inDataMappings.remove(inMapping);
                        break;
                     }
                  }
               }
            }

            // get all other accesspoints and try to map them to the still existing
            // in- and outdatamappings
            try
            {
               Class.forName(op.getSystem().getFullName());
            }
            catch (ClassNotFoundException e)
            {
               String message = "Couldn't find class " + op.getSystem().getFullName() +
                     " for operation " + op.getName() + ".";
               trace.warn(message, e);
               throw new InternalException(message, e);
            }

            // @todo (france, ub): broken; ap should be automatically created?
            //accessPoints = JavaApplicationTypeHelper.calculateClassAccessPoints(clazz,
            //      true, false, model.findDataType(PredefinedConstants.SERIALIZABLE_DATA), owna);

            // try to map unmapped inDataMappings to setter methods
            if (inDataMappings.size() > 0)
            {
               for (int i = 0; i < inDataMappings.size(); i++)
               {
                  IDataMapping inMapping;
                  String fullName;
                  String className;
                  String methodName;

                  inMapping = (IDataMapping) inDataMappings.elementAt(i);
                  fullName = JavaDataTypeUtils.getReferenceClassName(inMapping.getData());
                  className = fullName.substring(fullName.lastIndexOf('.') + 1);
                  methodName = "set" + className.substring(0, 1).toUpperCase() +
                        className.substring(1) + "(" + fullName + ")";

                  for (Iterator iterator = accessPoints.iterator(); iterator.hasNext();)
                  {
                     IAccessPoint accessPoint = (IAccessPoint) iterator.next();

                     if (methodName.equals(accessPoint.getId()))
                     {
                        inMapping.setActivityAccessPointId(accessPoint.getId());
                        inMapping.setContext(context);
                        break;
                     }
                  }
               }
            }

            // try to map unmapped outDataMappings to getter methods
            if (outDataMappings.size() > 0)
            {
               for (int i = 0; i < outDataMappings.size(); i++)
               {
                  IDataMapping outMapping;
                  String fullName;
                  String className;
                  String methodName;

                  outMapping = (IDataMapping) outDataMappings.elementAt(i);
                  fullName = JavaDataTypeUtils.getReferenceClassName(outMapping.getData());
                  className = fullName.substring(fullName.lastIndexOf('.') + 1);
                  methodName = "get" + className.substring(0, 1).toUpperCase() +
                        className.substring(1) + "()";

                  for (Iterator iterator = accessPoints.iterator(); iterator.hasNext();)
                  {
                     IAccessPoint accessPoint = (IAccessPoint) iterator.next();

                     if (methodName.equals(accessPoint.getId()))
                     {
                        outMapping.setActivityAccessPointId(accessPoint.getId());
                        outMapping.setContext(context);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private String createMethodName(OperationWrapper op)
   {
      Iterator parameter;
      StringBuffer nameBuffer;
      nameBuffer = new StringBuffer();
      nameBuffer.append(op.getName() + "(");
      parameter = op.getInputParameter().iterator();
      boolean first = true;

      while (parameter.hasNext())
      {
         if (!first)
         {
            nameBuffer.append(", ");
         }
         else
         {
            first = false;
         }
         ParameterWrapper param = (ParameterWrapper) parameter.next();
         nameBuffer.append(param.getClassWrapper().getFullName());
      }

      nameBuffer.append(")");
      return nameBuffer.toString();
   }

   private Method getCompletionMethod(OperationWrapper op)
   {
      Class appClass;
      Method completionMethod;
      Iterator parameters = op.getInputParameter().iterator();
      Class paramClasses[];
      ParameterWrapper param;
      int position = 0;

      paramClasses = new Class[op.getInputParameter().size()];

      try
      {
         while (parameters.hasNext())
         {
            param = (ParameterWrapper) parameters.next();
            paramClasses[position] = Class.forName(param.getClassWrapper().getFullName());
         }

         appClass = Class.forName(op.getSystem().getFullName());
         completionMethod = appClass.getMethod(op.getName(), paramClasses);
      }
      catch (Exception e)
      {
         String message = "Couldn't create method for operation " + op.getName()
               + " from system " + op.getSystem().getFullName() + ".";
         trace.warn(message, e);
         throw new InternalException(message, e);
      }

      return completionMethod;
   }

   private void loadWorkproducts(Document document)
   {
      WorkproductFolderParser wpParser;
      Node node;
      Map loadedFolder;

      try
      {
         wpParser = new WorkproductFolderParser(objectMap);
         node = wpParser.searchRootFolder(document);
         loadedFolder = wpParser.loadFolder(node);
         createData(loadedFolder);
         objectMap.putAll(loadedFolder);
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error while " +
               "trying to load class definitions: " + e.getMessage());
         converterWarnings.addLast(warning);
      }
   }

   private void createData(Map loadedFolder)
   {
      Iterator workProducts;
      WorkproductWrapper wp;
      IData data;
      HashMap existing;
      Boolean dummy;
      String id;

      existing = new HashMap();
      dummy = new Boolean(false);
      workProducts = loadedFolder.values().iterator();

      while (workProducts.hasNext())
      {
         wp = (WorkproductWrapper) workProducts.next();
         id = wp.getIdentifier() != null ? wp.getIdentifier() : wp.getName();

         if (existing.get(id) == null)
         {
            if (wp.getClassWrapper() != null)
            {
               data = model.createData(id,
                     model.findDataType(PredefinedConstants.SERIALIZABLE_DATA),
                     wp.getName(), wp.getDescription(), false, 0,
                     JavaDataTypeUtils.initSerializableBeanAttributes(wp.getClassWrapper().getFullName()));
            }
            else
            {
               data = model.createData(id,
                     model.findDataType(PredefinedConstants.PRIMITIVE_DATA),
                     wp.getName(), wp.getDescription(), false, 0,
                     JavaDataTypeUtils.initPrimitiveAttributes(Type.String, null));
            }

            wp.setData(data);
            existing.put(id, dummy);
         }
      }
   }

   private void loadClasses(Document document)
   {
      ClassFolderParser classParser;
      Node node;
      Map loadedFolder;

      try
      {
         classParser = new ClassFolderParser();
         node = classParser.searchRootFolder(document);
         loadedFolder = classParser.loadFolder(node);
         objectMap.putAll(loadedFolder);
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error while " +
               "trying to load class definitions: " + e.getMessage());
         converterWarnings.addLast(warning);
      }
   }

   /**
    * <p/>
    * Elements in TopEase XBench match to two different things in CARNOT. The mapping
    * table looks as follows:
    * </p>
    * <table>
    * <tr>
    * <th>Type</th>
    * <th>TopEase Name</th>
    * <th>CARNOT element</th>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>IT component</td>
    * <td>
    * Closest match would be an application. But since applications in CARNOT
    * are more specific, applications will be taken directly from operations.
    * </td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>Organization</td>
    * <td>
    * Maps to CARNOT roles or organizations. Since it cannot be distinguished
    * further, CARNOT organizations are used always.
    * </td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>Tool</td>
    * <td>not matched</td>
    * </tr>
    * <tr>
    * <td>4</td>
    * <td>Elect / Mech Module</td>
    * <td>not matched</td>
    * </tr>
    * </table>
    * 
    * @param node 
    */
   private void parseElementNode(Node node)
   {
      // Parse element properties

      String name = null;
      String identifier = null;
      String description = null;
      int identification = 0;
      String id;

      id = SimpleTaskUtil.getId(node);
      NodeList elements = node.getChildNodes();

      for (int n = 0; n < elements.getLength(); ++n)
      {
         if (elements.item(n).getNodeName().equals("name"))
         {
            name = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("identifier"))
         {
            identifier = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("description"))
         {
            description = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("identification"))
         {
            identification = Integer.parseInt(elements.item(n).getFirstChild().getNodeValue());
         }
      }

      identifier = identifier != null ? identifier : model.getDefaultOrganizationId();

      if (identification == 2)
      {
         IOrganization organization = model.createOrganization(identifier, name, description, 0);
         objectMap.put(id, organization);
      }
   }

   /**
    * <p/>
    * Parses process definition properties.
    * Be aware, that a TEProcess is NOT a CARNOT process. Processes in TE
    * are nothing more than a summary of activities. The order in which activities
    * are designed in a process does NOT say anything about the sequence in which
    * they run. Sequences are defined either in activities (sequence activities),
    * or in business value chains (BVCs). BVCs can reference activities from different
    * processes. Thus, as well BVCs as activities having sub-activies correspond to
    * CARNOT processes. The TEProcesses are not relevant for CARNOT at all.
    * </p>
    * 
    * @param node 
    */
   private void parseProcessNode(Node node)
   {
      try
      {
         NodeList activityNodes = SimpleTaskUtil.getContains(node);
         for (int m = 0; m < activityNodes.getLength(); ++m)
         {
            if (activityNodes.item(m).getNodeName().equals("AMActivity"))
            {
               parseSequenceActivity(activityNodes.item(m));
            }
         }
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error while " +
               "trying to create process: " + e.getMessage());
         converterWarnings.addLast(warning);
      }
   }

   /**
    * @param node private Activity parseActivityNode(Node node, ProcessDefinition processDefinition, boolean top)
    *             {
    *             <p/>
    *             incIndent();
    *             <p/>
    *             Activity activity = null;
    *             int activityType = 0;
    *             NodeList elements = null;
    *             ProcessDefinition subProcess = null;
    *             String performerID = null;
    *             String id = null;
    *             String name = null;
    *             String description = null;
    *             activityType = 0;
    *             performerID = null;
    *             <p/>
    *             NamedNodeMap attributes = node.getAttributes();
    *             <p/>
    *             for (int i = 0; i < attributes.getLength(); i++)
    *             {
    *             if (attributes.item(i).getNodeName() == null)
    *             {
    *             continue;
    *             }
    *             else if (attributes.item(i).getNodeName().equals("ID"))
    *             {
    *             id = attributes.item(i).getNodeValue();
    *             }
    *             }
    *             <p/>
    *             elements = node.getChildNodes();
    *             <p/>
    *             for (int n = 0; n < elements.getLength(); ++n)
    *             {
    *             if (elements.item(n).getNodeName().equals("name"))
    *             {
    *             name = elements.item(n).getFirstChild().getNodeValue();
    *             }
    *             else if (elements.item(n).getNodeName().equals("activityType"))
    *             {
    *             activityType = Integer.parseInt(elements.item(n).getFirstChild().getNodeValue());
    *             }
    *             else if (elements.item(n).getNodeName().equals("hasResponsibilityElement"))
    *             {
    *             NodeList referenceNodes = elements.item(n).getChildNodes();
    *             <p/>
    *             for (int m = 0; m < referenceNodes.getLength(); ++m)
    *             {
    *             if (referenceNodes.item(m).getNodeName().equals("reference"))
    *             {
    *             performerID = parseReferenceNode(referenceNodes.item(m));
    *             }
    *             }
    *             }
    *             }
    *             <p/>
    *             // TopEase has a weird mechanism of one "top level activity" per process, we skip this here
    *             <p/>
    *             if (!top)
    *             {
    *             activity = processDefinition.createActivity(model.getDefaultActivityId(), name, description);
    *             <p/>
    *             objectMap.put(id, activity);
    *             <p/>
    *             if (activityType == 2 || activityType == 3)
    *             {
    *             subProcess = model.createProcessDefinition(model.getDefaultProcessDefinitionId(), activity.getName(), "");
    *             <p/>
    *             trace.debug(getIndent() + "*** Creating subprocess definition \"" + subProcess.getName() + "\" ***");
    *             <p/>
    *             activity.setImplementationProcessDefinition(subProcess);
    *             activity.setImplementationType(new ImplementationTypeKey(ImplementationTypeKey.SUBPROCESS));
    *             }
    *             else
    *             {
    *             activity.setImplementationType(new ImplementationTypeKey(ImplementationTypeKey.MANUAL));
    *             <p/>
    *             if (performerID != null)
    *             {
    *             activity.setPerformer(model.findParticipant(performerID));
    *             }
    *             }
    *             }
    *             else
    *             {
    *             subProcess = processDefinition;
    *             }
    *             <p/>
    *             elements = node.getChildNodes();
    *             <p/>
    *             for (int n = 0; n < elements.getLength(); ++n)
    *             {
    *             if (elements.item(n).getNodeName().equals("contains"))
    *             {
    *             NodeList activityNodes = elements.item(n).getChildNodes();
    *             <p/>
    *             if (activityType == 2)
    *             {
    *             Activity lastActivity = null;
    *             <p/>
    *             trace.debug(getIndent() + "Start parsing sequence");
    *             <p/>
    *             for (int m = 0; m < activityNodes.getLength(); ++m)
    *             {
    *             if (activityNodes.item(m).getNodeName().equals("AMActivity"))
    *             {
    *             Activity subActivity = parseActivityNode(activityNodes.item(m), subProcess, false);
    *             <p/>
    *             if (lastActivity != null)
    *             {
    *             if (lastActivity == subActivity)
    *             {
    *             trace.debug("WARNING: lastActivity == subActivity");
    *             }
    *             <p/>
    *             trace.debug(getIndent() + "Linking " + lastActivity.getName() + " -> " + subActivity.getName() + ".");
    *             <p/>
    *             subProcess.createTransition(model.getDefaultTransitionId(), lastActivity.getName(), "",
    *             lastActivity, subActivity);
    *             }
    *             <p/>
    *             lastActivity = subActivity;
    *             }
    *             }
    *             <p/>
    *             trace.debug(getIndent() + "End parsing sequence");
    *             }
    *             else if (activityType == 3)
    *             {
    *             Activity startRoute = null;
    *             Activity endRoute = null;
    *             <p/>
    *             trace.debug(getIndent() + "Start parsing parallel");
    *             <p/>
    *             for (int m = 0; m < activityNodes.getLength(); ++m)
    *             {
    *             if (activityNodes.item(m).getNodeName().equals("AMActivity"))
    *             {
    *             if (startRoute == null)
    *             {
    *             startRoute = subProcess.createActivity(model.getDefaultActivityId(),
    *             "Start-" + subProcess.getName(), "");
    *             endRoute = subProcess.createActivity(model.getDefaultActivityId(),
    *             "End-" + subProcess.getName(), "");
    *             <p/>
    *             startRoute.setSplitType(new JoinSplitTypeKey(JoinSplitTypeKey.AND));
    *             endRoute.setJoinType(new JoinSplitTypeKey(JoinSplitTypeKey.AND));
    *             }
    *             <p/>
    *             Activity subActivity = parseActivityNode(activityNodes.item(m), subProcess, false);
    *             <p/>
    *             trace.debug(getIndent() + "Parallel activity: " + subActivity.getName());
    *             <p/>
    *             subProcess.createTransition(model.getDefaultTransitionId(), "Start-" + subActivity.getName(), "", startRoute, subActivity);
    *             subProcess.createTransition(model.getDefaultTransitionId(), "End-" + subActivity.getName(), "", subActivity, endRoute);
    *             }
    *             }
    *             <p/>
    *             trace.debug(getIndent() + "End parsing parallel");
    *             }
    *             }
    *             }
    *             <p/>
    *             decIndent();
    *             <p/>
    *             return activity;
    *             }
    */

   private IActivity parseStepActivity(Node node, IProcessDefinition processDefinition)
   {
      IActivity activity;
      String id = null;
      String identifier = null;
      String description = null;
      NamedNodeMap attributes;
      int activityType = 0;
      NodeList elements = null;
      String performerID = null;
      String name = null;
      IDataMapping mapping;
      Vector inDataMappings;
      Vector outDataMappings;

      inDataMappings = new Vector();
      outDataMappings = new Vector();
      attributes = node.getAttributes();
      elements = node.getChildNodes();

      for (int i = 0; i < attributes.getLength(); i++)
      {
         if (attributes.item(i).getNodeName() == null)
         {
            continue;
         }
         else if (attributes.item(i).getNodeName().equals("ID"))
         {
            id = attributes.item(i).getNodeValue();
         }
      }

      for (int n = 0; n < elements.getLength(); ++n)
      {
         if (elements.item(n).getNodeName().equals("name"))
         {
            name = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("identifier"))
         {
            identifier = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("description"))
         {
            description = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("activityType"))
         {
            activityType = Integer.parseInt(elements.item(n).getFirstChild().getNodeValue());
         }
         else if (elements.item(n).getNodeName().equals("hasConcernedPartyElement"))
         {
            Node reference;

            reference = SimpleTaskUtil.getChildByName(elements.item(n), "reference");
            performerID = SimpleTaskUtil.parseReferenceNode(reference);
         }
         else if (elements.item(n).getNodeName().equals("sendsFlow"))
         {
            String referenceId;
            WorkproductWrapper wp;
            NodeList referenceNodes = elements.item(n).getChildNodes();

            for (int m = 0; m < referenceNodes.getLength(); ++m)
            {
               if (referenceNodes.item(m).getNodeName().equals("reference"))
               {
                  referenceId = SimpleTaskUtil.parseReferenceNode(referenceNodes.item(m));
                  wp = (WorkproductWrapper) objectMap.get(referenceId);

                  if (wp != null)
                  {
                     outDataMappings.addElement(wp);
                  }
               }
            }
         }
         else if (elements.item(n).getNodeName().equals("receivesFlow"))
         {
            String referenceId;
            WorkproductWrapper wp;
            NodeList referenceNodes = elements.item(n).getChildNodes();

            for (int m = 0; m < referenceNodes.getLength(); ++m)
            {
               if (referenceNodes.item(m).getNodeName().equals("reference"))
               {
                  referenceId = SimpleTaskUtil.parseReferenceNode(referenceNodes.item(m));
                  wp = (WorkproductWrapper) objectMap.get(referenceId);

                  if (wp != null)
                  {
                     inDataMappings.addElement(wp);
                  }
               }
            }
         }
      }

      if ((activityType != 1) && (activityType != 0))
      {
         throw new PublicException("Node " + name + " is not a step!");
      }

      identifier = identifier != null ? identifier : processDefinition.getDefaultActivityId();
      activity = processDefinition.createActivity(identifier, name, description, 0);

      if (performerID != null)
      {
         IModelParticipant participant;
         ImplementationType key;

         participant = (IModelParticipant) objectMap.get(performerID);
         activity.setPerformer(participant);
         key = ImplementationType.Manual;
         activity.setImplementationType(key);
      }

      for (int i = 0; i < inDataMappings.size(); i++)
      {
         WorkproductWrapper wp = (WorkproductWrapper) inDataMappings.elementAt(i);
         mapping = activity.createDataMapping(wp.getName(), wp.getName(), wp.getData(), Direction.IN);
         mapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      }

      for (int i = 0; i < outDataMappings.size(); i++)
      {
         WorkproductWrapper wp = (WorkproductWrapper) outDataMappings.elementAt(i);
         mapping = activity.createDataMapping(wp.getName(), wp.getName(), wp.getData(), Direction.OUT);
         mapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      }

      objectMap.put(id, activity);
      return activity;
   }

   private IActivity parseForkActivity(Node node, IProcessDefinition process)
   {
      return null;
   }

   private IActivity parseLoopActivity(Node node, IProcessDefinition process)
   {
      return null;
   }

   private IActivity parseInterruptActivity(Node node, IProcessDefinition process)
   {
      return null;
   }

   private IActivity createSubProcessActivity(Node node, IProcessDefinition processDefinition)
   {
      String[] identNameDescr;
      String name = null;
      String identifier = null;
      String description = null;
      IActivity activity;
      IProcessDefinition subProcess;
      String id;

      id = SimpleTaskUtil.getId(node);
      identNameDescr = SimpleTaskUtil.getIdentNameDescr(node);
      identifier = identNameDescr[0] != null ?
            identNameDescr[0] : processDefinition.getDefaultActivityId();
      name = identNameDescr[1];
      description = identNameDescr[2];
      activity = processDefinition.createActivity(identifier, name, description, 0);
      activity.setImplementationType(ImplementationType.SubProcess);
      subProcess = parseSequenceActivity(node);
      activity.setImplementationProcessDefinition(subProcess);
      objectMap.put(id, activity);
      return activity;
   }

   /**
    * <p/>
    * This method is the acces point when converting AMActivities to CARNOT elements.
    * It is called from parsing TEProcesses, or when subprocesses are found in
    * activities of type "Sequence". The TEProcess must have elements of type "Sequence"
    * only.
    * </p>
    * <p/>
    * TopEase has different types of activites that match to different things in CARNOT.
    * AMActivities match to CARNOT model elements as follows:
    * </p>
    * <table>
    * <tr>
    * <th>Type</th>
    * <th>TopEase Name</th>
    * <th>CARNOT element</th>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>Step</td>
    * <td>Activity</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>Sequence</td>
    * <td>Process</td>
    * </tr>
    * <tr>
    * <td>3</td>
    * <td>Branch</td>
    * <td>XOR Split</td>
    * </tr>
    * <tr>
    * <td>4</td>
    * <td>Fork</td>
    * <td>AND Split</td>
    * </tr>
    * <tr>
    * <td>5</td>
    * <td>Loop</td>
    * <td>
    * CARNOT 2.5: Subprocess with loop<br>
    * CARNOT 2.6: activity block with 1 - n jumps back or forward
    * </td>
    * </tr>
    * <tr>
    * <td>6</td>
    * <td>Redo</td>
    * <td>
    * CARNOT 2.5: end of looped subprocess leading to restart of subprocess <br>
    * CARNOT 2.6 one of the above mentioned jumps back
    * </td>
    * </tr>
    * <tr>
    * <td>7</td>
    * <td>Exit</td>
    * <td>
    * CARNOT 2.5: end of subprocess without restart of this subprocess
    * CARNOT 2.6: one of the above mentioned jumps forward
    * </td>
    * </tr>
    * <tr>
    * <td>8</td>
    * <td>Interrupt</td>
    * <td>Activity with JMS application type (waiting for external event)</td>
    * </tr>
    * <tr>
    * <td>9</td>
    * <td>Start</td>
    * <td>not existing in CARNOT, implicit, can be ignored</td>
    * </tr>
    * <tr>
    * <td>10</td>
    * <td>End</td>
    * <td>not existing in CARNOT, implicit, cannot be ignored</td>
    * </tr>
    * </table>
    */
   private IProcessDefinition parseSequenceActivity(Node node)
   {
      String id;
      IProcessDefinition processDefinition = null;
      NodeList subActivities = null;
      int activityType = 0;
      NodeList elements = null;
      String name = null;
      String identifier = null;
      String description = null;

      id = SimpleTaskUtil.getId(node);
      elements = node.getChildNodes();

      for (int n = 0; n < elements.getLength(); ++n)
      {
         if (elements.item(n).getNodeName().equals("name"))
         {
            name = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("identifier"))
         {
            identifier = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("description"))
         {
            description = elements.item(n).getFirstChild().getNodeValue();
         }
         else if (elements.item(n).getNodeName().equals("activityType"))
         {
            activityType = Integer.parseInt(elements.item(n).getFirstChild().getNodeValue());
         }
         else if (elements.item(n).getNodeName().equals("contains"))
         {
            subActivities = elements.item(n).getChildNodes();
         }
      }

      if (activityType != 2)
      {
         throw new PublicException("Node " + name + " is not a sequence!");
      }

      identifier = identifier != null ? identifier : model.getDefaultProcessDefinitionId();
      processDefinition = model.createProcessDefinition(identifier, name, description);

      try
      {
         // handle activities that are in the sequence.
         if (subActivities != null)
         {
            IActivity currentActivity = null;
            IActivity[] lastActivities = null;
            JoinSplitType lastSplit = JoinSplitType.None;
            NodeList subActivityElements;
            Node subActivityElement;
            Node subActivity;
            String nodeName;
            int subActType;
            Vector orderedSubActivities;

            orderedSubActivities = new Vector(subActivities.getLength() / 2, 1);
            for (int subActPos = 0; subActPos < subActivities.getLength(); ++subActPos)
            {
               int rank = 0;
               subActType = -1;
               subActivity = subActivities.item(subActPos);
               subActivityElements = subActivity.getChildNodes();

               if ((subActivityElements != null)
                     && ("AMActivity".equals(subActivity.getNodeName())))
               {
                  subActType = 0;

                  for (int m = 0; m < subActivityElements.getLength(); ++m)
                  {
                     subActivityElement = subActivityElements.item(m);
                     nodeName = subActivityElement.getNodeName();

                     if ("rank".equals(nodeName))
                     {
                        rank = Integer.parseInt(subActivityElements.item(m).
                              getFirstChild().getNodeValue());
                        break;
                     }
                  }

                  orderedSubActivities.addElement(new RankedActivityHolder(rank, subActivity));
               }
            }

            orderedSubActivities = orderRankedActivites(orderedSubActivities);

            for (int subActPos = 0; subActPos < orderedSubActivities.size(); subActPos++)
            {
               RankedActivityHolder holder = (RankedActivityHolder)
                     orderedSubActivities.elementAt(subActPos);
               subActivity = holder.getActivity();
               subActivityElements = subActivity.getChildNodes();

               if ((subActivityElements != null)
                     && ("AMActivity".equals(subActivity.getNodeName())))
               {
                  subActType = 0;

                  for (int m = 0; m < subActivityElements.getLength(); ++m)
                  {
                     subActivityElement = subActivityElements.item(m);
                     nodeName = subActivityElement.getNodeName();

                     if ("activityType".equals(nodeName))
                     {
                        subActType = Integer.parseInt(subActivityElements.item(m).
                              getFirstChild().getNodeValue());
                     }
                  }


                  // Step activities mostly do not have the activity type attribute
                  if (subActType == 0)
                  {
                     subActType = 1;
                  }

                  if (subActType == 1) // Step
                  {
                     currentActivity = parseStepActivity(subActivity,
                           processDefinition);
                     lastActivities = createTransitions(lastActivities, currentActivity,
                           processDefinition, lastSplit);
                  }
                  else if (subActType == 2) // Sequence
                  {
                     currentActivity = createSubProcessActivity(subActivity,
                           processDefinition);
                     lastActivities = createTransitions(lastActivities, currentActivity,
                           processDefinition, lastSplit);
                  }
                  else if (subActType == 3) // Branch
                  {
                     if (lastActivities == null)
                     {
                        lastActivities = new IActivity[1];
                        lastActivities[0] = createLogicalRoute(processDefinition);
                     }

                     ActivityAndConditionHolder actsAndCons;
                     actsAndCons = parseSplitActivity(subActivity, processDefinition);

                     if (lastActivities.length > 1)
                     {
                        lastActivities = createTransitions(lastActivities,
                              actsAndCons.getActivities(), processDefinition, lastSplit, JoinSplitType.Xor,
                              actsAndCons.getConditions());
                     }
                     else
                     {
                        lastActivities = createTransitions(lastActivities[0],
                              actsAndCons.getActivities(), processDefinition, JoinSplitType.Xor,
                              actsAndCons.getConditions());
                     }

                     lastSplit = JoinSplitType.Xor;
                  }
                  else if (subActType == 4) // Fork
                  {
                     if (lastActivities == null)
                     {
                        lastActivities = new IActivity[1];
                        lastActivities[0] = createLogicalRoute(processDefinition);
                     }

                     ActivityAndConditionHolder actsAndCons;
                     actsAndCons = parseSplitActivity(subActivity,
                           processDefinition);

                     if (lastActivities.length > 1)
                     {
                        lastActivities = createTransitions(lastActivities,
                              actsAndCons.getActivities(), processDefinition, lastSplit, JoinSplitType.And,
                              actsAndCons.getConditions());
                     }
                     else
                     {
                        lastActivities = createTransitions(lastActivities[0],
                              actsAndCons.getActivities(), processDefinition, JoinSplitType.And,
                              actsAndCons.getConditions());
                     }

                     lastSplit = JoinSplitType.And;
                  }
                  else if (subActType == 5) // Loop
                  {
                     // @todo Loop activity
                     parseLoopActivity(node, processDefinition);
                  }
                  else if (subActType == 6) // Redo
                  {
                     // @todo Redo activity
                     parseLoopActivity(node, processDefinition);
                  }
                  else if (subActType == 7) // Exit
                  {
                     // @todo Exit activity
                  }
                  else if (subActType == 8) // Interrupt
                  {
                     // @todo Interrupt activity
                     parseInterruptActivity(node, processDefinition);
                  }
                  else if (subActType == 9) // Start
                  {
                     // ignore
                  }
                  else if (subActType == 10) // Stop
                  {
                     // @todo Stop activity
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.warn(e);
         ConvertWarningException warning = new ConvertWarningException("Error in " +
               "process " + processDefinition.getName() + ": " + e.getMessage());
         warning.setProcessDefinition(processDefinition);
         converterWarnings.addLast(warning);
      }

      objectMap.put(id, processDefinition);
      return processDefinition;
   }

   private Vector orderRankedActivites(Vector unOrderedActivities)
   {
      Vector orderedActivities;
      RankedActivityHolder rankedAH = null;

      orderedActivities = new Vector(unOrderedActivities.size());

      while (unOrderedActivities.size() > 0)
      {
         RankedActivityHolder tempHolder;
         int minMarker;

         minMarker = 1000;

         for (int unOrderedPos = 0; unOrderedPos < unOrderedActivities.size(); unOrderedPos++)
         {
            tempHolder = (RankedActivityHolder) unOrderedActivities.elementAt(unOrderedPos);

            if (minMarker > tempHolder.getRank())
            {
               minMarker = tempHolder.getRank();
               rankedAH = tempHolder;
            }
         }

         orderedActivities.add(rankedAH);
         unOrderedActivities.remove(rankedAH);
      }

      return orderedActivities;
   }

   private ActivityAndConditionHolder parseSplitActivity(Node branchActivityNode,
         IProcessDefinition processDefinition)
   {
      NodeList subAMAs; // AMA = AMActivity
      IActivity[] splitActivities;
      NodeList activityProps;
      int currActType = 0;
      String conditions[];
      Node subActivity;
      subAMAs = SimpleTaskUtil.getContains(branchActivityNode);
      splitActivities = new IActivity[subAMAs.getLength()];
      conditions = new String[subAMAs.getLength()];

      // loop over all activities contained by the split activity
      for (int splitPos = 0; splitPos < subAMAs.getLength(); ++splitPos)
      {
         subActivity = subAMAs.item(splitPos);
         activityProps = subActivity.getChildNodes();

         // loop over all attributes of the current activity contained by the split
         // activity
         if ((activityProps != null)
               && ("AMActivity".equals(subActivity.getNodeName())))
         {
            for (int actPropPos = 0; actPropPos < activityProps.getLength(); ++actPropPos)
            {
               if (activityProps.item(actPropPos).getNodeName().equals("activityType"))
               {
                  currActType = Integer.parseInt(activityProps.item(actPropPos)
                        .getChildNodes().item(0).getNodeValue());
               }
               else if (activityProps.item(actPropPos).getNodeName().equals("precondition"))
               {
                  conditions[splitPos] = activityProps.item(actPropPos).getChildNodes()
                        .item(0).getNodeValue();
               }
            }

            if ((currActType == 1) || (currActType == 0))
            {
               splitActivities[splitPos] = parseStepActivity(subAMAs.item(splitPos), processDefinition);
            }
            else if (currActType == 2)
            {
               splitActivities[splitPos] = createSubProcessActivity(subAMAs.item(splitPos), processDefinition);
            }
            else
            {
               throw new PublicException("Only step or sequence activities " +
                     "allowed in splits!");
            }
         }
      }

      return new ActivityAndConditionHolder(splitActivities, conditions);
   }

   private IActivity createLogicalRoute(IProcessDefinition processDefinition)
   {
      IActivity logicalRouteActivity;
      logicalRouteActivity = processDefinition.createActivity(processDefinition.getDefaultActivityId(), "Logical Route", null, 0);
      logicalRouteActivity.setSplitType(JoinSplitType.Xor);
      return logicalRouteActivity;
   }

   /**
    * creates transitions from one or more last activities to the current one. More
    * than one last activities are found after splits (fork or branch).
    * 
    * @param lastActivities    an array with the last activities
    * @param currentActivity   the current activitiy
    * @param processDefinition the process context in which the activities reside.
    * @return always an array containing only one object, which is the currentActivity
    *         given as parameter
    */
   private IActivity[] createTransitions(IActivity[] lastActivities,
         IActivity currentActivity,
         IProcessDefinition processDefinition,
         JoinSplitType splitType)
   {
      currentActivity.setJoinType(splitType);

      if (lastActivities != null)
      {
         for (int i = 0; i < lastActivities.length; ++i)
         {
            createTransition(lastActivities[i], currentActivity, processDefinition);
         }
      }

      lastActivities = new IActivity[1];
      lastActivities[0] = currentActivity;
      return lastActivities;
   }

   private IActivity[] createTransitions(IActivity lastActivity,
         IActivity[] currentActivities,
         IProcessDefinition processDefinition,
         JoinSplitType joinType,
         String[] conditions)
   {
      ITransition transition;
      lastActivity.setSplitType(joinType);

      if (lastActivity == null)
      {
         lastActivity = createLogicalRoute(processDefinition);
      }

      for (int i = 0; i < currentActivities.length; ++i)
      {
         transition = createTransition(lastActivity, currentActivities[i], processDefinition);
         transition.setCondition(conditions[i]);
      }

      return currentActivities;
   }

   private IActivity[] createTransitions(IActivity[] lastActivities,
         IActivity[] currentActivities,
         IProcessDefinition processDefinition,
         JoinSplitType joinType,
         JoinSplitType splitType,
         String[] conditions)
   {
      IActivity logicalRoute;

      if ((lastActivities == null) || (currentActivities == null))
      {
         throw new PublicException("This method can only be called if there are " +
               "multiple last and multiple current activities!");
      }

      logicalRoute = createLogicalRoute(processDefinition);
      createTransitions(lastActivities, logicalRoute, processDefinition, joinType);
      lastActivities = createTransitions(logicalRoute, currentActivities,
            processDefinition, splitType, conditions);

      return lastActivities;
   }

   private ITransition createTransition(IActivity predecessor, IActivity successor,
         IProcessDefinition processDefinition)
   {
      return processDefinition.createTransition(processDefinition.getDefaultTransitionId(),
            processDefinition.getDefaultTransitionId(), null, predecessor, successor);
   }

   /**
    * Generates Java code for all classes defined in the TopEase model.
    */
   private void generateClasses()
   {
      Iterator iterator = classTags.iterator();

      while (iterator.hasNext())
      {
         ClassTag classTag = (ClassTag) iterator.next();

         generateInterface(classTag);
         generateClass(classTag);
      }

      StringBuffer javac = new StringBuffer();

      javac.append(Parameters.instance().getString("Java.JDKHome", "c:/programme/jdk1.3.1"));
      javac.append("/bin/javac ");
      javac.append("-nowarn -d ");
      javac.append(codeGenerationClassesDir.getAbsolutePath());
      javac.append(" ");
      javac.append(codeGenerationBaseDir.getAbsolutePath());
      javac.append("/*.java");

      System.out.println(javac.toString());

      try
      {
         Runtime.getRuntime().exec(javac.toString());
      }
      catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }
   }

   private void generateInterface(ClassTag classTag)
   {
      PrintStream stream = null;

      try
      {
         File file = new File(codeGenerationBaseDir, classTag.getName() + ".java");

         if (!file.exists())
         {
            file.createNewFile();
         }

         stream = new PrintStream(new FileOutputStream(file));
      }
      catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }

      stream.println("package topease;");
      stream.println();
      createJavaDoc(stream, "Generated by Eclipse Stardust.");
      stream.println("public interface " + classTag.getName());
      stream.println("{");

      Iterator iterator = classTag.getAllAttributes().iterator();

      while (iterator.hasNext())
      {
         AttributeTag attributeTag = (AttributeTag) iterator.next();

         createJavaDoc(stream, "");
         stream.println("   public " + attributeTag.getType() + " get" + getMethodSuffix(attributeTag.getName()) + "();");
         createJavaDoc(stream, "");
         stream.println("   public void set" + getMethodSuffix(attributeTag.getName()) + "(" + attributeTag.getType()
               + " " + attributeTag.getName() + ");");
      }

      stream.println("}");
   }

   private void generateClass(ClassTag classTag)
   {
      PrintStream stream = null;

      try
      {
         File file = new File(codeGenerationImplDir, classTag.getName() + "Impl.java");

         if (!file.exists())
         {
            file.createNewFile();
         }

         stream = new PrintStream(new FileOutputStream(file));
      }
      catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }

      stream.println("package topease.impl;");
      stream.println();
      stream.println("import topease.*;");
      stream.println();
      createJavaDoc(stream, "Generated by Eclipse Stardust.");
      stream.println("public class " + classTag.getName() + "Impl extends " + classTag.getName());
      stream.println("{");

      Iterator iterator = classTag.getAllAttributes().iterator();

      while (iterator.hasNext())
      {
         AttributeTag attributeTag = (AttributeTag) iterator.next();
         stream.println("   private " + attributeTag.getType() + " " + attributeTag.getName() + ";");
      }

      stream.println();
      createJavaDoc(stream, "Default constructor.");
      stream.println("   public " + classTag.getName() + "Impl()");
      stream.println("   {");
      iterator = classTag.getAllAttributes().iterator();

      while (iterator.hasNext())
      {
         AttributeTag attributeTag = (AttributeTag) iterator.next();

         stream.println("   " + attributeTag.getName() + " = new " + attributeTag.getType() + "();");
      }

      stream.println("   }");
      iterator = classTag.getAllAttributes().iterator();

      while (iterator.hasNext())
      {
         AttributeTag attributeTag = (AttributeTag) iterator.next();

         createJavaDoc(stream, "");
         stream.println("   public " + attributeTag.getType() + " get" + getMethodSuffix(attributeTag.getName()) + "()");
         stream.println("   {");
         stream.println("      return " + attributeTag.getName() + ";");
         stream.println("   }");
         stream.println();

         createJavaDoc(stream, "");
         stream.println("   public void set" + getMethodSuffix(attributeTag.getName()) + "(" + attributeTag.getType()
               + " " + attributeTag.getName() + ")");
         stream.println("   {");
         stream.println("      this." + attributeTag.getName() + " = " + attributeTag.getName() + ";");
         stream.println("   }");
      }

      stream.println("}");
   }

   /**
    * @param stream 
    * @param text   
    */
   private void createJavaDoc(PrintStream stream, String text)
   {
      stream.println("/**");
      stream.println(" * " + text);
      stream.println(" */");
   }

   private String getMethodSuffix(String property)
   {
      return property.substring(0, 1).toUpperCase() + property.substring(1);
   }

   public String transformNameToID(String name)
   {
      name.toUpperCase();
      name.replace(' ', '_');
      return name;
   }
}

class ClassTag
{
   private String name;
   private Vector attributes;

   public ClassTag(String name)
   {
      this.name = name;
      attributes = new Vector();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void addAttribute(AttributeTag attributeTag)
   {
      attributes.add(attributeTag);
   }

   public Collection getAllAttributes()
   {
      return attributes;
   }
}

class AttributeTag
{
   private String name;
   private String type;
   private boolean primitive;

   public AttributeTag(String name)
   {
      this(name, "String");

      primitive = true;
   }

   public AttributeTag(String name, String type)
   {
      this.name = name;
      this.type = type;

      primitive = false;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public boolean isPrimitive()
   {
      return primitive;
   }
}

class ActivityAndConditionHolder
{
   private IActivity[] activities;
   private String[] conditions;

   public ActivityAndConditionHolder(IActivity[] activities, String[] conditions)
   {
      this.activities = activities;
      this.conditions = conditions;
      cleanTextTagEntries();
      removeParagraphs();
   }

   public IActivity[] getActivities()
   {
      return activities;
   }

   public void setActivities(IActivity[] activities)
   {
      this.activities = activities;
   }

   public String[] getConditions()
   {
      return conditions;
   }

   public void setConditions(String[] conditions)
   {
      this.conditions = conditions;
   }

   private void cleanTextTagEntries()
   {
      IActivity[] cleanedActivities;
      String[] cleandConditions;
      int count = 0;

      for (int i = 0; i < activities.length; i++)
      {
         if ((activities[i] != null) || (conditions[i]) != null)
         {
            count++;
         }
      }

      cleanedActivities = new IActivity[count];
      cleandConditions = new String[count];
      count = 0;

      for (int i = 0; i < activities.length; i++)
      {
         if ((activities[i] != null) || (conditions[i]) != null)
         {
            cleanedActivities[count] = activities[i];
            cleandConditions[count] = conditions[i];
            count++;
         }
      }

      activities = cleanedActivities;
      conditions = cleandConditions;
   }

   private void removeParagraphs()
   {
      String condition;
      int length;
      StringBuffer condBuf;

      for (int i = 0; i < conditions.length; i++)
      {
         condition = conditions[i].trim();
         length = condition.length();
         condBuf = new StringBuffer(conditions[i].trim());
         condBuf.delete(0, 4);
         condBuf.delete(length - 9, length);
         conditions[i] = condBuf.toString().trim();
      }
   }
}

class RankedActivityHolder
{
   private int rank;
   private Node activity;

   public RankedActivityHolder(int rank, Node activity)
   {
      this.rank = rank;
      this.activity = activity;
   }

   public int getRank()
   {
      return rank;
   }

   public void setRank(int rank)
   {
      this.rank = rank;
   }

   public Node getActivity()
   {
      return activity;
   }

   public void setActivity(Node activity)
   {
      this.activity = activity;
   }
}