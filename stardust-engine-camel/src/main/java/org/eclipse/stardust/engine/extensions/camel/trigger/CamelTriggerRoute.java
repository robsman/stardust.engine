package org.eclipse.stardust.engine.extensions.camel.trigger;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ENDPOINT_TYPE_CLASS_ATT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.GENERIC_ENDPOINT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ROUTE_EXT_ATT;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.initializeEndpoint;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.replaceSymbolicEndpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IAccessPoint;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IParameterMapping;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.core.model.beans.ExternalReferenceBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.ParameterMappingBean;
import org.eclipse.stardust.engine.core.model.beans.TriggerBean;
import org.eclipse.stardust.engine.core.model.beans.TypeDeclarationBean;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;
import org.eclipse.stardust.engine.extensions.camel.EndpointHelper;
import org.eclipse.stardust.engine.extensions.camel.converter.DataConverter;
import org.eclipse.stardust.engine.extensions.camel.runtime.Endpoint;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.UndefinedEndpointException;

public class CamelTriggerRoute
{

   private static final Logger logger = LogManager.getLogger(CamelTriggerRoute.class);
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
   
   private static final String NEW_LINE = "\n";
   private static final String QUOTATION = "\"";
   private static final String ENDPOINT_PREFIX = "\n<to uri=\"";
   private static final String ENDPOINT_SUFFIX = "\"/>";
   private static final String ROUTE_START = "<route ";
   private static final String ROUTE_END = "</route>";

   private static final String ACCESS_POINT_MESSAGE = "message";

   private static final String ACCESS_POINT_HEADERS = "headers";

   private static final String DOCUMENT_LIST = "dmsDocumentList";

   private static final String DOCUMENT = "dmsDocument";

   private ITrigger trigger;
   private StringBuilder routeDefinition;
   private Endpoint endpoint;
   private List<AccessPointProperties> accessPointList = new ArrayList<AccessPointProperties>();
   private MappingExpression mappingExpression = new CamelTriggerRoute.MappingExpression();
   private List<DataConverter> dataConverters;
   private String partition;
   private String ctu;
   private String ctp;

   private static String getNonPrimitiveType(String type){
      if(type.equals("String") ||type.equals("java.lang.String"))
         return "java.lang.String";
     String nonPrimitivetype=primitiveClasses.get(type).getName();
     return nonPrimitivetype;
      
   }
   
   public CamelTriggerRoute(CamelContext camelContext, ITrigger trigger, List<DataConverter> converters,
         String partition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
         UndefinedEndpointException
   {
      this.trigger = trigger;
      this.dataConverters = converters;

      this.ctu = (String) this.trigger.getAllAttributes().get("carnot:engine:camel::username");
      this.ctp = (String) this.trigger.getAllAttributes().get("carnot:engine:camel::password");

      if (!StringUtils.isEmpty(partition))
      {
         this.partition = partition;
      }
      else
      {
         this.partition = Parameters.instance().getString(SecurityProperties.DEFAULT_PARTITION, "default");
      }

      String selectedEndpointTypeClass = (String) trigger.getAttribute(ENDPOINT_TYPE_CLASS_ATT);
      if (selectedEndpointTypeClass == null)
         selectedEndpointTypeClass = GENERIC_ENDPOINT;

      if (logger.isDebugEnabled())
      {
         logger.warn("Evaluate camel trigger route with enpoint : " + selectedEndpointTypeClass);
      }

      String processId = ((IProcessDefinition) trigger.getParent()).getId();
      String modelId = trigger.getModel().getId();
      String multiModelDeploymentId = null;
      if (!StringUtils.isEmpty(modelId) && !StringUtils.isEmpty(processId))
      {
         multiModelDeploymentId = "modelId=" + modelId + "&amp;processId=" + processId;
      }
      else
      {
         if (!StringUtils.isEmpty(processId))
         {
            multiModelDeploymentId = "processId=" + processId;
         }
      }

      if (selectedEndpointTypeClass != null && processId != null)
      {
         String providedRouteDefinition = (String) trigger.getAttribute(ROUTE_EXT_ATT);

         if (!StringUtils.isEmpty(providedRouteDefinition))
         {
            endpoint = initializeEndpoint(selectedEndpointTypeClass);

            performParameterMapping();
            buildMappingExpression(accessPointList);

            StringBuilder route = new StringBuilder();
            route.append(ROUTE_START);
            route.append("id=");
            route.append(QUOTATION);
            route.append(getRouteId(partition, modelId, processId, trigger.getId(), false));
            route.append(QUOTATION);

            route.append(" autoStartup=\"true\" >");

            StringBuilder replacementString = new StringBuilder();

            if (mappingExpression.getBeanExpression().size() > 0)
            {

               if (logger.isDebugEnabled())
               {
                  logger.debug("Adding converters to route.");
               }

               for (int i = 0; i < mappingExpression.getBeanExpression().size(); i++)
               {
                  String beanmapping = mappingExpression.getBeanExpression().get(i);

                  if (i == 0)
                  {
                     replacementString.append(beanmapping);
                  }
                  else
                  {
                     replacementString.append(ENDPOINT_PREFIX);
                     replacementString.append(beanmapping);
                  }

                  replacementString.append(ENDPOINT_SUFFIX);
               }

               replacementString.append(ENDPOINT_PREFIX);

            }

            replacementString.append("ipp:process:start?");
            replacementString.append(multiModelDeploymentId);
            replacementString.append(mappingExpression.getBodyExpression());
            replacementString.append(QUOTATION);

            String authenticationEndpoint = buildAuthenticationEndpoint(this.ctu, this.ctp);

            if (mappingExpression.getHeaderExpression().size() > 0)
            {

               if (logger.isDebugEnabled())
               {
                  logger.debug("Adding Headers to route.");
               }
               String headersFragment = "";
               for (String headerName : mappingExpression.getHeaderExpression().keySet())
               {
                  headersFragment += "<setHeader headerName=\"" + headerName + "\">\n";
                  headersFragment += "<simple>$simple{" + mappingExpression.getHeaderExpression().get(headerName)
                        + "}</simple>\n";
                  headersFragment += "</setHeader>\n";
               }
               authenticationEndpoint += headersFragment;
            }
            providedRouteDefinition = injectAuthenticationEndpoint(providedRouteDefinition, authenticationEndpoint);
            route.append(replaceSymbolicEndpoint(providedRouteDefinition, replacementString.toString()));
            route.append(NEW_LINE);
            route.append(ROUTE_END);

            if (logger.isDebugEnabled())
            {
               logger.debug("Generated route:" + route);
            }

            routeDefinition = route;
         }
      }

   }

   public StringBuilder getRouteDefinition()
   {
      return routeDefinition;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public Map getData()
   {

      Map data = new HashMap();
      Iterator _iterator = trigger.getAllAccessPoints();
      while (_iterator.hasNext())
      {
         IAccessPoint ap = (IAccessPoint) _iterator.next();
         if (null != ap)
         {
            data.put(ap.getId(), "");
         }
      }

      return data;
   }

   @SuppressWarnings({"rawtypes"})
   private void performParameterMapping() throws IOException
   {
      Map<String, String> schemaRefs = new HashMap<String, String>();

      ModelElementList parameterMappings = this.trigger.getParameterMappings();

      Link typeBean = ((Link) ((ModelBean) this.trigger.getModel()).getTypeDeclarations());
      if (!typeBean.isEmpty())
      {

         Iterator iter = typeBean.iterator();
         while (iter.hasNext())
         {
            TypeDeclarationBean extType = (TypeDeclarationBean) iter.next();
            schemaRefs.put(extType.getId(), extType.getXpdlType() instanceof ExternalReferenceBean
                  ? ((ExternalReferenceBean) extType.getXpdlType()).getLocation().split("/")[1].replace('.', '*')
                  : "internal:" + ((ModelBean) this.trigger.getModel()).getId() + "::" + extType.getId());
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
            schemaRefs.put(parameterMapping.getData().getStringAttribute("carnot:engine:dataType"), "reference:"
                  + ((ModelBean) parameterMapping.getData().getParent()).getId() + "::"
                  + parameterMapping.getData().getStringAttribute("carnot:engine:dataType"));
         }

      }

      for (int i = 0; i < parameterMappings.size(); ++i)
      {

         IParameterMapping mapping = (IParameterMapping) parameterMappings.get(i);
         AccessPointProperties accessPtProps = new AccessPointProperties();

         String outBodyAccesPoint = (String) this.trigger.getAllAttributes().get(
               "carnot:engine:camel::outBodyAccessPoint");
         if (outBodyAccesPoint != null && outBodyAccesPoint.equalsIgnoreCase(mapping.getParameterId()))
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_MESSAGE);
            accessPtProps.setAccessPointPath(mapping.getParameterPath());
         }
         else
         {
            accessPtProps.setAccessPointLocation(ACCESS_POINT_HEADERS);
            accessPtProps.setAccessPointPath("get"
                  + getOutAccessPointNameUsingDataMappingName((ParameterMappingBean) mapping) + "()");
         }
         // accessPtProps.setAccessPointLocation(mapping.getParameterId());

         accessPtProps.setData(mapping.getData());
         accessPtProps.setDataPath(mapping.getDataPath());
         accessPtProps.setXsdName(schemaRefs.get(mapping.getData().getStringAttribute("carnot:engine:dataType")));
         /*
          * if(mapping.getData().getAllAttributes().containsKey(
          * "carnot:engine:dms:resourceMetadataSchema")) { if(! accessPtProps.
          * getAccessPointType().startsWith(STARDUST_ENGINE_CLASS))
          * accessPtProps.setAccessPointType(DOCUMENT_LIST); }
          */
         accessPtProps.setAccessPointType(mapping.getData().getType().getId());
         accessPtProps.setEndPoint(endpoint);
         accessPointList.add(accessPtProps);
      }
   }

   private String getOutAccessPointNameUsingDataMappingName(ParameterMappingBean mapping)
   {
      if (mapping != null && ((TriggerBean) mapping.getParent()).getAllOutAccessPoints() != null)
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

   private void buildMappingExpression(List<AccessPointProperties> accessList)
   {
      if (accessList == null || accessList.isEmpty())
         return;

      mappingExpression.getBodyExpression().append("&amp;data=");
      for (AccessPointProperties accessPtProps : accessList)
      {
         if (!mappingExpression.getBodyExpression().toString().endsWith("=")) // not
            // the
            // first
            // data
            mappingExpression.getBodyExpression().append(",");
         mappingExpression.getBodyExpression().append(accessPtProps.getData().getId());

         if (!StringUtils.isEmpty(accessPtProps.getDataPath()))
            mappingExpression.getBodyExpression().append("." + accessPtProps.getDataPath());

         String bodyPathType = extractBodyPathType(accessPtProps.getDataPath());
         String bodyMainType = extractBodyMainType(accessPtProps.getData());
         mappingExpression.getBodyExpression().append("::$simple{");

         if (accessPtProps.getAccessPointLocation().equals(ACCESS_POINT_HEADERS))
         {
            String headerName = null;
            if (accessPtProps.getAccessPointPath() != null)
            {
               int startIndex = accessPtProps.getAccessPointPath().indexOf("get");
               int endIndex = accessPtProps.getAccessPointPath().lastIndexOf("()");
               headerName = accessPtProps.getAccessPointPath().substring(startIndex + 3, endIndex);
            }
            
            if (accessPtProps.getAccessPointType().equals(DOCUMENT_LIST))
            {
               logger.warn("The Type "+DOCUMENT_LIST+" is not supported as header");
            }
            else if (accessPtProps.getAccessPointType().equals(DOCUMENT))
            {
               mappingExpression.getBodyExpression().append("headerAs(");
               mappingExpression.getBodyExpression().append(headerName);
               mappingExpression.getBodyExpression().append("\\," + bodyMainType + ")}");
            }
            else if (accessPtProps.getData().getType().getId().equals("struct"))
            {
               mappingExpression.getBodyExpression().append("headerAs(");
               mappingExpression.getBodyExpression().append(headerName);
               mappingExpression.getBodyExpression().append("\\,java.util.Map)}");
//               mappingExpression.getHeaderExpression().put(
//                     headerName,
//                     "bean:sdtFileConverter?method=genericXSDToSDT(&quot;" + accessPtProps.getXsdName()
//                           + "&quot; , &quot;" + accessPtProps.getData().getStringAttribute("carnot:engine:dataType")
//                           + "&quot;)");
//               
//
//               // mappingExpression.getBeanExpression().add("bean:structuredDataTranslator?method=convert(\""
//               // +
//               // accessPtProps.getData().getStringAttribute("carnot:engine:dataType")
//               // + "\" , \"$simple{body}\")");
//               // mappingExpression.getBodyExpression().append("bodyAs(java.util.Map)");
//               mappingExpression.getBodyExpression().append("header."+headerName+"}");
            }
            else if (accessPtProps.getData().getType().getId().equals("primitive"))
               mappingExpression.getBodyExpression().append("headerAs("+headerName+"\\,"+getNonPrimitiveType( ((Type)accessPtProps.getData().getAttribute("carnot:engine:type")).getName())+")}");
            else if (accessPtProps.getData().getType().getId().equals("serializable") && bodyMainType!=null)
            {
//               mappingExpression.getBodyExpression().append("bodyAs(");
//               mappingExpression.getBodyExpression().append(bodyMainType + ")");
               mappingExpression.getBodyExpression().append("headerAs("+headerName+"\\,"+bodyMainType.replaceAll("<", "&lt;").replaceAll(">", "&gt;")+")}");
            }

            // if (bodyMainType != null)
            // mappingExpression.getHeaderExpression().put("IsSerializable", "true");
            // mappingExpression.getBodyExpression().append("bodyAs(");
            // mappingExpression.getBodyExpression().append(bodyPathType + ")");

            // mappingExpression.getHeaderExpression().put(headerName + "_Converted",
            // "headersAs(" + headerName + "," + bodyMainType + ")");
            // if (bodyPathType == null && bodyMainType == null)
            // mappingExpression.getBodyExpression().append("header.");
            // else
            // mappingExpression.getBodyExpression().append("headerAs(");
            //
            //
            //
            // if (bodyPathType != null)
            // mappingExpression.getBodyExpression().append("," + bodyPathType + ")");
            // else if (bodyMainType != null)
            // mappingExpression.getBodyExpression().append("," + bodyMainType + ")");
            // mappingExpression.getBodyExpression().append("}");
            // /*
            // * if(bodyType != null) mappingExpression.getBodyExpression().append("::"
            // +
            // * bodyType + "::");$
            // */

         }
         else if (accessPtProps.getAccessPointLocation().equals(ACCESS_POINT_MESSAGE))
         {
            if (accessPtProps.getAccessPointType().equals(DOCUMENT_LIST))
            { // Document
              // Lists
               List<DataConverter> availableConvertersForDocumentType = getConverterForType(DOCUMENT_LIST,
                     dataConverters);
               if (availableConvertersForDocumentType == null || availableConvertersForDocumentType.isEmpty())
                  logger.warn("No Converters found for Access point of type " + DOCUMENT_LIST);
               else if (availableConvertersForDocumentType != null && availableConvertersForDocumentType.size() > 1)
                  logger.warn("Multiple Converters found for Access point of type " + DOCUMENT_LIST);
               else
               {
                  for (DataConverter converter : dataConverters)
                  {
                     // if
                     // (//converter.getFromEndpoint().equals(accessPtProps.getEndPoint().getClass().getCanonicalName())
                     // //&&
                     // converter.getTargetType().equals(DOCUMENT_LIST))
                     // {
                     mappingExpression.getBeanExpression().add("bean:" + converter.getClass().getCanonicalName());
                     // break;
                     // }
                  }
                  mappingExpression.getBodyExpression().append("body");
                  mappingExpression.getBodyExpression().append("}");
                  // return;
               }
            }
            else if (accessPtProps.getAccessPointType().equals(DOCUMENT))
            { // Document
               List<DataConverter> availableConvertersForDocumentType = getConverterForType(DOCUMENT, dataConverters);
               if (availableConvertersForDocumentType == null || availableConvertersForDocumentType.isEmpty())
                  logger.warn("No Converters found for Access point of type " + DOCUMENT);
               else if (availableConvertersForDocumentType != null && availableConvertersForDocumentType.size() > 1)
                  logger.warn("Multiple Converters found for Access point of type " + DOCUMENT);
               else
               {
                  for (DataConverter converter : availableConvertersForDocumentType)
                  {
                     // if
                     // (//converter.getFromEndpoint().equals(accessPtProps.getEndPoint().getClass().getCanonicalName())
                     // //&&
                     // //converter.getTargetType().equals(DOCUMENT))
                     // {
                     mappingExpression.getBeanExpression().add("bean:" + converter.getClass().getCanonicalName());
                     // break;
                     // }
                  }
                  mappingExpression.getBodyExpression().append("body");
                  // mappingExpression.getBodyExpression().append("}");
                  // return;
               }
            }
            if (bodyPathType == null)
            {
               if (accessPtProps.getData().getType().getId().equals("struct"))
               {
                  mappingExpression.getBeanExpression().add(
                        "bean:sdtFileConverter?method=genericXSDToSDT(&quot;" + accessPtProps.getXsdName()
                              + "&quot; , &quot;"
                              + accessPtProps.getData().getStringAttribute("carnot:engine:dataType") + "&quot;)");
                  // mappingExpression.getBeanExpression().add("bean:structuredDataTranslator?method=convert(\""
                  // +
                  // accessPtProps.getData().getStringAttribute("carnot:engine:dataType")
                  // + "\" , \"$simple{body}\")");
                  mappingExpression.getBodyExpression().append("bodyAs(java.util.Map)");
               }
               else if (accessPtProps.getData().getType().getId().equals("primitive"))
                  mappingExpression.getBodyExpression().append("bodyAs(java.lang.String)");
               else if (accessPtProps.getData().getType().getId().equals("serializable"))
               {
                  mappingExpression.getBodyExpression().append("bodyAs(");
                  mappingExpression.getBodyExpression().append(bodyMainType + ")");
               }
            }
            else
            {
               if (bodyMainType != null)
                  mappingExpression.getHeaderExpression().put("IsSerializable", "true");
               mappingExpression.getBodyExpression().append("bodyAs(");
               mappingExpression.getBodyExpression().append(bodyPathType + ")");
            }
            mappingExpression.getBodyExpression().append("}");
         }
      }
      // }
   }

   private List<DataConverter> getConverterForType(String type, List<DataConverter> converters)
   {
      List<DataConverter> selectedConverters = new ArrayList<DataConverter>();
      for (DataConverter converter : converters)
      {
         if (converter.getTargetType().equals(DOCUMENT))
            selectedConverters.add(converter);
      }
      return selectedConverters;
   }

   private String extractBodyPathType(String bodyExpression)
   {
      String bodyType = null;
      if (bodyExpression == null)
         return bodyType;
      Pattern bodyPattern = Pattern.compile("\\(.+?\\)");
      Matcher bodyMatcher = bodyPattern.matcher(bodyExpression);
      if (bodyMatcher.find())
      {
         bodyType = bodyMatcher.group().split("\\s+")[0];
         bodyType = bodyType.substring(1, bodyType.length() - 1);
      }
      return bodyType;
   }

   private String extractBodyMainType(IData data)
   {
      return (String) data.getAttribute("carnot:engine:className");
   }

   private String buildAuthenticationEndpoint(String user, String password)
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("<setHeader headerName=\"");
      buffer.append(CamelConstants.MessageProperty.ORIGIN);
      buffer.append("\">");
      buffer.append("<constant>");
      buffer.append(CamelConstants.OriginValue.TRIGGER_CONSUMER);
      buffer.append("</constant>");
      buffer.append(" </setHeader>");

      buffer.append("<setHeader headerName=\"ippPassword\">");
      buffer.append("<constant>" + EndpointHelper.sanitizeUri(password) + "</constant>");
      buffer.append(" </setHeader>");

      buffer.append("<setHeader headerName=\"ippUser\">");
      buffer.append("<constant>" + EndpointHelper.sanitizeUri(user) + "</constant>");
      buffer.append(" </setHeader>");

      buffer.append("<setHeader headerName=\"ippPartition\">");
      buffer.append("<constant>" + this.partition + "</constant>");
      buffer.append(" </setHeader>");

      buffer.append("<to uri=\"ipp:authenticate:setCurrent\"/>");
      // buffer.append(user);
      // //buffer.append("&amp;password=");
      // buffer.append(password);
      // buffer.append("&amp;partition=");
      // buffer.append(this.partition);
      // buffer.append("\"/>");

      return buffer.toString();
   }

   private String injectAuthenticationEndpoint(String routeDefinition, String authenticationEndpoint)
   {
      int fromStartIndex = routeDefinition.indexOf("<from");
      String fromEndpoint = routeDefinition.substring(fromStartIndex);
      int fromEndIndex = fromEndpoint.indexOf(">") + 1;
      fromEndpoint = fromEndpoint.substring(0, fromEndIndex);

      String routeDefinitionEndpoints = routeDefinition.substring(fromEndIndex);

      StringBuffer buffer = new StringBuffer();
      buffer.append(fromEndpoint);
      buffer.append("<transacted ref=\"required\"/>"); // TODO : make it more visible
      buffer.append(authenticationEndpoint);
      buffer.append(routeDefinitionEndpoints);

      return buffer.toString();
   }

   private static class MappingExpression
   {
      private StringBuffer bodyExpression = new StringBuffer();
      private List<String> beanExpression = new ArrayList<String>();
      private List<String> postExpression = new ArrayList<String>();
      private Map<String, String> headerExpression = new HashMap<String, String>();

      public StringBuffer getBodyExpression()
      {
         return bodyExpression;
      }

      public List<String> getBeanExpression()
      {
         return beanExpression;
      }

      public List<String> getPostExpression()
      {
         return postExpression;
      }

      public Map<String, String> getHeaderExpression()
      {
         return headerExpression;
      }
   }
}
