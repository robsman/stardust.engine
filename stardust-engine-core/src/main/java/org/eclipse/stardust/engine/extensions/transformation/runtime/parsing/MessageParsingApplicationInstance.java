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
package org.eclipse.stardust.engine.extensions.transformation.runtime.parsing;

import static org.eclipse.stardust.common.CollectionUtils.newList;
import static org.eclipse.stardust.common.CollectionUtils.newMap;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.eclipse.stardust.engine.extensions.transformation.MessagingUtils;
import org.eclipse.stardust.engine.extensions.transformation.format.IMessageFormat;
import org.eclipse.stardust.engine.extensions.transformation.format.RuntimeFormatManager;

/**
 * 
 */
public class MessageParsingApplicationInstance implements
   SynchronousApplicationInstance
{
   public static final Logger trace = LogManager.getLogger(MessageParsingApplicationInstance.class);

   /**
    * List of
    * <code>org.eclipse.stardust.common.Pair<code> objects containing ID and value of all IN access points.
    */
   private List<Pair> inAccessPointValues = newList();
   /**
    * Contains the IDs of the output access points of the rule to be invoked.
    */
   private Map<String,DataMapping> outAccessPoints = newMap();
   /**
    * Contains the output values of the rule call by access point ID
    */
   private Map<String, Object> outputValues;

   private IMessageFormat messageFormat;

   private String schema;

   private IModel model;
   
   private String outDataPath = "";

   public void bootstrap(ActivityInstance activityInstance)
   {
      trace.info("bootstrap()");

      ModelManager modelManager = ModelManagerFactory.getCurrent();
      this.model = modelManager.findModel(activityInstance.getModelOID());


      Application application = activityInstance.getActivity().getApplication();
      List<DataMapping> allOutDataMappings = activityInstance.getActivity().getApplicationContext(
            PredefinedConstants.APPLICATION_CONTEXT).getAllOutDataMappings();
      if (allOutDataMappings.size() == 0)
      {
         throw new RuntimeException("Could not find OUT data mapping");
      }
      
      
      DataMapping dataMapping = (DataMapping) allOutDataMappings.iterator().next();
      if (dataMapping.getDataPath() != null)
      {
         this.outDataPath = dataMapping.getDataPath();
      }
      
      for (DataMapping mapping : allOutDataMappings)
      {
         outAccessPoints.put(mapping.getApplicationAccessPoint().getId(), mapping);
      }

      outputValues = newMap();

      String messageFormatId = (String) application.getAttribute(Constants.MESSAGE_FORMAT);

      trace.info("Message Format ID: " + messageFormatId);

      try
      {
         messageFormat = RuntimeFormatManager.getMessageFormat(messageFormatId);
      }
      catch (Throwable x)
      {
         throw new RuntimeException("Could not retrieve message format for ID '"+messageFormatId+"'", x);
      }

      trace.info("Message Format: " + messageFormat);
      schema = (String) application.getAttribute(Constants.FORMAT_MODEL_FILE_PATH);

      trace.info("Schema: " + schema);
   }

   /**
    * 
    */
   public void setInAccessPointValue(String name, Object value)
   {
      trace.info("setInAccessPoint(" + name + ", " + value + ")");

      Pair param = findAccessPointValue(name);

      if (null != param)
      {
         inAccessPointValues.remove(param);
      }

      inAccessPointValues.add(new Pair(name, value));

      trace.info("inAccessPointValues.size() = " + inAccessPointValues.size());
   }

   /**
    * Only for processing in data mappings.
    */
   public Object getOutAccessPointValue(String name)
   {
      return outputValues.get(name);
   }

   /**
    * 
    */
   public void cleanup()
   {
      trace.info("cleanup()");
   }

   /**
    * 
    * @param outDataTypes
    * @return
    * @throws InvocationTargetException
    */
   private Map doGetOutAccessPointValues(Set outDataTypes)
      throws InvocationTargetException
   {
      Map result = new HashMap();

      for (String name : outAccessPoints.keySet())
      {
         if (outDataTypes.contains(name))
         {
            result.put(name, outputValues.get(name));
         }
      }

      return result;
   }

   /**
    * 
    * @param name
    * @return
    */
   private Pair findAccessPointValue(String name)
   {
      for (Pair entry : inAccessPointValues)
      {
         if (name.equals(entry.getFirst()))
         {
            return entry;
         }
      }

      return null;
   }

   /**
    * 
    */
   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      trace.info("invoke()");
      
      String outDataPath = "";

      try
      {
         DataMapping oudDataMapping = outAccessPoints.values().iterator().next();
                           
         IData data = model.findData(oudDataMapping.getDataId());
         

         org.w3c.dom.Document schemaDocument = getSchemaDocument(data);
         String inputMessageString = null;

         for (Pair entry : inAccessPointValues)
         {
            String name = (String) entry.getFirst();

            inputMessageString = (String) entry.getSecond();

            trace.info("Setting input access point " + name
                  + " to value " + inputMessageString + ".");
         }

         org.w3c.dom.Document parsedDocument = messageFormat.parse(new StringReader(inputMessageString), schemaDocument);
         Document document = fromW3CDocument(parsedDocument);					

         // Read the output data

         outputValues.clear();

         IXPathMap xPathMap = getXPathMap(data);
         StructuredDataConverter structuredDataConverter = newStructuredDataConverter(xPathMap);
         
         Map<String, Object> outputMessage = (Map<String, Object>) structuredDataConverter.toCollection(
               document.getRootElement(), this.outDataPath, true);

         for (String accessPointID : outAccessPoints.keySet())
         {
            trace.info("Setting input access point " + accessPointID + " to value " + outputMessage + ".");

            outputValues.put(accessPointID, outputMessage);
         }
      }
      catch (Exception e)
      {
         throw new InvocationTargetException((Throwable) e,
         "Could not perform message parsing.");
      }

      return doGetOutAccessPointValues(outDataTypes);
   }
   
   /* package-private */ org.w3c.dom.Document getSchemaDocument(final IData data)
   {
      return MessagingUtils.getStructuredAccessPointSchema(data);
   }
   
   /* package-private */ Document fromW3CDocument(final org.w3c.dom.Document document)
   {
      return DOMConverter.convert(document);
   }
   
   /* package-private */ IXPathMap getXPathMap(final IData data)
   {
      return DataXPathMap.getXPathMap(data);
   }
   
   /* package-private */ StructuredDataConverter newStructuredDataConverter(final IXPathMap xPathMap)
   {
      return new StructuredDataConverter(xPathMap);
   }
}
