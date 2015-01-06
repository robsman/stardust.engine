package org.eclipse.stardust.engine.extensions.camel.util.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
 * "Person.name::Peter" => Structured Data with ID 'Person' and inner path 'name' of
 * String type<br/>
 * Person.address[0]/city::$simple{header.city}==> set the value of exchange header city to the field city located in the first address
 * Person.a/b/c/cs[3]/c::$simple{header.c}
 * "Person.address/zip::65342::int" => Structured Data with ID 'Person' and inner path
 * 'address/zip' of Integer type
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

   private Map<String, Object> complexTypes = new HashMap<String,Object>();

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
            // TODO use regular expressions for splitting to allow escaped separator as
            // part of String value
            String[] kvArray = kvString.split(PARTS_SEPARATOR);
            if (kvArray.length == 2)
               kv = new KeyValueImpl(kvArray[0], kvArray[1]);
            else
               kv = new KeyValueImpl(kvArray[0], kvArray[1], kvArray[2]);
            this.keyValuePairs.add(kv);
            // check for complex types and process
            if (kv.getKey().contains(STRUCT_PATH_DELIMITER) && !serializable)
            {
               // TODO use regular expressions for splitting to allow escaped delimiter as
               // part of String value
               String[] nameParts = kv.getKey().split(STRUCT_PATH_DELIMITER_REGEX);
               if (nameParts.length == 2)
               {
                  Map<String, Object> existingMapping;
                  if (!complexTypes.containsKey(nameParts[0]))
                  {
                     complexTypes.put(
                           nameParts[0],
                           convertXpathToStructure(nameParts, null,
                                 createTypedValue(kv.getType(), kv.getValue())));
                  }
                  else
                  {
                     Object existingValue = complexTypes.get(nameParts[0]);
                     if (existingValue instanceof Map)
                     {
                        Map<String, Object> currentMapping = (Map) existingValue;
                        Map<String, Object> response=  convertXpathToStructure(nameParts,
                            currentMapping,
                            createTypedValue(kv.getType(), kv.getValue()));
                        ((Map)complexTypes.get(nameParts[0])).putAll((Map)response);
                        
                     }
                  }
               }
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
                     beanAndMethod[1] = beanAndMethod[1]
                           .split(SERIALIZABLE_METHOD_DELIMITER_REGEX)[0];
                     DataFragmentValue df = new DataFragmentValue(beanAndMethod[0],
                           beanAndMethod[1]);
                     this.keyValueMap.put(beanAndMethod[0], df);
                  }
               }
               else
                  this.keyValueMap.put(kv.getKey(),
                        createTypedValue(kv.getType(), kv.getValue()));
            }
         }
         // add complex types
         if (complexTypes.size() > 0)
         {
            keyValueMap.putAll(complexTypes);
         }
      }
   }

   private Map<String,Object> convertXpathToStructure(String[] nameParts,
         Map<String, Object> parentStructure, Object value)
   {
      if (nameParts.length != 2)
         throw new RuntimeException("Invalid path");
      Map<String, Object> stucture = new HashMap<String, Object>();

      if (!nameParts[1].contains("/") && !nameParts[1].contains("[") && !nameParts[1].contains("]"))
      {// first level: Person.firstName simple Element
         return handleNestedStructure(copyArray(nameParts, 1, nameParts.length),
               parentStructure, value);
      }else if(!nameParts[1].contains("/") && nameParts[1].contains("[") && nameParts[1].contains("]")){//first Level List
         
         return handleNestedStructure(copyArray(nameParts, 1, nameParts.length),
               parentStructure, value);
      }
      else
      {// multi levels : handle cases such as: customer/address/city
         String[] levels = nameParts[1].split("/");
         if (levels.length > 1)
         {
            if (!levels[0].contains("[") && !levels[0].contains("]"))
            {// simple Expression
               Map<String, Object> currentMapping;
               Object existingValue=null;
               if(parentStructure!=null)
                existingValue = parentStructure.get(levels[0]);
               
               if (existingValue != null && existingValue instanceof Map)
               {
                  currentMapping = (Map) existingValue;
               }
               else
               {
                  currentMapping = new HashMap<String, Object>();
               }
               Map<String, Object> response=handleNestedStructure(copyArray(levels, 1, levels.length),
                     (Map<String, Object>) currentMapping, value);
               Map output=new HashMap<String,Object>();
               output.put(levels[0], response);
               
               return output;
            }
            else
            {// handle list expression
               return handleNestedListStructure(levels, parentStructure, value);
            }
         }
      }
      return stucture;

   }

   private Map<String,Object> handleNestedListStructure(String[] levels,
         Map<String, Object> parentStructure, Object value)
   {
      List<Object> listField = new LinkedList<Object>();
      String fieldName = levels[0].substring(0, levels[0].indexOf("["));
      int index = Integer.parseInt(levels[0].substring(levels[0].indexOf("[") + 1,
            levels[0].indexOf("]")));
      Object[] elements = null;//
      if (index > 1)
      {
         elements = new Object[index];
        index--;
      }else {
         if(index==1){
            
            elements = new Object[2];
         }else{
         
         elements = new Object[1];
         }
         }
      if (parentStructure!=null &&parentStructure.containsKey(fieldName))
      {
         listField = (List<Object>) parentStructure.get(fieldName);
         elements = Arrays.copyOf(listField.toArray(), elements.length);
         if(!levels[0].contains("/") && levels.length==1)
            elements[index] =value;
         else
         elements[index] = handleNestedStructure(copyArray(levels, 1, levels.length),
               null, value);
         listField = Arrays.asList(elements);
         Map<String, Object> level = new HashMap<String, Object>();
         level.put(fieldName, listField);
         return level;
      }
      else
      {
         if(levels.length>1){
            elements[index] = handleNestedStructure(copyArray(levels, 1, levels.length),
               null, value);
         listField = Arrays.asList(elements);
         Map<String, Object> level = new HashMap<String, Object>();
         level.put(fieldName, listField);
         return level;
         }else{
            if(levels[0].contains("[") &&levels[0].contains("]"))
            {
               elements[index]=value;
               listField = Arrays.asList(elements);
               
               Map<String, Object> level = new HashMap<String, Object>();
               level.put(fieldName, listField);
               return level;
            }
         return null;
         }
      }
      
   }

   private Map<String,Object> handleNestedStructure(String[] nameParts,
         Map<String, Object> parentStructure, Object value)
   {
      Map<String, Object> nestedStructure = new HashMap<String, Object>();
      if(nameParts.length>0){
      if (nameParts.length == 1 && !nameParts[0].contains("[")
            && !nameParts[0].contains("]"))
      {
         if(parentStructure!=null ){
            nestedStructure=parentStructure;
         }
         nestedStructure.put(nameParts[0], value);
         return nestedStructure;
      }
      else
      {
         if (!nameParts[0].contains("[") && !nameParts[0].contains("]"))
         {
            Map<String, Object> level = new HashMap<String, Object>();
            level.put(
                  nameParts[0],
                  handleNestedStructure(copyArray(nameParts, 1, nameParts.length),
                        parentStructure, value));
            return level;
         }
         else
         {
            return handleNestedListStructure(nameParts, parentStructure, value);
         }
      }
      }
      return null;

   }

   private String[] copyArray(String[] array, int startIndex, int endIndex)
   {
      String[] copy = new String[endIndex - startIndex];
      for (int i = startIndex, j = 0; i < endIndex; i++, j++)
      {
         copy[j] = array[i];
      }
      return copy;
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
         log.error("Could not convert stringValue <" + value + "> to object type <"
               + type + ">.", e);
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
