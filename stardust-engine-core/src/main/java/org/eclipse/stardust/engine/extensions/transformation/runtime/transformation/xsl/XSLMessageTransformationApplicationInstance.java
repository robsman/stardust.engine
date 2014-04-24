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
package org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.xsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
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
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.core.struct.sxml.converters.DOMConverter;
import org.eclipse.stardust.engine.extensions.transformation.Constants;
import org.xml.sax.SAXException;

public class XSLMessageTransformationApplicationInstance implements
		SynchronousApplicationInstance
{
	public static final Logger trace = LogManager
			.getLogger(XSLMessageTransformationApplicationInstance.class);

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

	private TransformerFactory transformerFactory;

	private DocumentBuilderFactory documentBuilderFactory;

	private Transformer transformer;

	private org.w3c.dom.Document xslDocument;

	private StructuredDataConverter inputStructuredDataConverter;

	private StructuredDataConverter outputStructuredDataConverter;

	private org.w3c.dom.DOMImplementation domImpl = null;

	public void bootstrap(ActivityInstance activityInstance)
	{
		trace.info("bootstrap");

		Application application = activityInstance.getActivity()
				.getApplication();
		ModelManager modelManager = ModelManagerFactory.getCurrent();
        IModel model = modelManager.findModel(activityInstance.getModelOID());     
		
		List allInDataMappings = activityInstance.getActivity().getApplicationContext(
				PredefinedConstants.APPLICATION_CONTEXT).getAllInDataMappings();
		if (allInDataMappings.size() == 0)
        {
			throw new RuntimeException("Could not find IN data mapping");
        }
        
		DataMapping dataMapping = (DataMapping) allInDataMappings.get(0);
		
		IData data = model.findData(
				dataMapping.getDataId());
		IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
		inputStructuredDataConverter = new StructuredDataConverter(xPathMap);

		trace.info("Structured converter retrieved for data "
				+ dataMapping.getDataId());

		// Retrieve structured data converters for output data mapping

		List allOutDataMappings = activityInstance.getActivity().getApplicationContext(
				PredefinedConstants.APPLICATION_CONTEXT).getAllOutDataMappings();
		if (allOutDataMappings.size() == 0)
        {
			throw new RuntimeException("Could not find OUT data mapping");
        }
		
		dataMapping = (DataMapping) allOutDataMappings.get(0);
		data = model.findData(dataMapping.getDataId());
		xPathMap = DataXPathMap.getXPathMap(data);
		outputStructuredDataConverter = new StructuredDataConverter(xPathMap);

		// Initialize XSL resources

		transformerFactory = TransformerFactory.newInstance();

		if (!(transformerFactory.getFeature(DOMSource.FEATURE) && transformerFactory
				.getFeature(DOMResult.FEATURE)))
		{
			throw new RuntimeException(
					"DOM parsing not supported with XML settings and libraries.");
		}

		// Instantiate a DocumentBuilderFactory.

		documentBuilderFactory = DocumentBuilderFactory.newInstance();

		// And setNamespaceAware, which is required when parsing xsl files

		documentBuilderFactory.setNamespaceAware(true);

		// Use the DocumentBuilderFactory to create a DocumentBuilder.

		DocumentBuilder dBuilder = null;

		try
		{
			dBuilder = documentBuilderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException("Cannot configure XML parser.", e);
		}

		// Use the DocumentBuilder to parse the XSL stylesheet.

		try
		{			
			String xslCode = (String) (application
			.getAttribute(Constants.XSL_STRING));
			InputStream stringReader = new ByteArrayInputStream(xslCode.getBytes("UTF8"));
			xslDocument = dBuilder.parse(stringReader);
		}
		catch (SAXException e)
		{
			throw new RuntimeException(
					"Cannot read input stream with XSL content.", e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(
					"Cannot read input stream with XSL content.", e);
		}

		// Use the DOM Document to define a DOMSource object.

		DOMSource xslDomSource = new DOMSource(xslDocument);

		// Set the systemId: note this is actually a URL, not a local filename

		xslDomSource.setSystemId("transformation.xsl");

		// Process the stylesheet DOMSource and generate a Transformer.

		try
		{
			transformer = transformerFactory.newTransformer(xslDomSource);
		}
		catch (TransformerConfigurationException e)
		{
			throw new RuntimeException("Cannot initialize transformer.", e);
		}

		// Retrieve OUT mappingss

		for (Iterator i = activityInstance.getActivity().getApplicationContext(
		      PredefinedConstants.APPLICATION_CONTEXT).getAllOutDataMappings()
				.iterator(); i.hasNext();)
		{
			DataMapping mapping = (DataMapping) i.next();

			trace.info(mapping.getApplicationAccessPoint().getId());

			outAccessPoints.put(mapping.getApplicationAccessPoint().getId(),
					mapping);
		}

		outputValues = new HashMap();

		// Create DOM Implementation

		domImpl = getDOMImplementation();
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
		trace.info("invoke()");

		try
		{
			Map inputMessage = null;

			for (Iterator iterator = inAccessPointValues.iterator(); iterator
					.hasNext();)
			{
				Pair entry = (Pair) iterator.next();
				String name = (String) entry.getFirst();

				inputMessage = (Map) entry.getSecond();

				trace.info("Setting access point " + name + " to value "
						+ inputMessage + ".");
			}

			// Convert internal map representation to SXML and then to DOM object

			Node[] nodes = inputStructuredDataConverter
					.toDom(inputMessage, "", true);

			Assert.condition(nodes.length == 1);

			Document inputDocument = new Document((Element)nodes[0]);

			System.out.println("Input document: " + inputDocument);

			org.w3c.dom.Document xmlDoc = DOMConverter.convert(inputDocument,
					domImpl);

			//printXML(xmlDoc);
			
			// Use the DOM Document to define a DOMSource object.

			DOMSource xmlDomSource = new DOMSource(xmlDoc);

			// Set the base URI for the DOMSource so any relative URIs it
			// contains can be resolved.

			xmlDomSource.setSystemId("transformation.xml");

			// Create an empty DOMResult for the Result.

			DOMResult domResult = new DOMResult();

			// Perform the transformation, placing the output in the DOMResult.

			transformer.transform(xmlDomSource, domResult);
			Document outputDocument = DOMConverter
					.convert((org.w3c.dom.Document) domResult.getNode());
			
			System.out.println("Output document: " + outputDocument);
			
			Map outputMessage = (Map) outputStructuredDataConverter
			   .toCollection(outputDocument.getRootElement(), "", true);

			// Write output message

			outputValues.clear();

			for (Iterator iterator = outAccessPoints.keySet().iterator(); iterator
					.hasNext();)
			{
				String accessPointID = (String) iterator.next();

				outputValues.put(accessPointID, outputMessage);
			}
		}
      catch (Exception e)
      {
         throw new InvocationTargetException((Throwable) e,
               "Could not perform XSL message transformation.");
      }

		return doGetOutAccessPointValues(outDataTypes);
	}

	/*private void printXML(org.w3c.dom.Document xmlDoc) {
        try {
        	System.out.println("XML Input Doc:");
			Transformer transformer = TransformerFactory.newInstance().newTransformer();			
        	TransformerFactory.newInstance().newTransformer().transform( new DOMSource(xmlDoc), new StreamResult(System.out));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}		
	}*/

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
