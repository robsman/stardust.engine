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
package org.eclipse.stardust.engine.extensions.transformation.runtime.serialization;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
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
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.eclipse.stardust.engine.extensions.transformation.MessagingUtils;
import org.eclipse.stardust.engine.extensions.transformation.format.IMessageFormat;
import org.eclipse.stardust.engine.extensions.transformation.format.RuntimeFormatManager;
import org.w3c.dom.Document;



/**
 * 
 */
public class MessageSerializationApplicationInstance implements
		SynchronousApplicationInstance
{
	public static final Logger trace = LogManager
			.getLogger(MessageSerializationApplicationInstance.class);

	/**
	 * List of
	 * <code>org.eclipse.stardust.common.Pair<code> objects containing ID and value of all IN access points.
	 */
	private List inAccessPointValues = new ArrayList();
	private StructuredDataConverter structuredDataConverter;
	/**
	 * Contains the IDs of the output access points of the rule to be invoked.
	 */
	private Map /* <String,DataMapping> */outAccessPoints = new HashMap();
	/**
	 * Contains the output values of the rule call by access point ID
	 */
	private Map outputValues;

	private IMessageFormat messageFormat;

	private org.w3c.dom.DOMImplementation domImpl = null;
	private IXPathMap xPathMap;

   private Document schemaDocument;

	public void bootstrap(ActivityInstance activityInstance)
	{
		Application application = activityInstance.getActivity()
				.getApplication();
		ModelManager modelManager = ModelManagerFactory.getCurrent();

      IModel model = modelManager.findModel(activityInstance.getModelOID());

		// Retrieve structured data converters for data mappings
		// @@@ TODO Implement for multiple input messages

      
		List allInDataMappings = activityInstance.getActivity()
				.getApplicationContext(PredefinedConstants.APPLICATION_CONTEXT)
				.getAllInDataMappings();
      if (allInDataMappings.size() == 0)
      {
         throw new RuntimeException("Could not find IN data mapping");
      }
      DataMapping dataMapping = (DataMapping) allInDataMappings.iterator().next();
		IData data = model.findData(
				dataMapping.getDataId());		
		xPathMap = DataXPathMap.getXPathMap(data);
		structuredDataConverter = new StructuredDataConverter(xPathMap);

		// Retrieve OUT mappings
		
		String messageFormatId = (String) application.getAttribute(Constants.MESSAGE_FORMAT);
		
		this.schemaDocument = MessagingUtils.getStructuredAccessPointSchema(model, data);
		
      try
		{
			for (Iterator i = activityInstance.getActivity()
					.getApplicationContext(
					      PredefinedConstants.APPLICATION_CONTEXT)
					.getAllOutDataMappings().iterator(); i.hasNext();)
			{
				DataMapping mapping = (DataMapping) i.next();

				trace.debug("Out access point '"+mapping.getApplicationAccessPoint().getId()+"'");
				outAccessPoints.put(mapping.getApplicationAccessPoint().getId(), mapping);
			}
		}
		catch (Exception e)
		{
         throw new RuntimeException("Could not initialize message serialization application", e);
		}

		outputValues = new HashMap();

		// Prepare message format
		messageFormat = RuntimeFormatManager.getMessageFormat(messageFormatId);

		// Create DOM Implementation
		domImpl = getDOMImplementation();
	}

	/**
	 * 
	 */
	public void setInAccessPointValue(String name, Object value)
	{
		trace.debug("setInAccessPoint(" + name + ", " + value + ")");

		Pair param = findAccessPointValue(name);

		if (null != param)
		{
			inAccessPointValues.remove(param);
		}

		inAccessPointValues.add(new Pair(name, value));

		trace.debug("inAccessPointValues.size() = " + inAccessPointValues.size());
	}

	/**
	 * Only for processing in data mappings.
	 */
	public Object getOutAccessPointValue(String name)
	{
		try
		{
			return doGetOutAccessPointValue(name, false);
		}
		catch (InvocationTargetException e)
		{
			throw new InternalException(e.getMessage(), e.getTargetException());
		}
	}

	/**
	 * 
	 */
	public void cleanup()
	{
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
				result.put(name, doGetOutAccessPointValue(name, true));
			}
		}

		return result;
	}

	/**
	 * 
	 * @param name
	 * @param allowReturnValue
	 * @return
	 * @throws InvocationTargetException
	 */
	private Object doGetOutAccessPointValue(String name,
			boolean allowReturnValue) throws InvocationTargetException
	{
		return outputValues.get(name);
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
		trace.debug("invoke()");

		try
		{
			Map inputMessage = null;

			for (Iterator iterator = inAccessPointValues.iterator(); iterator
					.hasNext();)
			{
				Pair entry = (Pair) iterator.next();
				String name = (String) entry.getFirst();

				inputMessage = (Map) entry.getSecond();

				trace.info("Setting access point " + name + " to value " + inputMessage + ".");
			}

			Node[] nodes = structuredDataConverter.toDom(inputMessage, "", true);
			
			if (nodes == null || nodes.length == 0)
			{
			   throw new RuntimeException("Can not serialize: Input message is not initialized ("+inputMessage+")");
			}
			
			org.eclipse.stardust.engine.core.struct.sxml.Document domDocument = new org.eclipse.stardust.engine.core.struct.sxml.Document((Element)nodes[0]);
			org.w3c.dom.Document w3cDocument = DOMConverter.convert(domDocument, domImpl);			
			
			// Create output string

			outputValues.clear();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			messageFormat.serialize(w3cDocument, outputStream, schemaDocument);

			for (Iterator iterator = outAccessPoints.keySet().iterator(); iterator.hasNext();)
			{
				String accessPointID = (String) iterator.next();
				// outputStream.toString(): it's safe to use the default encoding for getting
				// the bytes from the stream since they have been written using the default
				// encoding as well (see messageFormat.serialize(...))
				outputValues.put(accessPointID, outputStream.toString());
			}			
		}
      catch (Exception e)
      {
         throw new InvocationTargetException((Throwable) e,
               "Could not perform message serialization.");
      }

		return doGetOutAccessPointValues(outDataTypes);
	}

	private org.w3c.dom.DOMImplementation getDOMImplementation()
	{
		if (domImpl == null)
		{
			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);

			try
			{
				domImpl = factory.newDocumentBuilder().getDOMImplementation();
			}
			catch (javax.xml.parsers.ParserConfigurationException e)
			{
				// do nothing special; just return null domImpl
			}
		}

		return domImpl;
	}
	
}