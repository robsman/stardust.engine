package org.eclipse.stardust.engine.extensions.camel.converter;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CSV_DEFAULT_DELIMITER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CSV_DELIMITER_KEY;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.CSV_AUTOGENHEADERS_KEY;

import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.commons.lang.CharUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataMapping;

/**
 * 
 * @author Sabri.Bousselmi
 *
 */
public class CsvTypeConverter extends AbstractBpmTypeConverter {
	
	private static final transient Logger logger = LogManager.getLogger(CsvTypeConverter.class);
	
	private boolean autogenHeaders;
	private char delimiter;
	protected CsvTypeConverter(Exchange exchange) 
	{
		super(exchange);
		autogenHeaders = true;
		delimiter = CSV_DEFAULT_DELIMITER;
	}
	
	protected CsvTypeConverter(Exchange exchange, Map<String, Object> params) 
	{
		super(exchange);
		this.initializeParameters(params);
	}

	@SuppressWarnings({"rawtypes"})
	@Override
	public void unmarshal(DataMapping mapping, Map<String, Object> extendedAttributes)
	{
		String csv = (String) findDataValue(mapping, extendedAttributes);
		if (csv != null) 
		{
			Set sdtKeys = null;
			if(isStuctured(mapping))
			{
				long modelOid = new Long(mapping.getModelOID());
				SDTConverter converter = new SDTConverter(mapping, modelOid);
				sdtKeys = converter.getxPathMap().getAllXPaths();
			}
			Object result = CsvUtil.unmarshal(mapping, csv, sdtKeys, delimiter);

			this.replaceDataValue(mapping, result, extendedAttributes);
		}
		
		if (logger.isDebugEnabled())
	    {
			logger.debug("Unmarshal CSV input: [" + csv + "]\n" + "using the following delimiter: '" + delimiter + "'");
	    }
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void marshal(DataMapping mapping, Map<String, Object> extendedAttributes) 
	{
		StringBuilder csv = new StringBuilder();
		Object data = this.findDataValue(mapping, extendedAttributes);
		if(data != null)
		{
			Set sdtKeys = null;
			if(isStuctured(mapping))
			{
				long modelOid = new Long(mapping.getModelOID());
			    SDTConverter converter = new SDTConverter(mapping, modelOid);
			    sdtKeys = converter.getxPathMap().getAllXPaths();
			}
			csv = CsvUtil.marshal(mapping, data, sdtKeys, delimiter, autogenHeaders);
			
		    this.replaceDataValue(mapping, csv.toString(), extendedAttributes);
		}
		
		if (logger.isDebugEnabled())
	    {
			logger.debug("Marshal data input: [" + data + "]\n" + "using the following Options:" +
					" delimiter: '" + delimiter + "', "
					+ "autogenHeaders: " + autogenHeaders);
	    }
	}
	
	private void initializeParameters(Map<String, Object> params)
	{
		this.delimiter = CharUtils.toChar(
				(String) params.get(CSV_DELIMITER_KEY) == null ? "," : (String) params.get(CSV_DELIMITER_KEY));
		this.autogenHeaders = params.get(CSV_AUTOGENHEADERS_KEY) == null ? true : Boolean.valueOf((String) params.get(CSV_AUTOGENHEADERS_KEY));
	}
}
