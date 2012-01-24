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

import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
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
	public static final Logger trace = LogManager
			.getLogger(MessageParsingApplicationInstance.class);

	/**
	 * List of
	 * <code>org.eclipse.stardust.common.Pair<code> objects containing ID and value of all IN access points.
	 */
	private List inAccessPointValues = new ArrayList();
	/**
	 * Contains the IDs of the output access points of the rule to be invoked.
	 */
	private Map /* <String,DataMapping> */outAccessPoints = new HashMap();
	/**
	 * Contains the output values of the rule call by access point ID
	 */
	private Map outputValues;

	private IMessageFormat messageFormat;

	private String schema;

   private IModel model;

	public void bootstrap(ActivityInstance activityInstance)
	{
		trace.info("bootstrap()");
		
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      this.model = modelManager.findModel(activityInstance.getModelOID());

      Application application = activityInstance.getActivity().getApplication();
		
		List allOutDataMappings = activityInstance.getActivity().getApplicationContext(
				PredefinedConstants.APPLICATION_CONTEXT).getAllOutDataMappings();
      if (allOutDataMappings.size() == 0)
      {
         throw new RuntimeException("Could not find OUT data mapping");
      }
      for (Iterator i = allOutDataMappings.iterator(); i.hasNext();)
		{
			DataMapping mapping = (DataMapping) i.next();

			outAccessPoints.put(mapping.getApplicationAccessPoint().getId(),
					mapping);
		}

		outputValues = new HashMap();

		String messageFormatId = (String) application.getAttribute(Constants.MESSAGE_FORMAT);

		trace.info("Message Format ID: " + messageFormatId);

		try
		{
			messageFormat = RuntimeFormatManager
					.getMessageFormat(messageFormatId);
		}
		catch (Throwable x)
		{
			throw new RuntimeException("Could not retrieve message format for ID '"+messageFormatId+"'", x);
		}

		trace.info("Message Format: " + messageFormat);
		schema = (String) application
				.getAttribute(Constants.FORMAT_MODEL_FILE_PATH);

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

		trace
				.info("inAccessPointValues.size() = "
						+ inAccessPointValues.size());
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

		for (Iterator i = outAccessPoints.keySet().iterator(); i.hasNext();)
		{
			String name = (String) i.next();

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
		for (Iterator i = inAccessPointValues.iterator(); i.hasNext();)
		{
			Pair entry = (Pair) i.next();

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

		try
		{
	       DataMapping oudDataMapping = (DataMapping) this.outAccessPoints
	          .values().iterator().next();
	       IData data = model.findData(oudDataMapping.getDataId());

			org.w3c.dom.Document schemaDocument = MessagingUtils.getStructuredAccessPointSchema(model, data);
			String inputMessageString = null;

			for (Iterator iterator = inAccessPointValues.iterator(); iterator
					.hasNext();)
			{
				Pair entry = (Pair) iterator.next();
				String name = (String) entry.getFirst();

				inputMessageString = (String) entry.getSecond();

				trace.info("Setting input access point " + name
						+ " to value " + inputMessageString + ".");
			}

			org.w3c.dom.Document parsedDocument = messageFormat.parse(new StringBufferInputStream(inputMessageString), schemaDocument);
			Document document = DOMConverter.convert(parsedDocument);					
			
			// Read the output data

			outputValues.clear();

			IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
			StructuredDataConverter structuredDataConverter = new StructuredDataConverter(
					xPathMap);
			Map/*<String, Object>*/ outputMessage = (Map/*<String, Object>*/) structuredDataConverter
					.toCollection(document.getRootElement(), "", true);

			for (Iterator iterator = outAccessPoints.keySet().iterator(); iterator
					.hasNext();)
			{
				String accessPointID = (String) iterator.next();

				trace.info("Setting input access point "
						+ accessPointID + " to value " + outputMessage + ".");

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
}
