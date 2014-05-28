package org.eclipse.stardust.engine.extensions.camel.converter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.core.struct.TypedXPath;

/**
 * 
 * @author Sabri.Bousselmi
 *
 */
public class CsvUtil {

	private static final transient Logger logger = LogManager.getLogger(CsvUtil.class);
	
	private static final char CSV_QUOTE = '"';
	private static final String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
	
	@SuppressWarnings("rawtypes")
	public static Object unmarshal(String mapping, String csv, Set sdtKeys, char delimiter) 
	{
		Object result = null;
		if(sdtKeys != null)
		{
			result = CsvUtil.unmarshalSDT(mapping, csv, sdtKeys, delimiter);
		} else
		{
			result = CsvUtil.unmarshalPrimitive(csv, delimiter);
		}
		return result;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static StringBuilder marshal(DataMapping mapping, Object dataMap, Set sdtKeys, char delimiter, boolean autogenHeaders) 
	{
		StringBuilder csv = new StringBuilder();
		if (dataMap instanceof Map<?, ?>) 
		{
			Map<String, Object> data = Map.class.cast(dataMap);
			// We support Flat Map or List of Flat Map
			csv = CsvUtil.marshalSDT(data, sdtKeys, delimiter, autogenHeaders);
		} else
		{
			csv = CsvUtil.marshalPrimitive(mapping, dataMap, delimiter, autogenHeaders);
		}
		return csv;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String, Object> unmarshalSDT(String mapping, String csv, Set sdtKeys, char delimiter)
	{
		Map<String, Object> sdt = new HashMap<String, Object>();
		String[] data = csv.split("\r\n|\r|\n");
		if (data.length > 2 || (data.length == 2 && isSdtListExpected(sdtKeys))) 
      {
         // convert to List of SDT
         List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
         String firstLine = data[0];
         int i = 1;
         while (i < data.length) 
         {
            String secondLine = data[i];
            Map<String, Object> map = CsvUtil.createMap(sdtKeys, firstLine, secondLine, delimiter, true);
            list.add(map);
            i++;
         }
         sdt.put(mapping, list);
         
      } else if (data.length == 2) 
      {
         // convert to SDT
         String firstLine = data[0];
         String secondLine = data[1];
         sdt = CsvUtil.createMap(sdtKeys, firstLine, secondLine, delimiter, false);
         
      } else
		{
			logger.warn("The CSV input must contain more than one Line");
		}
		return sdt;
	}
	
	private static String unmarshalPrimitive(String csv, char delimiter)
	{
		String primitive = StringUtils.EMPTY;
		String[] data = csv.split("\r\n|\r|\n");
		if(data.length == 1)
		{
			primitive = unescapeCsv(data[0], delimiter);
		} else if(data.length >1)
		{
			primitive = unescapeCsv(data[1], delimiter);
		}
		return primitive;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static StringBuilder marshalSDT(Map<String, Object> data, Set sdtKeys, char delimiter, boolean autogenHeaders)
	{
		StringBuilder csv = new StringBuilder();
		int nbKey = data.keySet().size();
		String firstKey = (String) data.keySet().toArray()[0];
		if ((nbKey == 1) && (data.get(firstKey) instanceof List<?>)) 
		{
			// List of Flat Map
			List<Map<String, Object>> listMap = (List<Map<String, Object>>) data
					.get(firstKey);
			// Retreive first Line only from the first key Map
			Map<String, Object> firstMap = listMap.get(0);
			Set orderedKeys = CsvUtil.getOrderedKeys(sdtKeys, firstMap, true);
			if (autogenHeaders) 
			{
				StringBuilder firstLine = CsvUtil.getFirstLine(orderedKeys, delimiter);
				csv.append(firstLine.toString());
			}

			for (Map<String, Object> map : listMap) 
			{
				if (!csv.toString().isEmpty()) 
				{
					csv.append("\n");
				}
				StringBuilder nextLine = CsvUtil.getNextLine(orderedKeys, map, delimiter);
				csv.append(nextLine.toString());
			}
		} else 
		{
			// Flat Map
			Set orderedKeys = CsvUtil.getOrderedKeys(sdtKeys, data, false);
			if (autogenHeaders) 
			{
				StringBuilder firstLine = CsvUtil.getFirstLine(orderedKeys, delimiter);
				csv.append(firstLine.toString() + "\n");
			}
			StringBuilder secondtLine = CsvUtil.getNextLine(orderedKeys, data, delimiter);
			csv.append(secondtLine.toString());
		}

		return csv;
	}
	
	private static StringBuilder marshalPrimitive(DataMapping mapping, Object dataMap, char delimiter, boolean autogenHeaders)
	{
		StringBuilder csv = new StringBuilder();
		if (autogenHeaders) 
		{
			csv.append(mapping.getDataId() + "\n");
		}
		csv.append(escapeCsv((String) dataMap, delimiter));
		return csv;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static StringBuilder getNextLine(Set orderedKeys, Map<String, Object> map, char delimiter) 
	{
		StringBuilder nextLine = new StringBuilder();
		String separator = StringUtils.EMPTY;
		boolean addedEmptyValue = false;
		// Flat Map
		// List of key
		Iterator<TypedXPath> it = orderedKeys.iterator();
		while(it.hasNext())
		{
			TypedXPath typedXPath = it.next();
			String key = typedXPath.getId();
			if(!nextLine.toString().isEmpty() || addedEmptyValue)
			{
				separator = CharUtils.toString(delimiter);
			}
			
			Object value = map.get(key);
			if(value == null)
			{
				nextLine.append(separator + StringUtils.EMPTY);
				addedEmptyValue = true;
				
			} else
			{
				// escapeCsv for String Types
				if(typedXPath.getXsdTypeName().equals("string"))
				{
					nextLine.append(separator + escapeCsv((String) value, delimiter));
				} else if(typedXPath.getXsdTypeName().equals("date"))
				{
					nextLine.append(separator + escapeCsv(Date.class.cast(value).toString(), delimiter));
				} else
				{
					nextLine.append(separator + value);
				}
			}
			
		}
		return nextLine;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static StringBuilder getFirstLine(Set orderedKeys, char delimiter)
	{
		StringBuilder firstLine = new StringBuilder();
		String separator = StringUtils.EMPTY;
		// Flat Map
		// List of key
		Iterator<TypedXPath> it = orderedKeys.iterator();
		while(it.hasNext())
		{
			if(!firstLine.toString().isEmpty())
			{
				separator = CharUtils.toString(delimiter);
			}
			firstLine.append(separator + it.next().getId());
		}
		return firstLine;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, Object> createMap(Set sdtKeys, String firstLine, String secondLine, char delimiter, boolean ignoreChildXPath)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		String[] keys = stringToCsv(firstLine, delimiter);
		String[] values = stringToCsv(secondLine, delimiter);
		if(keys.length != values.length)
		{
			logger.warn("Header line must contain the same number of fields as the records in"
						+"the rest of the CSV input");
			logger.warn("Header line: " + firstLine);
			logger.warn("Wrong line: " + secondLine);
			logger.warn("CSV Unmarshalling is ignored for this line: " + secondLine);

			return map;
		}
		Iterator<TypedXPath> it = sdtKeys.iterator();
		while(it.hasNext())
		{
			TypedXPath typedXPath = it.next();
			String key = typedXPath.getId();
			if (!key.isEmpty()
					&& (!CsvUtil.isChildXPath(typedXPath) || ignoreChildXPath)
					&& !typedXPath.isList() && !typedXPath.getXPath().isEmpty())
			{
				// search key in first Line
				if(Arrays.asList(keys).contains(key))
				{
					int indexKey = Arrays.asList(keys).indexOf(key);
					String value = values[indexKey];
					map.put(key, unescapeCsv(value, delimiter));
				}
			}
		}
		return map;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Set<TypedXPath> getOrderedKeys(Set sdtKeys, Map<String, Object> map, boolean ignoreChildXPath)
	{
		Set<TypedXPath> orderedKeys = new TreeSet<TypedXPath>(new Comparator<TypedXPath>() {

			@Override
			public int compare(TypedXPath o1, TypedXPath o2)
			{
				return  o1.getOrderKey() - o2.getOrderKey();
			}
		});
		
		Iterator<TypedXPath> it = sdtKeys.iterator();
		while(it.hasNext())
		{
			TypedXPath typedXPath = it.next();
			if ((!CsvUtil.isChildXPath(typedXPath) || ignoreChildXPath)
					&& !typedXPath.isList() && !typedXPath.getId().isEmpty()
					&& !typedXPath.getXPath().isEmpty()) 
			{
				orderedKeys.add(typedXPath);
			}
		}
		return orderedKeys;
	}
	
	
	/**
	 * /* see <a href="http://en.wikipedia.org/wiki/Comma-separated_values">Wikipedia</a> and
     * <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>.
     * 
	 * @param str the input CSV column String
	 * @param delemiter
	 * @return the input String, enclosed in double quotes if the value contains a delimiter,
     * newline or double quote, <code>null</code> if null string input
	 */
    private static String escapeCsv(String str, char delemiter) {
    	char[] CSV_SEARCH_CHARS = new char[] {delemiter, CSV_QUOTE, CharUtils.CR, CharUtils.LF};
        if (StringUtils.containsNone(str, CSV_SEARCH_CHARS)) {
            return str;
        }
        try {
            StringWriter writer = new StringWriter();
            escapeCsv(writer, str, CSV_SEARCH_CHARS);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            ioe.printStackTrace();
            return null;
        }
    }
    
    
   private static void escapeCsv(Writer out, String str, char[] CSV_SEARCH_CHARS) throws IOException {
       if (StringUtils.containsNone(str, CSV_SEARCH_CHARS)) {
           if (str != null) {
               out.write(str);
           }
           return;
       }
       out.write(CSV_QUOTE);
       for (int i = 0; i < str.length(); i++) {
           char c = str.charAt(i);
           if (c == CSV_QUOTE) {
               out.write(CSV_QUOTE); // escape double quote
           }
           out.write(c);
       }
       out.write(CSV_QUOTE);
   }

   /**
    * * see <a href="http://en.wikipedia.org/wiki/Comma-separated_values">Wikipedia</a> and
    * <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>.
    *
    * @param str the input CSV column String
    * @param delimiter
    * @return the input String, with enclosing double quotes removed and embedded double 
    * quotes unescaped, <code>null</code> if null string input
    */
   private static String unescapeCsv(String str, char delimiter) {
       if (str == null) {
           return null;
       }
       try {
           StringWriter writer = new StringWriter();
           unescapeCsv(writer, str, delimiter);
           return writer.toString();
       } catch (IOException ioe) {
           // this should never ever happen while writing to a StringWriter
           ioe.printStackTrace();
           return null;
       }
   }
   
   private static void unescapeCsv(Writer out, String str, char delimiter) throws IOException {
	     if (str == null) {
	         return;
	     }
	     if (str.length() < 2) {
	         out.write(str);
	         return;
	     }
	     if ( str.charAt(0) != CSV_QUOTE || str.charAt(str.length() - 1) != CSV_QUOTE ) {
	         out.write(str);
	         return;
	     }

	     // strip quotes
	     String quoteless = str.substring(1, str.length() - 1);
	     char[] CSV_SEARCH_CHARS = new char[] {delimiter, CSV_QUOTE, CharUtils.CR, CharUtils.LF};
	     if ( StringUtils.containsAny(quoteless, CSV_SEARCH_CHARS) ) {
	         // deal with escaped quotes; ie) ""
	         str = StringUtils.replace(quoteless, CSV_QUOTE_STR + CSV_QUOTE_STR, CSV_QUOTE_STR);
	     }

	     out.write(str);
	 }
	
	
   private static String[] stringToCsv(String input, char delimiter)
	{
		 String otherThanQuote = " [^\"] ";
	     String quotedString = String.format(" \" %s* \" ", otherThanQuote);
	     String regex = String.format("(?x) "+ // enable comments, ignore white spaces
	    		  "\\"                       + 
	    		  CharUtils.toString(delimiter)	// match a delimiter
	    		  							 + 
	              "(?=                      "+ // start positive look ahead
	              "  (                      "+ //   start group 1
	              "    %s*                  "+ //     match 'otherThanQuote' zero or more times
	              "    %s                   "+ //     match 'quotedString'
	              "  )*                     "+ //   end group 1 and repeat it zero or more times
	              "  %s*                    "+ //   match 'otherThanQuote'
	              "  $                      "+ // match the end of the string
	              ")                        ", // stop positive look ahead
	              otherThanQuote, quotedString, otherThanQuote);

	     String[] csv = input.split(regex, -1);
	     return csv;
	}
	
	private static boolean isChildXPath(TypedXPath typedXPath)
	{
		return typedXPath.getXPath().contains("/") || !typedXPath.getChildXPaths().isEmpty();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
   private static boolean isSdtListExpected(Set sdtKeys)
   {
      Iterator<TypedXPath> it = sdtKeys.iterator();
      while(it.hasNext())
      {
         TypedXPath typedXPath = it.next();
         if(typedXPath.getXPath().isEmpty() || typedXPath.getXPath().contains("/")
               || typedXPath.getParentXPath().isList() || typedXPath.isList())
         {
            continue;
         }
         else
         {
            return false;
         }
      }
      return true;
   }
	
}
