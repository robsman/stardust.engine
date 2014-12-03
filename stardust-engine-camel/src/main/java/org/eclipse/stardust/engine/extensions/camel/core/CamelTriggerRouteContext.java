package org.eclipse.stardust.engine.extensions.camel.core;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_HEADERS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_MESSAGE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCUMENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCUMENT_LIST;
import static org.eclipse.stardust.engine.extensions.camel.Util.extractBodyMainType;
import static org.eclipse.stardust.engine.extensions.camel.Util.performParameterMapping;

import java.io.IOException;
import java.util.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;

public class CamelTriggerRouteContext extends TriggerRouteContext
{
   private static final Logger logger = LogManager
         .getLogger(CamelTriggerRouteContext.class);

   private static final Map<String, Class< ? >> primitiveClasses = new HashMap<String, Class< ? >>();
   static
   {
      primitiveClasses.put("long", Long.class);
      primitiveClasses.put("int", Integer.class);
      primitiveClasses.put("integer", Integer.class);
      primitiveClasses.put("bool", Boolean.class);
      primitiveClasses.put("boolean", Boolean.class);
      primitiveClasses.put("double", Double.class);
      primitiveClasses.put("byte", Byte.class);
      primitiveClasses.put("float", Float.class);
      primitiveClasses.put("short", Short.class);
      primitiveClasses.put("char", Character.class);
      primitiveClasses.put("character", Character.class);
      primitiveClasses.put("Timestamp", Date.class);
      
   }



   public CamelTriggerRouteContext(ITrigger trigger, String partitionId,
         String camelContextId)
   {
      this.trigger = trigger;
      this.partitionId = partitionId;
      this.camelContextId = camelContextId;
   }

   @Override
   public String getRouteId()
   {
      return Util.getRouteId(partitionId, getModelId(), getProcessId(), getId(), false);
   }

   /**
    * will contains the trigger mapping expressions
    * 
    * @return
    */
   public MappingExpression getMappingExpression()
   {
      MappingExpression mappingExpression = new MappingExpression();
      try
      {
         List<AccessPointProperties> accessPointList = performParameterMapping(this.trigger);
         buildMappingExpression(accessPointList, mappingExpression);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

      return mappingExpression;
   }

   /**
    * Always start route automatically
    * 
    * @return
    */
   public Boolean autoStartRoute()
   {

	   Boolean startup = true;
	   if (trigger.getAttribute("carnot:engine:camel::autoStartup") != null){
		   startup = (Boolean) trigger.getAttribute("carnot:engine:camel::autoStartup");
	   }
	  return startup;

   }

   /**
    * Will create the data mapping for ipp:process:start endpoint according to the trigger
    * configuration.
    * 
    * @param accessList
    * @param mappingExpression
    */
   private void buildMappingExpression(List<AccessPointProperties> accessList,
         MappingExpression mappingExpression)
   {
      if (accessList == null || accessList.isEmpty())
         return;

      mappingExpression.getBodyExpression().append("data=");
      for (AccessPointProperties accessPtProps : accessList)
      {
         // not the first data
         if (!mappingExpression.getBodyExpression().toString().endsWith("="))
            mappingExpression.getBodyExpression().append(",");

         mappingExpression.getBodyExpression().append(accessPtProps.getData().getId());

         if (!StringUtils.isEmpty(accessPtProps.getDataPath()))
            mappingExpression.getBodyExpression().append(
                  "." + accessPtProps.getDataPath());

         String bodyMainType = extractBodyMainType(accessPtProps.getData());
         mappingExpression.getBodyExpression().append("::$simple{");

         if (accessPtProps.getAccessPointLocation().equals(ACCESS_POINT_HEADERS))
         {
            String headerName = null;
            if (accessPtProps.getAccessPointPath() != null)
            {
               int startIndex = accessPtProps.getAccessPointPath().indexOf("get");
               int endIndex = accessPtProps.getAccessPointPath().lastIndexOf("()");
               headerName = accessPtProps.getAccessPointPath().substring(startIndex + 3,
                     endIndex);
            }

            if (accessPtProps.getAccessPointType().equals(DOCUMENT_LIST))
            {
               logger.warn("The Type " + DOCUMENT_LIST + " is not supported as header");
            }
            else if (accessPtProps.getAccessPointType().equals(DOCUMENT))
            {
               mappingExpression.getBodyExpression().append("headerAs(");
               mappingExpression.getBodyExpression().append(headerName);
               mappingExpression.getBodyExpression().append("\\," + bodyMainType + ")}");

               if(!this.getEventImplementation().equals(CamelConstants.GENERIC_CAMEL_ROUTE_EVENT))
               {
                  mappingExpression.getPostHeadersExpression().add(
                        "<setHeader headerName=\"" + headerName
                              + "\"><simple>$simple{body}</simple></setHeader>");
                  mappingExpression.setIncludeMoveEndpoint(true);
               }
            }
            else if (accessPtProps.getData().getType().getId().equals("struct"))
            {
               if (!StringUtils.isEmpty(accessPtProps.getDataPath())){
                  mappingExpression.getBodyExpression().append("header.");
                  mappingExpression.getBodyExpression().append(headerName);
                  mappingExpression.getBodyExpression().append("}");
               }else{
               mappingExpression.getBodyExpression().append("headerAs(");
               mappingExpression.getBodyExpression().append(headerName);
               mappingExpression.getBodyExpression().append("\\,java.util.Map)}");
               }
               mappingExpression.setIncludeConversionStrategy(true);
            }
            else if (accessPtProps.getData().getType().getId().equals("primitive"))
               mappingExpression.getBodyExpression().append(
                     "headerAs("
                           + headerName
                           + "\\,"
                           + getNonPrimitiveType(((Type) accessPtProps.getData()
                                 .getAttribute("carnot:engine:type")).getName()) + ")}");
            else if (accessPtProps.getData().getType().getId().equals("serializable")
                  && bodyMainType != null)
            {
               mappingExpression.getBodyExpression().append(
                     "headerAs(" + headerName + "\\,"
                           + bodyMainType.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                           + ")}");
            }
         }
         else if (accessPtProps.getAccessPointLocation().equals(ACCESS_POINT_MESSAGE))
         {
            if (accessPtProps.getAccessPointType().equals(DOCUMENT_LIST))
            { // Document
              // Lists
               logger.warn("Document List Type is not yet supported.");
            }
            else if (accessPtProps.getAccessPointType().equals(DOCUMENT))
            { // Document
                  mappingExpression.getBodyExpression().append("body");
                   mappingExpression.getBodyExpression().append("}");
                   return;
            }
            if (accessPtProps.getData().getType().getId().equals("struct"))
            {
               if (!StringUtils.isEmpty(accessPtProps.getDataPath()))
                  mappingExpression.getBodyExpression().append("body");
               else
               mappingExpression.getBodyExpression().append("bodyAs(java.util.Map)");
               mappingExpression.setIncludeConversionStrategy(true);
            }
            else if (accessPtProps.getData().getType().getId().equals("primitive"))
               mappingExpression.getBodyExpression().append(
                     "bodyAs("
                           + getNonPrimitiveType(((Type) accessPtProps.getData()
                                 .getAttribute("carnot:engine:type")).getName()) + ")");
            else if (accessPtProps.getData().getType().getId().equals("serializable"))
            {
               mappingExpression.getBodyExpression().append("bodyAs(");
               mappingExpression.getBodyExpression().append(bodyMainType + ")");
            }
            mappingExpression.getBodyExpression().append("}");
         }
      }
   }

   private static String getNonPrimitiveType(String type)
   {
      if (type.equals("String") || type.equals("java.lang.String"))
         return "java.lang.String";
      String nonPrimitivetype = primitiveClasses.get(type).getName();
      return nonPrimitivetype;

   }

//   private static List<DataConverter> getConverterForType(String type,
//         List<DataConverter> converters)
//   {
//      List<DataConverter> selectedConverters = new ArrayList<DataConverter>();
//      for (DataConverter converter : converters)
//      {
//         if (converter.getTargetType().equals(DOCUMENT))
//            selectedConverters.add(converter);
//      }
//      return selectedConverters;
//   }

}
