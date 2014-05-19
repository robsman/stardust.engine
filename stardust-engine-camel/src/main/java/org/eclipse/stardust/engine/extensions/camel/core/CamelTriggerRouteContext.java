package org.eclipse.stardust.engine.extensions.camel.core;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_HEADERS;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ACCESS_POINT_MESSAGE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCUMENT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCUMENT_LIST;
import static org.eclipse.stardust.engine.extensions.camel.Util.extractBodyMainType;

import java.io.IOException;
import java.util.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IParameterMapping;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.beans.*;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.extensions.camel.Util;
import org.eclipse.stardust.engine.extensions.camel.converter.DataConverter;
import org.eclipse.stardust.engine.extensions.camel.trigger.AccessPointProperties;
import org.eclipse.stardust.engine.extensions.camel.trigger.CamelTriggerRoute;

public class CamelTriggerRouteContext extends TriggerRouteContext
{
   private static final Logger logger = LogManager.getLogger(CamelTriggerRouteContext.class);
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
   }
   private MappingExpression mappingExpression=new MappingExpression();
   private List<DataConverter> dataConverters;
   
   public CamelTriggerRouteContext(ITrigger trigger, String partitionId,
         String camelContextId,List<DataConverter> dataConverters)
   {
      this.trigger = trigger;
      this.partitionId = partitionId;
      this.camelContextId = camelContextId;
      this.dataConverters=dataConverters;
   }

   @Override
   public String getRouteId()
   {
      return Util.getRouteId(partitionId, getModelId(), getProcessId(), getId(), false);
   }

   public MappingExpression getMappingExpression()
   {
      try
      {
         List<AccessPointProperties> accessPointList = performParameterMapping();
         buildMappingExpression(accessPointList, mappingExpression);
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return mappingExpression;
   }
   
   
   public boolean autoStartRoute(){
      return true;
   }
   
   
   @SuppressWarnings({"rawtypes"})
   private List<AccessPointProperties> performParameterMapping() throws IOException
   {
      Map<String, String> schemaRefs = new HashMap<String, String>();
      List<AccessPointProperties> accessPointList = new ArrayList<AccessPointProperties>();
      ModelElementList parameterMappings = this.trigger.getParameterMappings();

      Link typeBean = ((Link) ((ModelBean) this.trigger.getModel()).getTypeDeclarations());
      if (!typeBean.isEmpty())
      {

         Iterator iter = typeBean.iterator();
         while (iter.hasNext())
         {
            TypeDeclarationBean extType = (TypeDeclarationBean) iter.next();

            if (extType.getXpdlType() instanceof ExternalReferenceBean)
            {
               ExternalReferenceBean xpdlType = ((ExternalReferenceBean) extType
                     .getXpdlType());
               schemaRefs.put(extType.getId(), xpdlType.getLocation().replace('.', '*'));

            }
            else
            {
               schemaRefs.put(extType.getId(),
                     "internal:" + ((ModelBean) this.trigger.getModel()).getId() + "::"
                           + extType.getId());
            }

            // the last replace is to overcome a camel bug in parsing of bean
            // name when the
            // method/param
            // contains a . character

         }
      }
      else
      {// check for external references
         for (Object parameter : parameterMappings)
         {
            ParameterMappingBean parameterMapping = (ParameterMappingBean) parameter;
            if (parameterMapping != null && parameterMapping.getData() != null)
            {
               if (parameterMapping.getData().getExternalReference() != null)
               {
                  IReference ref = parameterMapping.getData().getExternalReference();

                  schemaRefs.put(ref.getId(), "reference:"
                        + ref.getExternalPackage().getReferencedModel().getId() + "::"
                        + ref.getId());
               }
               else if ((parameterMapping.getData().getStringAttribute(
                     "carnot:engine:dataType") != null)
                     && (((ModelBean) parameterMapping.getData().getParent()).getId() != null))
               {

                  schemaRefs.put(
                        parameterMapping.getData().getStringAttribute(
                              "carnot:engine:dataType"),
                        "reference:"
                              + ((ModelBean) parameterMapping.getData().getParent())
                                    .getId()
                              + "::"
                              + parameterMapping.getData().getStringAttribute(
                                    "carnot:engine:dataType"));
               }

            }
         }

      }

      for (int i = 0; i < parameterMappings.size(); ++i)
      {

         IParameterMapping mapping = (IParameterMapping) parameterMappings.get(i);
         AccessPointProperties accessPtProps = new AccessPointProperties();

         String outBodyAccesPoint = (String) this.trigger.getAllAttributes().get(
               "carnot:engine:camel::outBodyAccessPoint");
         if ((outBodyAccesPoint != null && outBodyAccesPoint.equalsIgnoreCase(mapping
               .getParameterId()))
               || (mapping != null && mapping.getParameterId() != null && mapping
                     .getParameterId().equalsIgnoreCase(ACCESS_POINT_MESSAGE)))
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_MESSAGE);
            accessPtProps.setAccessPointPath(mapping.getParameterPath());
         }
         else
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_HEADERS);
            accessPtProps
                  .setAccessPointPath("get"
                        + getOutAccessPointNameUsingDataMappingName((ParameterMappingBean) mapping)
                        + "()");
         }
         // accessPtProps.setAccessPointLocation(mapping.getParameterId());

         accessPtProps.setData(mapping.getData());
         accessPtProps.setDataPath(mapping.getDataPath());
         if (mapping.getData() != null
               && mapping.getData().getExternalReference() != null)
            accessPtProps.setXsdName(schemaRefs.get(mapping.getData()
                  .getExternalReference().getId()));
         else
         {

            accessPtProps.setXsdName(schemaRefs.get(mapping.getData().getStringAttribute(
                  "carnot:engine:dataType")));
         }
         /*
          * if(mapping.getData().getAllAttributes().containsKey(
          * "carnot:engine:dms:resourceMetadataSchema")) { if(! accessPtProps.
          * getAccessPointType().startsWith(STARDUST_ENGINE_CLASS))
          * accessPtProps.setAccessPointType(DOCUMENT_LIST); }
          */
         accessPtProps.setAccessPointType(mapping.getData().getType().getId());
         // accessPtProps.setEndPoint(endpoint);
         accessPointList.add(accessPtProps);
      }
      return accessPointList;
   }
   @SuppressWarnings("rawtypes")
   private String getOutAccessPointNameUsingDataMappingName(ParameterMappingBean mapping)
   {
      if (mapping != null
            && ((TriggerBean) mapping.getParent()).getAllOutAccessPoints() != null)
      {
         Iterator itr = ((TriggerBean) mapping.getParent()).getAllOutAccessPoints();
         while (itr.hasNext())
         {
            AccessPointBean accessPoint = (AccessPointBean) itr.next();
            if (accessPoint.getId().equalsIgnoreCase(mapping.getParameterId()))
            {
               return accessPoint.getName();
            }
         }

      }
      return null;
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

      mappingExpression.getBodyExpression().append("&amp;data=");
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

               mappingExpression.getPostHeadersExpression().add(
                     "<setHeader headerName=\"" + headerName
                           + "\"><simple>$simple{body}</simple></setHeader>");// As(org.eclipse.stardust.engine.api.runtime.Document)
               mappingExpression.setIncludeMoveEndpoint(true);
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
               List<DataConverter> availableConvertersForDocumentType = getConverterForType(
                     DOCUMENT_LIST, dataConverters);
               if (availableConvertersForDocumentType == null
                     || availableConvertersForDocumentType.isEmpty())
                  logger.warn("No Converters found for Access point of type "
                        + DOCUMENT_LIST);
               else if (availableConvertersForDocumentType != null
                     && availableConvertersForDocumentType.size() > 1)
                  logger.warn("Multiple Converters found for Access point of type "
                        + DOCUMENT_LIST);
               else
               {
                  mappingExpression.getBodyExpression().append("body");
                  mappingExpression.getBodyExpression().append("}");
               }
            }
            else if (accessPtProps.getAccessPointType().equals(DOCUMENT))
            { // Document
               List<DataConverter> availableConvertersForDocumentType = getConverterForType(
                     DOCUMENT, dataConverters);
               if (availableConvertersForDocumentType == null
                     || availableConvertersForDocumentType.isEmpty())
                  logger.warn("No Converters found for Access point of type " + DOCUMENT);
               else if (availableConvertersForDocumentType != null
                     && availableConvertersForDocumentType.size() > 1)
                  logger.warn("Multiple Converters found for Access point of type "
                        + DOCUMENT);
               else
               {
                  mappingExpression.getBodyExpression().append("body");
                  mappingExpression.setIncludeMoveEndpoint(true);
               }
            }
            if (accessPtProps.getData().getType().getId().equals("struct"))
            {
               if (!StringUtils.isEmpty(accessPtProps.getDataPath()))
                  mappingExpression.getBodyExpression().append("body");
               else
               mappingExpression.getBodyExpression().append("bodyAs(java.util.Map)");
               
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
   private List<DataConverter> getConverterForType(String type,
         List<DataConverter> converters)
   {
      List<DataConverter> selectedConverters = new ArrayList<DataConverter>();
      for (DataConverter converter : converters)
      {
         if (converter.getTargetType().equals(DOCUMENT))
            selectedConverters.add(converter);
      }
      return selectedConverters;
   }
}
