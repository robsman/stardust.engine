/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2011 Sungard CSA LLC
 */
package org.eclipse.stardust.engine.ws;

import static javax.xml.bind.DatatypeConverter.parseBoolean;
import static javax.xml.bind.DatatypeConverter.parseByte;
import static javax.xml.bind.DatatypeConverter.parseDateTime;
import static javax.xml.bind.DatatypeConverter.parseDouble;
import static javax.xml.bind.DatatypeConverter.parseFloat;
import static javax.xml.bind.DatatypeConverter.parseInt;
import static javax.xml.bind.DatatypeConverter.parseLong;
import static javax.xml.bind.DatatypeConverter.parseShort;
import static javax.xml.bind.DatatypeConverter.parseString;
import static javax.xml.bind.DatatypeConverter.printBoolean;
import static javax.xml.bind.DatatypeConverter.printByte;
import static javax.xml.bind.DatatypeConverter.printDateTime;
import static javax.xml.bind.DatatypeConverter.printDouble;
import static javax.xml.bind.DatatypeConverter.printFloat;
import static javax.xml.bind.DatatypeConverter.printInt;
import static javax.xml.bind.DatatypeConverter.printLong;
import static javax.xml.bind.DatatypeConverter.printShort;
import static javax.xml.bind.DatatypeConverter.printString;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PRIMITIVE_DATA;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;
import static org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils.getLastXPathPart;
import static org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils.getXPathWithoutIndexes;
import static org.eclipse.stardust.engine.ws.WebServiceEnv.currentWebServiceEnvironment;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.fromXto;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DataMappingDetails;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.api.ws.DocumentXto;
import org.eclipse.stardust.engine.api.ws.DocumentsXto;
import org.eclipse.stardust.engine.api.ws.FolderXto;
import org.eclipse.stardust.engine.api.ws.FoldersXto;
import org.eclipse.stardust.engine.api.ws.InstancePropertiesXto;
import org.eclipse.stardust.engine.api.ws.NoteXto;
import org.eclipse.stardust.engine.api.ws.ObjectFactory;
import org.eclipse.stardust.engine.api.ws.ParameterXto;
import org.eclipse.stardust.engine.api.ws.ParametersXto;
import org.eclipse.stardust.engine.api.ws.XmlValueXto;
import org.eclipse.stardust.engine.core.interactions.ModelResolver;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.Text;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class DataFlowUtils
{
   public final static QName NOTES_NAMESPACE = new QName(
         "http://eclipse.org/stardust/ws/v2012a/api", "Note");

   private static final Logger trace = LogManager.getLogger(DataFlowUtils.class);

   private static final JAXBContext WS_API_JAXB_CONTEXT;

   private static final ObjectFactory WS_API_OBJECT_FACTORY = new ObjectFactory();

   static
   {
      JAXBContext context = null;
      try
      {
         context = JAXBContext.newInstance(ObjectFactory.class);
      }
      catch (JAXBException je)
      {
         trace.error(
               "Failed initializing JAXB context for Web Service API, some parts will not be functional.",
               je);
      }

      WS_API_JAXB_CONTEXT = context;
   }

   public static InstancePropertiesXto marshalInstanceProperties(ActivityInstance ai)
   {
      WebServiceEnv wsEnv = currentWebServiceEnvironment();
      Model model = wsEnv.getModel(ai.getModelOID());

      InstancePropertiesXto instanceProperties = null;
      List<DataPath> descriptorDefinitions = ai.getDescriptorDefinitions();
      if (descriptorDefinitions != null)
      {
         instanceProperties = new InstancePropertiesXto();

         for (DataPath dataPath : descriptorDefinitions)
         {
            IDescriptorProvider descriptorProvider = (IDescriptorProvider) ai;

            ParameterXto parameterXto = marshalDescriptorValue(descriptorProvider, model,
                  dataPath, wsEnv);

            if (parameterXto != null)
            {
               instanceProperties.getInstanceProperty().add(parameterXto);
            }
            else
            {
               trace.warn("Marshaling of ActivityInstanceProperty (" + dataPath.getId()
                     + ") failed. Property ignored.");
               // throw new UnsupportedOperationException(
               // "Marshaling of ActivityInstanceProperty (" + dataPath.getId()
               // + ") failed.");
            }
         }
      }
      return instanceProperties;
   }

   private static boolean isCaseDescriptor(DataPath dataPath)
   {
      return dataPath.getQualifiedId().startsWith(
            "{" + PredefinedConstants.PREDEFINED_MODEL_ID);
   }

   public static InstancePropertiesXto marshalInstanceProperties(ProcessInstance pi, boolean includeDescriptors)
   {
      WebServiceEnv wsEnv = currentWebServiceEnvironment();
      Model model = wsEnv.getModel(pi.getModelOID());

      return marshalInstanceProperties(pi, includeDescriptors, model, wsEnv);
   }

   public static InstancePropertiesXto marshalInstanceProperties(ProcessInstance pi,
         boolean includeDescriptors, Model model, ModelResolver resolver)
   {
      InstancePropertiesXto instanceProperties = null;
      if (pi instanceof ProcessInstanceDetails)
      {
         IDescriptorProvider descriptorProvider = (IDescriptorProvider) pi;

         if (includeDescriptors)
         {
            // get Descriptors
            List<DataPath> descriptorDefinitions = null;
            if (pi instanceof ProcessInstanceDetails)
            {
               descriptorDefinitions = ((ProcessInstanceDetails) pi).getDescriptorDefinitions();
            }

            // add Descriptors
            if (descriptorDefinitions != null && !descriptorDefinitions.isEmpty())
            {
               instanceProperties = new InstancePropertiesXto();

               for (DataPath dataPath : descriptorDefinitions)
               {
                  ParameterXto parameterXto = marshalDescriptorValue(descriptorProvider,
                        model, dataPath, resolver);

                  if (parameterXto != null)
                  {
                     instanceProperties.getInstanceProperty().add(parameterXto);
                  }
                  else
                  {
                     trace.warn("Marshaling of ProcessInstanceProperty ("
                           + dataPath.getId() + ") failed. Property ignored.");
                     // throw new UnsupportedOperationException(
                     // "Marshaling of ProcessInstanceProperty (" + dataPath.getId()
                     // + ") failed.");
                  }
               }

            }
         }
      }
      // add Notes
      // marshalNotes(pi, instanceProperties);

      return instanceProperties;
   }

   public static InstancePropertiesXto marshalProcessInstanceProperties(
         long processInstanceOid, Map<String, Serializable> dataPaths)
   {
      WebServiceEnv wsEnv = currentWebServiceEnvironment();
      ServiceFactory sf = wsEnv.getServiceFactory();
      WorkflowService wfs = sf.getWorkflowService();

      ProcessInstance pi = wfs.getProcessInstance(processInstanceOid);
      Model model = wsEnv.getModel(pi.getModelOID());
      ProcessDefinition process = model.getProcessDefinition(pi.getProcessID());

      List<DataPath> descriptorDefinitions = null;
      if (pi instanceof ProcessInstanceDetails)
      {
         descriptorDefinitions = ((ProcessInstanceDetails) pi).getDescriptorDefinitions();
      }

      InstancePropertiesXto instanceProperties = new InstancePropertiesXto();
      if ( !dataPaths.keySet().isEmpty())
      {
         for (String dataPathId : dataPaths.keySet())
         {
            DataPath dataPath = process.getDataPath(dataPathId);

            IDescriptorProvider descriptorProvider = (IDescriptorProvider) pi;
            ParameterXto parameterXto = null;
            if (dataPath == null)
            {
               // lookup if its a case descriptor
               for (DataPath descriptorDataPath : descriptorDefinitions)
               {
                  if (descriptorDataPath.getId().equals(dataPathId))
                  {
                     parameterXto = marshalDescriptorValue(descriptorProvider, model,
                           descriptorDataPath, wsEnv);
                     break;
                  }
               }
            }
            else
            {
               parameterXto = marshalInDataValue(model, dataPath,
                     dataPaths.get(dataPath.getId()), wsEnv);
            }

            if (parameterXto != null)
            {
               instanceProperties.getInstanceProperty().add(parameterXto);
            }
            else
            {
               trace.warn("Marshaling of ProcessInstanceProperty (" + dataPath.getId()
                     + ") failed. Property not marshaled");
               // throw new UnsupportedOperationException(
               // "Unmarshaling of ProcessInstanceProperty (" + dataPath.getId()
               // + ") failed.");
            }

         }

      }

      // add Notes
      marshalNotes(pi, instanceProperties, wfs);

      // return null instead of empty list
      if (instanceProperties.getInstanceProperty().isEmpty())
      {
         instanceProperties = null;
      }
      return instanceProperties;
   }

   private static ParameterXto marshalDescriptorValue(IDescriptorProvider pid,
         Model model, DataPath dataPath, ModelResolver resolver)
   {
      Serializable descriptorValue = (Serializable) pid.getDescriptorValue(dataPath.getId());
      ParameterXto parameterXto = null;
      if (isCaseDescriptor(dataPath))
      {
         // handle case descriptors
         parameterXto = marshalPrimitiveValue(null, dataPath.getId(),
               dataPath.getMappedType(), descriptorValue);
      }
      else
      {
         parameterXto = marshalInDataValue(model, dataPath, descriptorValue, resolver);
      }
      return parameterXto;
   }

   private static void marshalNotes(ProcessInstance pi,
         InstancePropertiesXto instanceProperties, WorkflowService wfs)
   {
      // resolve ScopeProcessInstance
      ProcessInstance scopePi = getScopePi(pi, wfs);
      if (scopePi.getAttributes() != null)
      {
         ProcessInstanceAttributes attributes = scopePi.getAttributes();
         List<Note> notes = attributes.getNotes();
         for (Note note : notes)
         {
            instanceProperties.getInstanceProperty().add(marshalNote(note));
         }
      }
   }

   private static ProcessInstance getScopePi(ProcessInstance pi, WorkflowService wfs)
   {
      ProcessInstance scopePi = null;
      if (pi.getScopeProcessInstanceOID() != pi.getOID())
      {
         scopePi = wfs.getProcessInstance(pi.getScopeProcessInstanceOID());
      }
      else
      {
         scopePi = pi;
      }
      return scopePi;
   }

   public static ParameterXto marshalNote(Note note)
   {
      ParameterXto ret = new ParameterXto();

      ret.setName(NOTES_NAMESPACE.getLocalPart());
      ret.setNamespace(NOTES_NAMESPACE.getNamespaceURI());
      ret.setPrimitive(note.getText());
      ret.setType(NOTES_NAMESPACE);

      XmlValueXto xml = new XmlValueXto();

      NoteXto xto = new NoteXto();
      xto.setText(note.getText());
      xto.setTimestamp(note.getTimestamp());
      xto.setUser(XmlAdapterUtils.toWs(note.getUser()));

      if (ContextKind.ActivityInstance.equals(note.getContextKind()))
      {
         xto.setActivityOid(note.getContextOid());
      }
      else if (ContextKind.ProcessInstance.equals(note.getContextKind()))
      {
         xto.setProcessOid(note.getContextOid());
      }

      JAXBElement<NoteXto> xtoWrapper = WS_API_OBJECT_FACTORY.createNote(xto);

      if (null != xtoWrapper)
      {
         try
         {
            DOMResult res = new DOMResult();
            WS_API_JAXB_CONTEXT.createMarshaller().marshal(xtoWrapper, res);

            xml.getAny().add(((org.w3c.dom.Document) res.getNode()).getDocumentElement());
         }
         catch (JAXBException je)
         {
            trace.error("Failed marshalling DMS element.", je);
         }
      }

      ret.setXml(xml);

      return ret;
   }

   public static ParametersXto marshalInDataValues(Model model, Activity activity,
         String contextId, Map<String, ? extends Serializable> params, ModelResolver resolver)
   {
      ApplicationContext context = null;
      if (activity != null)
      {
         context = activity.getApplicationContext(contextId == null
               ? PredefinedConstants.DEFAULT_CONTEXT
               : contextId);
      }
      return marshalInDataValues(model, context, params, resolver);
   }

   public static ParametersXto marshalInDataValues(Model model,
         ApplicationContext context, Map<String, ? extends Serializable> params, ModelResolver resolver)
   {
      ParametersXto res = new ParametersXto();

      marshalInDataValues(model, context, params, res, resolver);

      return res;
   }

   public static void marshalInDataValues(Model model, ApplicationContext context,
         Map<String, ? extends Serializable> params, ParametersXto res, ModelResolver resolver)
   {
      if ((null != context) && (null != model) && (null != params))
      {
         for (Entry<String, ? extends Serializable> entry : params.entrySet())
         {
            // if the context of an external web app is used, it is required to iterate
            // through all data mappings in order to find the correct Aceespoint
            if (PredefinedConstants.EXTERNALWEBAPP_CONTEXT == context.getId())
            {
               @SuppressWarnings("unchecked")
               List<DataMappingDetails> mappings = context.getAllInDataMappings();
               for (DataMappingDetails mapping : mappings)
               {
                  if (CompareHelper.areEqual(mapping.getApplicationAccessPoint().getId(), entry.getKey()))
                  {
                     DataMapping dm = context.getDataMapping(Direction.IN, mapping.getId());
                     ParameterXto inDataValue = marshalInDataValue(model, dm, entry.getValue(), resolver);
                     if (inDataValue != null)
                     {
                        res.getParameter().add(inDataValue);
                     }
                     else
                     {
                        trace.warn("Marshaling of InDataValue (" + dm.getId()
                              + ") failed. Property ignored.");
                     }
                     break;
                  }
               }

            }
            // Otherwise treat as before
            else
            {
               DataMapping dm = context.getDataMapping(Direction.IN, entry.getKey());
               ParameterXto inDataValue = marshalInDataValue(model, dm, entry.getValue(), resolver);
               if (inDataValue != null)
               {
                  res.getParameter().add(inDataValue);
               }
               else
               {
                  trace.warn("Marshaling of InDataValue (" + dm.getId()
                        + ") failed. Property ignored.");
               }

            }

         }
      }
   }

   public static ParameterXto marshalInDataValue(Model model, DataMapping dm,
         Serializable param, ModelResolver resolver)
   {
      return marshalInDataValue(model, model.getData(dm.getDataId()), dm.getId(),
            dm.getDataPath(), dm.getMappedType(), param, resolver);
   }

   public static ParameterXto marshalInDataValue(Model model, DataPath dp,
         Serializable param, ModelResolver resolver)
   {
      return marshalInDataValue(model, model.getData(dp.getData()), dp.getId(),
            dp.getAccessPath(), dp.getMappedType(), param, resolver);
   }

   private static ParameterXto marshalInDataValue(Model model, Data data, String paramId,
         String rootXPath, Class<?> mappedType, Serializable param, ModelResolver resolver)
   {
      ParameterXto res = null;

      if ((null != data) && (null != model))
      {
         if (isPrimitiveType(model, data))
         {
            res = marshalPrimitiveValue(data, paramId, mappedType, param);
         }
         else if (isStructuredType(model, data))
         {
            res = marshalStructValue(model, data, paramId, rootXPath, param, resolver);
         }
         else if (isDmsType(model, data))
         {
            res = marshalDmsValue(model, data, paramId, param, resolver);
         }
         else if (isSerializableType(model, data))
         {
            res = marshalSerializableValue(paramId, mappedType, param);
         }
         else if (isEntityBeanType(model, data))
         {
            res = marshalEntityBeanValue(paramId, mappedType, param);
         }
         else
         {
            trace.error("Marshaling of InDataValue ('" + data.getId()
                  + "') failed. Type not supported.");
            // TODO review: unsupported InDataType marshaled with null value
            // throw new UnsupportedOperationException("Marshaling of InDataValue ("
            // + data.getId() + ") failed. Type not supported");
         }
      }
      return res;
   }

   private static ParameterXto marshalEntityBeanValue(String paramId,
         Class< ? > mappedType, Serializable param)
   {
      ParameterXto ret = null;

      if (mappedType != null)
      {
         ret = marshalSerializableAsPrimitive(paramId, mappedType, param);

         if (ret.getType() == null)
         {
            trace.warn("Could not marshal entityType value for (" + paramId
                  + "). MappedType not supported: " + mappedType);
            ret = null;
         }

      }
      else
      {
         trace.warn("Could not marshal entityType value for (" + paramId
               + "). Empty MappedType not supported");
      }
      return ret;
   }

   private static ParameterXto marshalSerializableValue(String paramId,
         Class< ? > mappedType, Serializable param)
   {
      ParameterXto ret;

      ret = marshalSerializableAsPrimitive(paramId, mappedType, param);
      if (ret.getType() == null)
      {
         ret = marshalSerializableAsBase64(paramId, param);
      }
      return ret;
   }

   private static ParameterXto marshalSerializableAsPrimitive(String paramId,
         Class< ? > mappedType, Serializable param)
   {
      ParameterXto ret = new ParameterXto();

      ret.setName(paramId);

      ret.setType(marshalSimpleTypeXsdType(mappedType));

      ret.setPrimitive(marshalSimpleTypeXsdValue(param));

      return ret;
   }

   private static ParameterXto marshalSerializableAsBase64(String paramId,
         Serializable param)
   {
      ParameterXto ret = new ParameterXto();
      ret.setName(paramId);
      ret.setType(QNameConstants.QN_BASE64BINARY);
      ret.setPrimitive(marshalBase64EncodedSerializable(param));
      return ret;
   }

   public static String marshalInDataValueAsJson(Model model, DataMapping dm,
         Serializable param)
   {
      String res = null;

      if ((null != dm) && (null != model) && (null != param))
      {
         Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
               .create();

         if (isPrimitiveType(model, dm))
         {
            res = gson.toJson(param);
         }
         else if (isStructuredType(model, dm))
         {
            if ((param instanceof Map) || (param instanceof List))
            {
               res = gson.toJson(param);
            }
         }
         else if (isDmsType(model, dm))
         {
            // TODO support DMS types?
         }
      }

      return res;
   }

   public static Map<String, ? extends Serializable> unmarshalInitialDataValues(
         String processID, ParametersXto params, WebServiceEnv wsEnv)
   {
      Model model = wsEnv.getActiveModel();
      QName qName = QName.valueOf(processID);
      if (!XMLConstants.NULL_NS_URI.equals(qName.getNamespaceURI()))
      {
         model = wsEnv.getActiveModel(qName.getNamespaceURI());
         processID = qName.getLocalPart();
      }
      return unmarshalInitialDataValues(processID, params, model, wsEnv);
   }

   public static Map<String, ? extends Serializable> unmarshalInitialDataValues(
         String processID, ParametersXto params, Model model, ModelResolver resolver)
   {
      Map<String, Serializable> res = null;
      if ((null != params) && !params.getParameter().isEmpty())
      {
         if (null != model)
         {
            res = new HashMap<String, Serializable>();

            // iterate over parameters and unmarshal each
            for (int i = 0; i < params.getParameter().size(); ++i)
            {
               ParameterXto param = params.getParameter().get(i);

               Data data = model.getData(param.getName());
               if (data != null)
               {
                  res.put(param.getName(),
                        unmarshalDataValue(model, data, null, null, param, resolver));
               }
               else
               {
                  res.put(param.getName(), null);
               }
            }
         }
      }

      return res;
   }

   public static Map<String, Serializable> unmarshalDataValues(Model model,
         Direction direction, Activity activity, String contextId, ParametersXto params, ModelResolver resolver)
   {
      Map<String, Serializable> res = null;
      if ((null != params) && !params.getParameter().isEmpty())
      {
         ApplicationContext context = null;
         if (activity != null)
         {
            context = activity.getApplicationContext(contextId == null
                  ? PredefinedConstants.DEFAULT_CONTEXT
                  : contextId);
         }

         if (null != context)
         {
            res = new HashMap<String, Serializable>();

            // iterate over parameters and unmarshal each
            for (int i = 0; i < params.getParameter().size(); ++i)
            {
               ParameterXto param = params.getParameter().get(i);

               DataMapping dm = context.getDataMapping(direction, param.getName());
               if (null != dm)
               {
                  Data data = model.getData(dm.getDataId());

                  if (data != null)
                  {
                     res.put(
                           param.getName(),
                           unmarshalDataValue(model, data, dm.getDataPath(),
                                 dm.getMappedType(), param, resolver));
                  }
                  else
                  {
                     throw new NullPointerException("Data not found in model for id: "
                           + param.getName());
                  }
               }
            }
         }
      }

      return res;
   }



   public static Map<String, ? extends Serializable> unmarshalProcessInstanceProperties(
         long processInstanceOid, List< ? extends ParameterXto> params)
   {
      WebServiceEnv wsEnv = currentWebServiceEnvironment();
      ServiceFactory sf = wsEnv.getServiceFactory();
      WorkflowService wfs = sf.getWorkflowService();
      ProcessInstance pi = wfs.getProcessInstance(processInstanceOid);

      Map<String, Serializable> res = null;
      if ((null != params) && !params.isEmpty())
      {
         res = new HashMap<String, Serializable>();

         boolean noteAdded = false;
         ProcessInstanceAttributes attributes = null;

         // iterate over parameters and unmarshal each
         for (ParameterXto param : params)
         {
            if (param.getName() == null)
            {
               // ignore value
            }
            else if ( !NOTES_NAMESPACE.equals(new QName(param.getNamespace(),
                  param.getName())))
            {
               Model model = wsEnv.getModel(
                     pi.getModelOID());
               // unmarshal DataValues
               res.put(
                     param.getName(),
                     unmarshalProcessDataValue(pi.getProcessID(),
                           ((ProcessInstanceDetails) pi).getDescriptorDefinitions(),
                           model, param, wsEnv));
            }
            else
            {
               // unmarshal Notes
               if (param.getXml() == null)
               {
                  if (attributes == null)
                  {
                     attributes = getScopePi(pi, wfs).getAttributes();
                  }
                  attributes.addNote(param.getPrimitive());
                  noteAdded = true;
               }
            }
         }
         if (noteAdded)
         {
            wfs.setProcessInstanceAttributes(attributes);
         }
      }

      return res;
   }

   public static Serializable unmarshalProcessDataValue(String processId,
         List<DataPath> descriptorDefinitions, Model model, ParameterXto param, ModelResolver resolver)
   {
      Serializable res = null;

      ProcessDefinition process = model.getProcessDefinition(processId);
      DataPath dp = process.getDataPath(param.getName());

      if (null != dp)
      {
         Data data = model.getData(dp.getData());
         if (data != null)
         {
            res = unmarshalDataValue(model, data, dp.getAccessPath(), dp.getMappedType(),
                  param, resolver);
         }
         else
         {
            throw new NullPointerException("Data not found in model for id: "
                  + param.getName());
         }
      }
      else
      {
         // is not found in model unmarshaling as primitive (case descriptor)
         res = unmarshalPrimitiveValue(param.getType(), param.getPrimitive());
      }
      return res;
   }

   public static Serializable unmarshalOutDataValue(Model model, AccessPoint ap,
         Reference ref, Object param, ModelResolver resolver)
   {
      Serializable res = null;

      if ((null != ap) && (null != model) && (null != param))
      {
         if (isPrimitiveType(model, ap))
         {
            res = unmarshalPrimitiveValue(model, ap, (String) param);
         }
         else if (isStructuredType(model, ap))
         {
            res = unmarshalStructValue(model, ap, ref, param, resolver);
         }
         else
         {
            trace.warn("Access point of unsupported type: " + ap.getId());
            throw new WebApplicationException(Status.BAD_REQUEST);
         }
      }

      return res;
   }

   public static Serializable unmarshalOutDataValue(Model model, DataMapping dm,
         Object param, ModelResolver resolver)
   {
      Serializable res = null;

      if ((null != dm) && (null != model) && (null != param))
      {
         if (isPrimitiveType(model, dm))
         {
            res = unmarshalPrimitiveValue(model, dm, (String) param);
         }
         else if (isStructuredType(model, dm))
         {
            res = unmarshalStructValue(model, dm, param, resolver);
         }
         else if (isDmsType(model, dm))
         {
            res = unmarshalDmsValue(model, dm, param, resolver);
         }
      }

      return res;
   }

   public static Serializable unmarshalOutDataValue(Model model, DataMapping dm,
         ParameterXto param, ModelResolver resolver)
   {
      Data data = model.getData(dm.getDataId());
      return unmarshalDataValue(model, data, dm.getDataPath(), dm.getMappedType(), param, resolver);
   }


   public static Serializable unmarshalBusinessObjectDataValue(Model model, String qualifiedBusinessObjectId, ParameterXto param)
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      String modelId = qname.getNamespaceURI();
      String businessObjectId = qname.getLocalPart();

      ModelResolver env = currentWebServiceEnvironment();

      if (model == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL_WITH_ID.raise(modelId));
      }

      Data data = model.getData(businessObjectId);
      if (data == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(businessObjectId));
      }

      return unmarshalDataValue(model, data, null, null, param, env);
   }

   public static Serializable unmarshalDataValue(Model model, Data data,
         String rootXPath, Class< ? > mappedType, ParameterXto param, ModelResolver resolver)
   {
      Serializable unmarshalledValue = null;

      // Workaround for WsAdapter to transfer Serializable as Base64encodedBinary
      if (QNameConstants.QN_BASE64BINARY.equals(param.getType())
            && QNameConstants.QN_BASE64BINARY.getLocalPart().equals(param.getNamespace()))
      {
         unmarshalledValue = unmarshalBase64EncodedSerializable(param.getPrimitive());
      }
      else if (data == null)
      {
         trace.warn("Unmarshaling of DataValue (" + param.getName()
               + ") failed. Data not found in Model");
         // throw new UnsupportedOperationException("Unmarshaling of DataValue ("
         // + param.getName() + ") failed. Data not found in Model");
      }
      else if (isPrimitiveType(model, data))
      {
         validatePrimitiveType(data, param.getType());

         // primitive value may be null and gets unmarshaled as null
         unmarshalledValue = unmarshalPrimitiveValue(model, data, mappedType,
               param.getPrimitive());
      }
      else if (isStructuredType(model, data))
      {
         if (null != param.getXml())
         {
            unmarshalledValue = unmarshalStructValue(model, data, rootXPath,
                  param.getXml().getAny(), resolver);
         }
         // allow primitive==null if type points to a primitive struct data
         else if (null != param.getType())
         {
            unmarshalledValue = unmarshalStructValue(model, data, rootXPath,
                  param.getPrimitive(), resolver);
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Cannot unmarshal DataValues: no structured value available for "
                        + param.getName());
            // no structured value available
         }
      }
      else if (isDmsType(model, data))
      {
         if (null != param.getXml())
         {
            unmarshalledValue = unmarshalDmsValue(model, data, param.getXml().getAny(), resolver);
         }
         else
         {
            trace.warn("Cannot unmarshal DataValues: no DMS value available");
            // no DMS value available
         }
      }
      else if (isSerializableType(model, data))
      {
         unmarshalledValue = unmarshalSerializableValue(model, data, param);
      }
      else if (isEntityBeanType(model, data))
      {
         unmarshalledValue = unmarshalSerializableValue(model, data, param);
      }
      else
      {
         trace.error("Unmarshaling of DataValue (" + param.getName()
               + ") failed. Type not supported");
         throw new UnsupportedOperationException("Unmarshaling of DataValue ("
               + param.getName() + ") failed. Type not supported");
      }

      return unmarshalledValue;
   }

   private static void validatePrimitiveType(Data data, QName type)
   {
      Type typeAttribute = (Type) data.getAttribute(TYPE_ATT);

      if (type == null && Type.String.equals(typeAttribute) || Type.Enumeration.equals(typeAttribute))
      {
         // default to string
         return;
      }
      else if (typeAttribute == Type.Calendar
            && marshalPrimitiveType(typeAttribute).equals(QNameConstants.QN_BASE64BINARY))
      {
         // handle Calendar to Binary
         return;
      }
      else if ( !marshalPrimitiveType(typeAttribute).equals(type))
      {
         throw new InvalidValueException(
               BpmRuntimeError.BPMRT_INCOMPATIBLE_TYPE_FOR_DATA.raise(data.getId()));
      }
   }

   private static Serializable unmarshalSerializableValue(Model model, Data data,
         ParameterXto param)
   {
      Serializable ret = null;
      if (QNameConstants.QN_BASE64BINARY.equals(param.getType()))
      {
         try
         {
            ret = Serialization.deserializeObject(Base64.decode(param.getPrimitive()
                  .getBytes()));
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (ClassNotFoundException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }
      else
      {
         ret = unmarshalSimpleTypeXsdValue(param);
      }
      return ret;
   }

   public static Serializable unmarshalSimpleTypeXsdValue(ParameterXto param)
   {
      QName targetType = param.getType();
      String value = param.getPrimitive();

      if (null == targetType || QNameConstants.QN_STRING.equals(targetType)
            || QNameConstants.QN_ENUMERATION.equals(targetType))
      {
         return parseString(value);
      }
      else if (QNameConstants.QN_LONG.equals(targetType))
      {
         return parseLong(value);
      }
      else if (QNameConstants.QN_INT.equals(targetType))
      {
         return parseInt(value);
      }
      else if (QNameConstants.QN_SHORT.equals(targetType))
      {
         return parseShort(value);
      }
      else if (QNameConstants.QN_BYTE.equals(targetType))
      {
         return parseByte(value);
      }
      else if (QNameConstants.QN_DOUBLE.equals(targetType))
      {
         return parseDouble(value);
      }
      else if (QNameConstants.QN_FLOAT.equals(targetType))
      {
         return parseFloat(value);
      }
      else if (QNameConstants.QN_BOOLEAN.equals(targetType))
      {
         return parseBoolean(value);
      }
      else if (QNameConstants.QN_DATETIME.equals(targetType))
      {
         Calendar cal = parseDateTime(value);
         return cal.getTime();
      }
      throw new UnsupportedOperationException(
            "Error unmarshaling SerializableData: Type not supported: " + targetType);
   }

   private static Serializable unmarshalBase64EncodedSerializable(String primitive)
   {
      Serializable ret = null;
      if ( !isEmpty(primitive))
      {
         try
         {
            ret = Serialization.deserializeObject(Base64.decode(primitive.getBytes()));
         }
         catch (IOException e)
         {
            trace.warn(e.getMessage(), e);
         }
         catch (ClassNotFoundException e)
         {
            trace.warn(e.getMessage(), e);
         }
      }
      return ret;
   }

   private static String marshalBase64EncodedSerializable(Serializable value)
   {
      String ret = null;
      try
      {
         ret = new String(Base64.encode(Serialization.serializeObject(value)));
      }
      catch (IOException e)
      {
         trace.warn(e.getMessage(), e);
      }
      return ret;
   }

   @SuppressWarnings("deprecation")
   public static QName marshalPrimitiveType(Type type)
   {
      QName result = null;

      if (Type.String == type)
      {
         result = QNameConstants.QN_STRING;
      }
      else if (Type.Long == type)
      {
         result = QNameConstants.QN_LONG;
      }
      else if (Type.Integer == type)
      {
         result = QNameConstants.QN_INT;
      }
      else if (Type.Short == type)
      {
         result = QNameConstants.QN_SHORT;
      }
      else if (Type.Byte == type)
      {
         result = QNameConstants.QN_BYTE;
      }
      else if (Type.Double == type)
      {
         result = QNameConstants.QN_DOUBLE;
      }
      else if (Type.Float == type)
      {
         result = QNameConstants.QN_FLOAT;
      }
      else if (Type.Boolean == type)
      {
         result = QNameConstants.QN_BOOLEAN;
      }
      else if (Type.Timestamp == type)
      {
         result = QNameConstants.QN_DATETIME;
      }
      else if (Type.Calendar == type || Type.Money == type)
      {
         result = QNameConstants.QN_BASE64BINARY;
      }
      else if (Type.Char == type)
      {
         result = QNameConstants.QN_CHAR;
      }
      else if (Type.Enumeration == type)
      {
         result = QNameConstants.QN_ENUMERATION;
      }
      else
      {
         trace.warn("Error marshaling primitive Type: Unsupported primitive type: "
               + type);
      }

      return result;
   }

   public static Class< ? > getMappedType(QName type)
   {
      if ((null == type) || (QNameConstants.QN_STRING.equals(type)))
      {
         return String.class;
      }
      else if (QNameConstants.QN_LONG.equals(type))
      {
         return Long.class;
      }
      else if (QNameConstants.QN_INT.equals(type))
      {
         return Integer.class;
      }
      else if (QNameConstants.QN_SHORT.equals(type))
      {
         return Short.class;
      }
      else if (QNameConstants.QN_BYTE.equals(type))
      {
         return Byte.class;
      }
      else if (QNameConstants.QN_DOUBLE.equals(type))
      {
         return Double.class;
      }
      else if (QNameConstants.QN_FLOAT.equals(type))
      {
         return Float.class;
      }
      else if (QNameConstants.QN_BOOLEAN.equals(type))
      {
         return Boolean.class;
      }
      else if (QNameConstants.QN_DATETIME.equals(type))
      {
         return Date.class;
      }
      else if (QNameConstants.QN_CHAR.equals(type))
      {
         return Character.class;
      }
      else if (QNameConstants.QN_BASE64BINARY.equals(type))
      {
         return Calendar.class;
      }
      else
      {
         trace.warn("Unsupported primitive type code " + type);

         return Object.class;
      }
   }

   public static Type unmarshalPrimitiveType(QName type)
   {
      if ((null == type) || (QNameConstants.QN_STRING.equals(type)))
      {
         return Type.String;
      }
      else if (QNameConstants.QN_LONG.equals(type))
      {
         return Type.Long;
      }
      else if (QNameConstants.QN_INT.equals(type))
      {
         return Type.Integer;
      }
      else if (QNameConstants.QN_SHORT.equals(type))
      {
         return Type.Short;
      }
      else if (QNameConstants.QN_BYTE.equals(type))
      {
         return Type.Byte;
      }
      else if (QNameConstants.QN_DOUBLE.equals(type))
      {
         return Type.Double;
      }
      else if (QNameConstants.QN_FLOAT.equals(type))
      {
         return Type.Float;
      }
      else if (QNameConstants.QN_BOOLEAN.equals(type))
      {
         return Type.Boolean;
      }
      else if (QNameConstants.QN_DATETIME.equals(type))
      {
         return Type.Timestamp;
      }
      else if (QNameConstants.QN_CHAR.equals(type))
      {
         return Type.Char;
      }
      else if (QNameConstants.QN_ENUMERATION.equals(type))
      {
         return Type.Enumeration;
      }
      else if (QNameConstants.QN_BASE64BINARY.equals(type))
      {
         return Type.Calendar;
      }
      else
      {
         trace.warn("Unsupported primitive type code " + type);

         return null;
      }
   }

   public static ParameterXto marshalPrimitiveValue(Model model, AccessPoint ap,
         Serializable value)
   {
      ParameterXto param = new ParameterXto();
      param.setName(ap.getId());

      Type primitiveType = (Type) ap.getAttribute(TYPE_ATT);
      QName type = marshalPrimitiveType(primitiveType);

      param.setType(type);
      param.setPrimitive(marshalSimpleTypeXsdValue(value));

      return param;
   }

   public static ParameterXto marshalPrimitiveValue(Model model, DataPath dp,
         Serializable value)
   {
      return marshalPrimitiveValue(model.getData(dp.getData()), dp.getId(),
            dp.getMappedType(), value);
   }

   public static ParameterXto marshalPrimitiveValue(Model model, DataMapping dm,
         Serializable value)
   {
      return marshalPrimitiveValue(model.getData(dm.getDataId()), dm.getId(),
            dm.getMappedType(), value);
   }

   public static ParameterXto marshalPrimitiveValue(Data data, String id,
         Class< ? > mappedType, Serializable value)
   {
      ParameterXto param = new ParameterXto();
      param.setName(id);

      QName type = null;
      if (mappedType == null)
      {
         Type primitiveType = (Type) data.getAttribute(TYPE_ATT);
         type = marshalPrimitiveType(primitiveType);
      }
      else
      {
         type = marshalSimpleTypeXsdType(mappedType);
      }
      if (type != null)
      {
         param.setType(type);
      }
      else
      {
         throw new UnsupportedOperationException(
               "Marshaling not supported for primitiveType id: " + id + " (mappedType="
                     + mappedType + ")");
      }
      param.setPrimitive(marshalPrimitiveValue(value));
      return param;
   }

   public static String marshalPrimitiveValue(Serializable value)
   {
      String ret = null;
      if (null != value)
      {
         // handle simpleType
         ret = marshalSimpleTypeXsdValue(value);

         if (value instanceof Character)
         {
            // param.setPrimitive(printString(((Character) value).toString()));

            ret = new String(Base64.encode((((Character) value).toString().getBytes())));
         }
         else if (value instanceof Calendar || value instanceof Money)
         {
            try
            {
               ret = new String(Base64.encode(Serialization.serializeObject(value)));
            }
            catch (IOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         }

         // if (param.getType() == null)
         // {
         // throw new UnsupportedOperationException(
         // "Marshaling not supported for primitiveType: " + primitiveType.getName()
         // + " ValueClass: " + value.getClass());
         // }
      }
      return ret;
   }

   public static String marshalSimpleTypeXsdValue(Serializable value)
   {
      String ret = null;
      if (value instanceof String)
      {
         ret = printString((String) value);
      }
      else if (value instanceof Long)
      {
         ret = printLong((Long) value);
      }
      else if (value instanceof Integer)
      {
         ret = printInt((Integer) value);
      }
      else if (value instanceof Short)
      {
         ret = printShort((Short) value);
      }
      else if (value instanceof Byte)
      {
         ret = printByte((Byte) value);
      }
      else if (value instanceof Double)
      {
         ret = printDouble((Double) value);
      }
      else if (value instanceof Float)
      {
         ret = printFloat((Float) value);
      }
      else if (value instanceof Boolean)
      {
         ret = printBoolean((Boolean) value);
      }
      else if (value instanceof Date)
      {
         Calendar cal = Calendar.getInstance();
         cal.setTime((Date) value);
         ret = printDateTime(cal);
      }
      else if (value instanceof Enum<?>)
      {
         ret = printString(((Enum<?>) value).name());
      }
      return ret;
   }

   public static QName marshalSimpleTypeXsdType(Class< ? > value)
   {
      QName ret = null;
      if (String.class.equals(value))
      {
         ret = QNameConstants.QN_STRING;
      }
      else if (Long.class.equals(value))
      {
         ret = QNameConstants.QN_LONG;
      }
      else if (Integer.class.equals(value))
      {
         ret = QNameConstants.QN_INT;
      }
      else if (Short.class.equals(value))
      {
         ret = QNameConstants.QN_SHORT;
      }
      else if (Byte.class.equals(value))
      {
         ret = QNameConstants.QN_BYTE;
      }
      else if (Double.class.equals(value))
      {
         ret = QNameConstants.QN_DOUBLE;
      }
      else if (Float.class.equals(value))
      {
         ret = QNameConstants.QN_FLOAT;
      }
      else if (Boolean.class.equals(value))
      {
         ret = QNameConstants.QN_BOOLEAN;
      }
      else if (Date.class.equals(value))
      {
         ret = QNameConstants.QN_DATETIME;
      }
      else if (Calendar.class.equals(value) || Money.class.equals(value))
      {
         ret = QNameConstants.QN_BASE64BINARY;
      }
      else if (Character.class.equals(value))
      {
         ret = QNameConstants.QN_CHAR;
      }
      else if (value != null && value.getClass().isEnum())
      {
         ret = QNameConstants.QN_ENUMERATION;
      }
      return ret;
   }

   public static Serializable unmarshalPrimitiveValue(Model model, AccessPoint ap,
         String value)
   {
      Serializable result = null;

      Type primitveType = (Type) ap.getAttribute(TYPE_ATT);
      result = unmarshalPrimitiveValue(primitveType, value);

      return result;
   }

   public static Serializable unmarshalPrimitiveValue(Model model, Data data,
         Class< ? > mappedType, String value)
   {
      Serializable result = null;

      if (mappedType == null)
      {
         Type primitiveType = (Type) data.getAttribute(TYPE_ATT);

         result = unmarshalPrimitiveValue(primitiveType, value);
      }
      else
      {
         // check on internal type (mappedType) instead of external QName
         result = unmarshalPrimitiveValue(marshalSimpleTypeXsdType(mappedType), value);
      }

      return result;
   }

   public static Serializable unmarshalPrimitiveValue(Model model, DataMapping dm,
         String value)
   {
      Data data = model.getData(dm.getDataId());

      return unmarshalPrimitiveValue(model, data, dm.getMappedType(), value);
   }

   public static Serializable unmarshalPrimitiveValue(Model model, DataPath dp,
         String value)
   {
      Data data = model.getData(dp.getData());

      return unmarshalPrimitiveValue(model, data, dp.getMappedType(), value);
   }

   public static Serializable unmarshalPrimitiveValue(QName type, String value)
   {
      Type targetType = unmarshalPrimitiveType(type);

      return unmarshalPrimitiveValue(targetType, value);
   }

   public static Serializable unmarshalPrimitiveValue(Type targetType, String value)
   {
      if ((null == targetType) || (Type.String == targetType) || (Type.Enumeration == targetType))
      {
         return parseString(value);
      }
      else if (Type.Long == targetType)
      {
         return parseLong(value);
      }
      else if (Type.Integer == targetType)
      {
         return parseInt(value);
      }
      else if (Type.Short == targetType)
      {
         return parseShort(value);
      }
      else if (Type.Byte == targetType)
      {
         return parseByte(value);
      }
      else if (Type.Double == targetType)
      {
         return parseDouble(value);
      }
      else if (Type.Float == targetType)
      {
         return parseFloat(value);
      }
      else if (Type.Boolean == targetType)
      {
         return parseBoolean(value);
      }
      else if (Type.Timestamp == targetType)
      {
         if (StringUtils.isNotEmpty(value))
         {
            Calendar cal = parseDateTime(value);
            return cal.getTime();
         }
         else
         {
            return null;
         }
      }
      else if (Type.Char == targetType)
      {
         // return value == null ? null : Character.valueOf(parseString(value).charAt(0));
         return value == null ? null : Character.valueOf(new String(
               Base64.decode(value.getBytes())).charAt(0));
      }
      else if (Type.Calendar == targetType)
      {
         Serializable ret = null;
         if (value != null)
         {
            ret = deserialize(Base64.decode(value.getBytes()));
         }
         return ret;
      }
      else
      {
         trace.warn("Ignoring primitive type code " + targetType);
      }
      return value;
   }

   private static Serializable deserialize(byte[] decode)
   {
      Serializable ret = null;

      try
      {
         ret = Serialization.deserializeObject(decode);
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         trace.error(e);
      }
      catch (ClassNotFoundException e)
      {
         // TODO Auto-generated catch block
         trace.error(e);
      }
      return ret;
   }

   public static QName getStructuredTypeName(Model model, String typeDeclarationId, ModelResolver resolver)
   {
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, typeDeclarationId, resolver);
      return getStructuredTypeName(pair.getFirst(), pair.getSecond(), null);
   }

   public static QName getStructuredTypeName(Model model, DataPath dp, ModelResolver resolver)
   {
      Data data = model.getData(dp.getData());
      return getStructuredTypeName(model, data, dp.getAccessPath(), resolver);
   }

   public static QName getStructuredTypeName(Model model, Data data, String accessPath,
         ModelResolver resolver)
   {
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, data, resolver);
      return getStructuredTypeName(pair.getFirst(), pair.getSecond(), accessPath);
   }

   public static QName getStructuredTypeName(Model model, AccessPoint ap, Reference ref,
         String accessPath, ModelResolver resolver)
   {
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, ap, ref, resolver);
      return getStructuredTypeName(pair.getFirst(), pair.getSecond(), accessPath);
   }

   public static Set<TypedXPath> getXPaths(Model model, DataMapping dm)
   {
      Data data = model.getData(dm.getDataId());
      String typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      return getXPaths(model, typeDeclarationId, dm.getDataPath());
   }

   public static Set<TypedXPath> getXPaths(Model model, String typeDeclarationId)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      return StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
   }

   public static Set<TypedXPath> getXPaths(Model model, String typeDeclarationId,
         String derefPath)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(model,
            typeDeclaration);
      if (StringUtils.isEmpty(derefPath))
      {
         return allXPaths;
      }
      Set<TypedXPath> xpaths = CollectionUtils.newSet();
      for (TypedXPath xpath : allXPaths)
      {
         String path = xpath.getXPath();
         if (path.equals(derefPath))
         {
            addChildren(xpaths, null, xpath, derefPath);
            break;
         }
      }
      return xpaths;
   }

   private static void addChildren(Set<TypedXPath> xpaths, TypedXPath newParent,
         TypedXPath xpath, String derefPath)
   {
      String newPath = xpath.getXPath().substring(derefPath.length());
      if (newPath.startsWith("/"))
      {
         newPath = newPath.substring(1);
      }
      TypedXPath newXPath = new TypedXPath(newParent, xpath.getOrderKey(), newPath,
            xpath.isAttribute(), xpath.getXsdElementName(), xpath.getXsdElementNs(),
            xpath.getXsdTypeName(), xpath.getXsdTypeNs(), xpath.getType(),
            xpath.isList(), xpath.getAnnotations(), xpath.getEnumerationValues());
      xpaths.add(newXPath);
      for (TypedXPath childXPath : xpath.getChildXPaths())
      {
         addChildren(xpaths, newXPath, childXPath, derefPath);
      }
   }

   public static QName getStructuredTypeName(Model model, DataMapping dm, ModelResolver resolver)
   {
      Data data = model.getData(dm.getDataId());
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, data, resolver);
      return getStructuredTypeName(pair.getFirst(), pair.getSecond(), dm.getDataPath());
   }

   private static QName getStructuredTypeName(Model model, TypeDeclaration typeDeclaration,
         String derefPath)
   {
      Set<TypedXPath> xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
      return getStructuredTypeName(xPaths, derefPath);
   }

   public static QName getStructuredTypeName(Set<TypedXPath> xPaths, String derefPath)
   {
      IXPathMap xPathMap = new ClientXPathMap(xPaths);

      TypedXPath xPath = null;

      if (derefPath == null)
      {
         xPath = xPathMap.getRootXPath();
         if (xPath != null)
         {
            if (!isEmpty(xPath.getXsdElementName()))
            {
               return new QName(xPath.getXsdElementNs(), xPath.getXsdElementName());
            }
            else
            {
               return new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName());
            }
         }
      }
      else
      {
         xPath = xPathMap.getXPath(getXPathWithoutIndexes(derefPath));
         if (xPath != null)
         {
            return new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName());
         }
      }
      return null;
   }

   public static QName getDmsTypeName(Model model, Data data)
   {

      Class< ? > xtoType = null;

      String dataType = getTypeId(data);
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataType))
      {
         // TODO retrieve from annotation on DocumentXto?
         xtoType = DocumentXto.class;
      }
      else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataType))
      {
         // TODO retrieve from annotation on DocumentListXto?
         xtoType = DocumentsXto.class;
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataType))
      {
         // TODO retrieve from annotation on FolderXto?
         xtoType = FolderXto.class;
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataType))
      {
         // TODO retrieve from annotation on FolderListXto?
         xtoType = FoldersXto.class;
      }

      if (null != xtoType)
      {
         return getDmsTypeName(xtoType);
      }

      return null;
   }

   public static QName getDmsTypeName(Model model, DataMapping dm)
   {
      Data data = model.getData(dm.getDataId());

      return getDmsTypeName(model, data);
   }

   public static QName getDmsTypeName(Model model, DataPath dp)
   {
      Data data = model.getData(dp.getData());

      return getDmsTypeName(model, data);
   }

   public static QName getDmsTypeName(Class< ? > xtoType)
   {
      if (null != xtoType)
      {
         XmlType xmlTypeAnnotation = xtoType.getAnnotation(XmlType.class);

         String namespace = xmlTypeAnnotation.namespace();
         if (isEmpty(namespace))
         {
            XmlSchema schemaAnnotation = xtoType.getPackage().getAnnotation(
                  XmlSchema.class);
            if (null != schemaAnnotation)
            {
               namespace = schemaAnnotation.namespace();
            }
         }

         return new QName(namespace, xmlTypeAnnotation.name());
      }

      return null;
   }

   public static ParameterXto marshalStructValue(Model model, AccessPoint ap,
         Reference ref, Serializable value, ModelResolver resolver)
   {
      ParameterXto param = new ParameterXto();
      param.setName(ap.getId());

      // TODO remove workaround: retrieve structuredData Map from Array
      if (value instanceof List && ((List< ? >) value).size() == 1
            && ((List< ? >) value).get(0) instanceof Map)
      {
         value = (Serializable) ((List< ? >) value).get(0);
      }

      // Map in case of Structure and String in case of Structure As Enum
      if (value instanceof Map || value instanceof String)
      {
         Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, ap, ref, resolver);
         marshalStructValue(pair.getFirst(), pair.getSecond(), null, value, param);
      }
      else if (null != value)
      {
         trace.debug("Unsupported type " + value.getClass() + " for " + ap.getId());
      }

      return param;
   }

   public static ParameterXto marshalStructValue(Model model, DataMapping dm,
         Serializable value, ModelResolver resolver)
   {

      return marshalStructValue(model, model.getData(dm.getDataId()), dm.getId(),
            dm.getDataPath(), value, resolver);
   }

   public static ParameterXto marshalStructValue(Model model, DataPath dp,
         Serializable value, ModelResolver resolver)
   {
      return marshalStructValue(model, model.getData(dp.getData()), dp.getId(),
            dp.getAccessPath(), value, resolver);
   }

   public static ParameterXto marshalStructValue(Model model, Data data, String paramId,
         String rootXPath, Serializable value, ModelResolver resolver)
   {
      ParameterXto param = new ParameterXto();
      param.setName(paramId);

      // TODO remove workaround: retrieve structuredData Map from Array
      if (value instanceof List && ((List< ? >) value).size() == 1
            && ((List< ? >) value).get(0) instanceof Map)
      {
         value = (Serializable) ((List< ? >) value).get(0);
      }

      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, data, resolver);
      marshalStructValue(pair.getFirst(), pair.getSecond(), rootXPath, value, param);

      return param;
   }

   private static Pair<Model, TypeDeclaration> resolveTypeDeclaration(Model model,
         AccessPoint accessPoint, Reference reference, ModelResolver resolver)
   {
      String typeDeclarationId = (String) accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      if (resolver != null && reference != null)
      {
         model = resolver.getModel(reference.getModelOid());
      }
      return resolveTypeDeclaration(model, typeDeclarationId, resolver);
   }

   private static Pair<Model, TypeDeclaration> resolveTypeDeclaration(Model model,
         Data data, ModelResolver resolver)
   {
      if (resolver != null && data.getModelOID() != model.getModelOID())
      {
         model = resolver.getModel((long) data.getModelOID());
      }
      String typeDeclarationId = null;
      Reference reference = data.getReference();
      if (resolver != null && reference != null)
      {
         model = resolver.getModel(reference.getModelOid());
         typeDeclarationId = reference.getId();
      }
      else
      {
         typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
      }
      return resolveTypeDeclaration(model, typeDeclarationId, resolver);
   }

   private static Pair<Model, TypeDeclaration> resolveTypeDeclaration(Model model,
         String typeDeclarationId, ModelResolver resolver)
   {
      if (resolver != null && typeDeclarationId != null && typeDeclarationId.startsWith("typeDeclaration:"))
      {
         // For data created in current model, Structured type in different model
         try
         {
            String parts[] = typeDeclarationId.split("\\{")[1].split("\\}");
            typeDeclarationId = parts[1];
            Model referencedModel = resolver.getModel(model.getResolvedModelOid(parts[0]));
            model = referencedModel != null ? referencedModel : model;
         }
         catch (Exception e)
         {
            trace.error("Error occured in Type declaration parsing", e);
         }
      }
      return new Pair<Model, TypeDeclaration>(model, model.getTypeDeclaration(typeDeclarationId));
   }

   public static XmlValueXto marshalStructValue(Model model, String typeDeclarationId,
         String derefPath, Serializable value)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
      if (null != typeDeclaration)
      {
         IXPathMap xPathMap = new ClientXPathMap(StructuredTypeRtUtils.getAllXPaths(
               model, typeDeclaration));

         return marshalStructValue(xPathMap, derefPath, value);
      }
      return null;
   }

   private static void marshalStructValue(Model model, TypeDeclaration typeDeclaration,
         String derefPath, Serializable value, ParameterXto parameterXto)
   {
      if (null != typeDeclaration)
      {
         IXPathMap xPathMap = new ClientXPathMap(StructuredTypeRtUtils.getAllXPaths(
               model, typeDeclaration));

         marshalStructValue(xPathMap, derefPath, value, parameterXto);
      }
   }

   public static XmlValueXto marshalStructValue(IXPathMap xPathMap, String derefPath,
         Serializable value)
   {
      XmlValueXto result = null;
      if (null != value)
      {
         StructuredDataConverter structConverter = new StructuredDataConverter(xPathMap, true);

         Node[] dom = structConverter.toDom(value, derefPath, true);

         result = newXmlValue(dom[0]);
      }
      return result;
   }

   public static void marshalStructValue(IXPathMap xPathMap, String derefPath,
         Serializable value, ParameterXto parameterXto)
   {
      TypedXPath xPath = xPathMap.getXPath(getXPathWithoutIndexes(derefPath));
      if (null != xPath)
      {
         // TODO handle mappedType?
         parameterXto.setType(new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName()));

         if (null != value)
         {
            StructuredDataConverter structConverter = new StructuredDataConverter(
                  xPathMap, true);

            Node[] dom = structConverter.toDom(value, derefPath, true);

            boolean isComplexValue;
            if (BigData.NULL == xPath.getType())
            {
               isComplexValue = true;
            }
            else
            {
               isComplexValue = xPath.isList();

               // see if derefPaths select a single list element
               if (xPath.isList() && !isEmpty(derefPath))
               {
                  String lastPart = getLastXPathPart(derefPath);
                  if ( !derefPath.equals(getXPathWithoutIndexes(lastPart)))
                  {
                     isComplexValue = false;
                  }
               }
               // marshal primitive lists as complex xml
               if (dom.length > 1)
               {
                  isComplexValue = true;
               }
            }

            handleXmlValue(isComplexValue, dom, parameterXto);
         }
      }
   }

   public static Serializable unmarshalStructValue(Model model, AccessPoint ap,
         Reference reference, Object value, ModelResolver resolver)
   {
      if (value instanceof Element)
      {
         Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, ap, reference, resolver);
         return unmarshalStructValue(pair.getFirst(), pair.getSecond(), null, value);
      }
      else if (value instanceof String) // Struct As Enum
      {
         return (String) value;
      }
      else if (value != null)
      {
         trace.debug("Unsupported type " + value.getClass() + " for " + ap.getId());
      }
      return null;
   }

   public static Serializable unmarshalStructValue(Model model, DataMapping dm,
         Object value, ModelResolver resolver)
   {
      Data data = model.getData(dm.getDataId());
      return unmarshalStructValue(model, data, dm.getDataPath(), value, resolver);
   }

   public static Serializable unmarshalStructValue(Model model, DataPath dp,
         Object value, ModelResolver resolver)
   {
      Data data = model.getData(dp.getData());
      return unmarshalStructValue(model, data, dp.getAccessPath(), value, resolver);
   }

   public static Serializable unmarshalStructValue(Model model, Data data,
         String rootXPath, Object value, ModelResolver resolver)
   {
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, data, resolver);
      return unmarshalStructValue(pair.getFirst(), pair.getSecond(), rootXPath, value);
   }

   public static Serializable unmarshalStructValue(Model model, String typeDeclarationId,
         String rootXPath, Object value, ModelResolver resolver)
   {
      Pair<Model, TypeDeclaration> pair = resolveTypeDeclaration(model, typeDeclarationId, resolver);
      return unmarshalStructValue(pair.getFirst(), pair.getSecond(), rootXPath, value);
   }

   public static Serializable unmarshalStructValue(Model model,
         TypeDeclaration typeDeclaration, String rootXPath, Object value)
   {
      if (null != typeDeclaration)
      {
         Set<TypedXPath> xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
         return unmarshalStructValue(xPaths, rootXPath, value);
      }
      return null;
   }

   public static Serializable unmarshalStructValue(Set<TypedXPath> xPaths,
         String rootXPath, Object value)
   {
      Serializable result = null;
      if (value instanceof List)
      {
         value = unwrap((List< ? >) value);
      }

      if (value instanceof Element)
      {
         IXPathMap xPathMap = new ClientXPathMap(xPaths);

         StructuredDataConverter structConverter = new StructuredDataConverter(xPathMap, true);

         String xmlValue = XmlUtils.toString((Element) value);
         org.eclipse.stardust.engine.core.struct.sxml.Document xomDoc;
         try
         {
            xomDoc = DocumentBuilder.buildDocument(new StringReader(xmlValue));

            result = (Serializable) structConverter.toCollection(xomDoc.getRootElement(),
                  rootXPath, true);

            if (result instanceof List
            /* && ((List) result).get(0) instanceof Map */)
            {
               result = (Serializable) unwrap((List<?>) result);
            }
         }
         catch (IOException ioex)
         {
            trace.debug("Failed unmarshalling structured data value.", ioex);
         }

      }
      else if (value instanceof String)
      {
         org.eclipse.stardust.engine.core.struct.sxml.Element element = new org.eclipse.stardust.engine.core.struct.sxml.Element(
               StructuredDataXPathUtils.getLastXPathPart(rootXPath));
         element.appendChild(new Text((String) value));

         IXPathMap xPathMap = new ClientXPathMap(xPaths);

         StructuredDataConverter structConverter = new StructuredDataConverter(xPathMap, true);

         result = (Serializable) structConverter.toCollection(element, rootXPath, true);


      }
      else if (value instanceof List)
      {
         List<Serializable> list = new ArrayList<Serializable>(((List<?>) value).size());
         for (Object element : (List<?>) value)
         {
            list.add(unmarshalStructValue(xPaths, rootXPath, element));
         }
         result = (Serializable) list;
      }
      else if (null != value)
      {
         trace.debug("Unsupported structured data value: " + value.getClass());
         throw new UnsupportedOperationException("Unsupported structured data value: "
               + value.getClass());
      }

      return result;
   }

   public static ParameterXto marshalDmsValue(Model model, DataMapping dm,
         Serializable value, ModelResolver resolver)
   {

      return marshalDmsValue(model, model.getData(dm.getDataId()), dm.getId(), value, resolver);
   }

   public static ParameterXto marshalDmsValue(Model model, DataPath dp, Serializable value, ModelResolver resolver)
   {
      return marshalDmsValue(model, model.getData(dp.getData()), dp.getId(), value, resolver);
   }

   public static ParameterXto marshalDmsValue(Model model, Data data, String paramId,
         Serializable value, ModelResolver resolver)
   {
      ParameterXto param = new ParameterXto();
      param.setName(paramId);

      String metaDataTypeId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);

      JAXBElement< ? > xtoWrapper = null;
      if (value instanceof Document)
      {
         DocumentXto xto = XmlAdapterUtils.toWs((Document) value, model, metaDataTypeId, resolver);
         xtoWrapper = WS_API_OBJECT_FACTORY.createDocument(xto);
      }
      else if (value instanceof Folder)
      {
         FolderXto xto = XmlAdapterUtils.toWs((Folder) value, model, metaDataTypeId, resolver);
         xtoWrapper = WS_API_OBJECT_FACTORY.createFolder(xto);
      }
      else if ((value instanceof List) && isDmsDocumentList(model, data))
      {
         DocumentsXto xto = new DocumentsXto();
         @SuppressWarnings("unchecked")
         List<Document> docList = (List<Document>) value;
         for (Document doc : docList)
         {
            xto.getDocument().add(XmlAdapterUtils.toWs(doc, model, metaDataTypeId, resolver));
         }
         xtoWrapper = WS_API_OBJECT_FACTORY.createDocuments(xto);
      }
      else if ((value instanceof List) && isDmsFolderList(model, data))
      {
         FoldersXto xto = new FoldersXto();
         @SuppressWarnings("unchecked")
         List<Folder> folderList = (List<Folder>) value;
         for (Folder folder : folderList)
         {
            xto.getFolder().add(XmlAdapterUtils.toWs(folder, model, metaDataTypeId, resolver));
         }
         xtoWrapper = WS_API_OBJECT_FACTORY.createFolders(xto);
      }
      else if (null != value)
      {
         trace.warn("Unsupported type " + value.getClass() + " for " + paramId);
      }

      if (null != xtoWrapper)
      {
         param.setType(getDmsTypeName(xtoWrapper.getDeclaredType()));
         param.setXml(marshalDmsXto(xtoWrapper.getValue()));
      }

      return param;
   }

   public static XmlValueXto marshalDmsXto(Object value)
   {
      XmlValueXto param = new XmlValueXto();

      JAXBElement< ? > xtoWrapper = null;
      if (value instanceof DocumentXto)
      {
         xtoWrapper = WS_API_OBJECT_FACTORY.createDocument((DocumentXto) value);
      }
      else if (value instanceof FolderXto)
      {
         xtoWrapper = WS_API_OBJECT_FACTORY.createFolder((FolderXto) value);
      }
      else if (value instanceof DocumentsXto)
      {
         xtoWrapper = WS_API_OBJECT_FACTORY.createDocuments((DocumentsXto) value);
      }
      else if (value instanceof FoldersXto)
      {
         xtoWrapper = WS_API_OBJECT_FACTORY.createFolders((FoldersXto) value);
      }
      else if (null != value)
      {
         trace.warn("Unsupported DMS element: " + value.getClass());
      }

      if (null != xtoWrapper)
      {
         try
         {
            DOMResult res = new DOMResult();
            WS_API_JAXB_CONTEXT.createMarshaller().marshal(xtoWrapper, res);

            param.getAny().add(
                  ((org.w3c.dom.Document) res.getNode()).getDocumentElement());
         }
         catch (JAXBException je)
         {
            trace.error("Failed marshalling DMS element.", je);
         }
      }

      return param;
   }

   public static Serializable unmarshalDmsValue(Model model, Data data, Object value, ModelResolver resolver)
   {
      Serializable result = null;

      // TODO remove workaround (retrieve single element from list)
      if (value instanceof List)
      {
         value = unwrap((List<?>) value);
      }

      if (value instanceof Element)
      {
         String metaDataTypeId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);

         try
         {
            Object xto = WS_API_JAXB_CONTEXT.createUnmarshaller().unmarshal(
                  (Element) value);

            if (xto instanceof JAXBElement< ? >)
            {
               JAXBElement< ? > xtoWrapper = (JAXBElement< ? >) xto;
               if (xtoWrapper.getValue() instanceof DocumentXto)
               {
                  result = fromXto((DocumentXto) xtoWrapper.getValue(), model,
                        metaDataTypeId, new DmsDocumentBean(), resolver);
               }
               else if (xtoWrapper.getValue() instanceof DocumentsXto)
               {
                  ArrayList<Document> docs = newArrayList();

                  DocumentsXto docsXto = (DocumentsXto) xtoWrapper.getValue();
                  for (DocumentXto docXto : docsXto.getDocument())
                  {
                     docs.add(fromXto(docXto, model, metaDataTypeId,
                           new DmsDocumentBean(), resolver));
                  }

                  result = docs;
               }
               else if (xtoWrapper.getValue() instanceof FolderXto)
               {
                  result = fromXto((FolderXto) xtoWrapper.getValue(), model,
                        metaDataTypeId, new DmsFolderBean(), resolver);
               }
               else if (xtoWrapper.getValue() instanceof FoldersXto)
               {
                  ArrayList<Folder> folders = newArrayList();

                  FoldersXto foldersXto = (FoldersXto) xtoWrapper.getValue();
                  for (FolderXto folderXto : foldersXto.getFolder())
                  {
                     folders.add(fromXto(folderXto, model, metaDataTypeId,
                           new DmsFolderBean(), resolver));
                  }

                  result = folders;
               }
            }
         }
         catch (JAXBException je)
         {
            trace.error("Failed unmarshalling DMS element.", je);
         }
      }
      else if (null != value)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Unsupported type " + value.getClass() + " for " + data.getId());
         }
      }

      return result;
   }

   private static Object unwrap(List<?> value)
   {
      return value.size() == 1 ? value.get(0) : value;
   }

   public static Serializable unmarshalDmsValue(Model model, DataMapping dm, Object value, ModelResolver resolver)
   {
      Data data = model.getData(dm.getDataId());

      return unmarshalDmsValue(model, data, value, resolver);
   }

   public static Serializable unmarshalDmsValue(Model model, DataPath dp, Object value, ModelResolver resolver)
   {
      Data data = model.getData(dp.getData());

      return unmarshalDmsValue(model, data, value, resolver);
   }

   public static boolean isPrimitiveType(Model model, AccessPoint ap)
   {
      return (ap.getAttribute(PredefinedConstants.TYPE_ATT) instanceof Type);
   }

   public static boolean isPrimitiveType(Model model, Data data)
   {
      boolean isPrimitive = false;

      if (null != model)
      {
         String dataType = getTypeId(data);
         isPrimitive = PRIMITIVE_DATA.equals(dataType);
      }
      isPrimitive |= data.getAttribute(PredefinedConstants.TYPE_ATT) instanceof Type;

      return isPrimitive;
   }

   public static boolean isPrimitiveType(Model model, DataMapping dm)
   {
      boolean isPrimitive = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isPrimitive = isPrimitiveType(model, data);
      }

      return isPrimitive;
   }

   public static boolean isPrimitiveType(Model model, DataPath dp)
   {
      boolean isPrimitive = false;

      if (null != model)
      {
         Data data = model.getData(dp.getData());

         isPrimitive = isPrimitiveType(model, data);
      }

      return isPrimitive;
   }

   public static boolean isStructuredType(Model model, AccessPoint accessPoint)
   {
      return null != accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
   }

   public static boolean isStructuredType(Model model, Data data)
   {
      boolean isStruct = false;

      String dataType = getTypeId(data);
      isStruct = StructuredTypeRtUtils.isStructuredType(dataType);

      return isStruct;
   }

   public static boolean isStructuredType(Model model, DataMapping dm)
   {
      boolean isStruct = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isStruct = isStructuredType(model, data);
      }

      return isStruct;
   }

   public static boolean isStructuredType(Model model, DataPath dp)
   {
      boolean isStruct = false;

      if (null != model)
      {
         Data data = model.getData(dp.getData());

         isStruct = isStructuredType(model, data);
      }

      return isStruct;
   }

   public static boolean isDmsType(Model model, Data data)
   {
      boolean isDms = false;

      String dataType = getTypeId(data);
      isDms = StructuredTypeRtUtils.isDmsType(dataType);

      return isDms;
   }

   public static boolean isDmsType(Model model, DataMapping dm)
   {
      boolean isDms = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isDms = isDmsType(model, data);
      }

      return isDms;
   }

   public static boolean isDmsType(Model model, DataPath dp)
   {
      boolean isDms = false;

      if (null != model)
      {
         Data data = model.getData(dp.getData());

         isDms = isDmsType(model, data);
      }

      return isDms;
   }

   public static boolean isSerializableType(Model model, Data data)
   {
      boolean isSer = false;

      String dataType = getTypeId(data);
      isSer = PredefinedConstants.SERIALIZABLE_DATA.equals(dataType);

      return isSer;
   }

   public static boolean isSerializableType(Model model, DataMapping dm)
   {
      boolean isSer = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isSer = isSerializableType(model, data);
      }

      return isSer;
   }

   public static boolean isSerializableType(Model model, DataPath dp)
   {
      boolean isSer = false;

      if (null != model)
      {
         Data data = model.getData(dp.getData());

         isSer = isSerializableType(model, data);
      }

      return isSer;
   }

   public static boolean isEntityBeanType(Model model, Data data)
   {
      boolean isSer = false;

      String dataType = getTypeId(data);
      isSer = PredefinedConstants.ENTITY_BEAN_DATA.equals(dataType);

      return isSer;
   }

   public static boolean isEntityBeanType(Model model, DataMapping dm)
   {
      boolean isSer = false;

      if (null != model)
      {
         Data data = model.getData(dm.getDataId());

         isSer = isSerializableType(model, data);
      }

      return isSer;
   }

   public static boolean isEntityBeanType(Model model, DataPath dp)
   {
      boolean isSer = false;

      if (null != model)
      {
         Data data = model.getData(dp.getData());

         isSer = isSerializableType(model, data);
      }

      return isSer;
   }

   public static boolean isDmsDocumentList(Model model, Data data)
   {
      boolean isDms = false;

      if (null != model)
      {
         String dataType = getTypeId(data);
         isDms = DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataType);
      }

      return isDms;
   }

   public static boolean isDmsFolderList(Model model, Data data)
   {
      boolean isDms = false;

      if (null != model)
      {
         String dataType = getTypeId(data);
         isDms = DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataType);
      }

      return isDms;
   }

   public static XmlValueXto newXmlValue(Node value)
   {
      // TODO convert map to XML more efficiently
      org.w3c.dom.Document dom = XmlUtils.parseString(value.toXML());

      XmlValueXto result = new XmlValueXto();
      result.getAny().add(dom.getDocumentElement());

      return result;
   }

   public static void handleXmlValue(boolean isComplexValue, Node[] values,
         ParameterXto parameterXto)
   {
      // TODO convert map to XML more efficiently

      XmlValueXto xmlValue = new XmlValueXto();

      for (int i = 0; i < values.length; i++ )
      {
         Node value = values[i];

         if (isComplexValue)
         {
            org.w3c.dom.Document dom = XmlUtils.parseString(value.toXML());
            xmlValue.getAny().add(dom.getDocumentElement());
         }
         else
         {
            parameterXto.setPrimitive(values[0].getValue());
         }
      }
      if (isComplexValue)
      {
         parameterXto.setXml(xmlValue);
      }
   }

   public static String getTypeId(Data data)
   {
      return (String) Reflect.getFieldValue(data, "typeId");
   }
}

