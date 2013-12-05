package org.eclipse.stardust.engine.extensions.camel.util.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.DataFragmentValue;

/**
 * Supports expressing key-value-pair data as Strings and provides parsing capabilities.
 * 
 * Examples:
 * 
 * "Name::Peter" => Primitive String data with ID 'Name'<br/>
 * "ClientNumber::56788::long" => Primitive Long data with ID 'ClientNumber'<br/>
 * "Person.name::Peter" => Structured Data with ID 'Person' and inner path 'name' of String type<br/>
 * "Person.address/zip::65342::int" => Structured Data with ID 'Person' and inner path 'address/zip' of Integer type
 * 
 * Supported data type conversions are:
 * <ul>
 * <li>long</li>
 * <li>int|integer</li>
 * <li>bool|boolean</li>
 * <li>double</li>
 * <li>byte</li>
 * <li>float</li>
 * <li>short</li>
 * <li>char</li>
 * <li>date</li>
 * </ul>
 * 
 * @author JanHendrik.Scheufen
 * 
 */
public class KeyValueList
{

   public static final String STRUCT_PATH_DELIMITER = ".";
   public static final String STRUCT_PATH_DELIMITER_REGEX = "\\.";
   public static final String SERIALIZABLE_METHOD_DELIMITER_REGEX = "\\(";
   public static final String PARTS_SEPARATOR = "::";
   private List<KeyValue> keyValuePairs;
   private Map<String, Object> keyValueMap;
   private Map<String, Map<String, Object>> complexTypes = new HashMap<String, Map<String, Object>>();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

   private static Logger log = LogManager.getLogger(KeyValueList.class);

   private Boolean serializable;

   public KeyValueList(List<String> entries)
   {
      this.serializable = false;
      setEntries(entries);
   }

   public KeyValueList(String[] entries)
   {
      this.serializable = false;
      setEntries(Arrays.asList(entries));
   }

   public KeyValueList(String[] entries, Boolean serializable)
   {
      this.serializable = serializable;
      setEntries(Arrays.asList(entries));
   }

   public KeyValueList(List<String> entries, Boolean serializable)
   {
      this.serializable = serializable;
      setEntries(entries);
   }

   protected void setEntries(List<String> list)
   {
      if (list != null)
      {
         this.keyValuePairs = new ArrayList<KeyValue>(list.size());
         this.keyValueMap = new HashMap<String, Object>(list.size());
         KeyValue kv;
         for (String kvString : list)
         {
            // TODO use regular expressions for splitting to allow escaped separator as part of String value
            String[] kvArray = kvString.split(PARTS_SEPARATOR); 
            if (kvArray.length == 2)
               kv = new KeyValueImpl(kvArray[0], kvArray[1]);
            else
               kv = new KeyValueImpl(kvArray[0], kvArray[1], kvArray[2]);
            this.keyValuePairs.add(kv);
            // check for complex types and process
            if (kv.getKey().contains(STRUCT_PATH_DELIMITER) && !serializable)
            {
               // TODO use regular expressions for splitting to allow escaped delimiter as part of String value
               String[] nameParts = kv.getKey().split(STRUCT_PATH_DELIMITER_REGEX); 
               if (!complexTypes.containsKey(nameParts[0]))
                  complexTypes.put(nameParts[0], new HashMap<String, Object>());
               Map<String, Object> complexType = complexTypes.get(nameParts[0]);
               complexType.put(nameParts[1], createTypedValue(kv.getType(), kv.getValue()));
            }
            else
            {
               if (serializable)
               {
                  if (kv.getKey().contains(STRUCT_PATH_DELIMITER))
                  {
                     String[] beanAndMethod = new String[2];
                     beanAndMethod[0] = kv.getKey().split(STRUCT_PATH_DELIMITER_REGEX)[0];
                     beanAndMethod[1] = kv.getKey().split(STRUCT_PATH_DELIMITER_REGEX)[1];
                     beanAndMethod[1] = beanAndMethod[1].split(SERIALIZABLE_METHOD_DELIMITER_REGEX)[0];
                     DataFragmentValue df = new DataFragmentValue(beanAndMethod[0], beanAndMethod[1]);
                     // this.keyValueMap.put(kv.getKey(), createTypedValue(kv.getType(),
                     // kv.getValue()));
                     this.keyValueMap.put(beanAndMethod[0], df);
                  }
               }
               else
                  this.keyValueMap.put(kv.getKey(), createTypedValue(kv.getType(), kv.getValue()));
            }
         }
         // add complex types
         if (complexTypes.size() > 0)
         {
            keyValueMap.putAll(complexTypes);
         }
      }
   }

   public static Object createTypedValue(String type, String value)
   {
      try
      {
         if ("long".equalsIgnoreCase(type))
         {
            return Long.parseLong(value);
         }
         else if ("int".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type))
         {
            return Integer.parseInt(value);
         }
         else if ("bool".equalsIgnoreCase(type) || "boolean".equalsIgnoreCase(type))
         {
            return Boolean.parseBoolean(value);
         }
         else if ("double".equalsIgnoreCase(type))
         {
            return Double.parseDouble(value);
         }
         else if ("byte".equalsIgnoreCase(type))
         {
            return Byte.parseByte(value);
         }
         else if ("float".equalsIgnoreCase(type))
         {
            return Float.parseFloat(value);
         }
         else if ("short".equalsIgnoreCase(type))
         {
            return Short.parseShort(value);
         }
         else if ("char".equalsIgnoreCase(type) || "character".equalsIgnoreCase(type))
         {
            if (value.length() > 0)
               return value.toCharArray()[0];
         }
         else if ("date".equalsIgnoreCase(type))
         {
            return DATE_FORMAT.parse(value);
         }
      }
      catch (Exception e)
      {
         log.error("Could not convert stringValue <" + value + "> to object type <" + type + ">.", e);
      }
      return value;
   }

   public Map<String, Object> getTypedMap()
   {
      return keyValueMap;
   }

   public static DateFormat getDateFormat()
   {
      return DATE_FORMAT;
   }

}
