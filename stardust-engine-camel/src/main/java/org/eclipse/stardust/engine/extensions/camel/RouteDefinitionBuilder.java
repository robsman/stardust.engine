package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.BLANK_SPACE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ENDPOINT_PREFIX;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ENDPOINT_SUFFIX;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.GREATER_THAN_SIGN;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.LESS_THAN_SIGN;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.NEW_LINE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.QUOTATION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTE_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.ORIGIN;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PARTITION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.PASSWORD;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.USER;
import static org.eclipse.stardust.engine.extensions.camel.Util.getRouteId;
import static org.eclipse.stardust.engine.extensions.camel.Util.replaceSymbolicEndpoint;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * This class is able to produce xml configuration for Camel. the configuration is a route
 * configuration based on the configuration provided in the Camel Trigger or Camel
 * Application type (Producer/Consumer)
 *
 */
public class RouteDefinitionBuilder
{
   private static final Logger logger = LogManager.getLogger(RouteDefinitionBuilder.class.getCanonicalName());

   /**
    * Creates the route configuration for a Camel trigger.
    *
    * @return
    */
   public static StringBuilder createRouteDefintionForCamelTrigger(String providedRouteDefinition, String partitionId,
         String modelId, String processId, String triggerName, String userName, String password,
         MappingExpression mappingExpression)
   {
      StringBuilder route = new StringBuilder();

      route.append(route(getRouteId(partitionId, modelId, processId, triggerName, false), true));

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
      replacementString.append(buildMultiModelDeploymentId(modelId, processId));
      replacementString.append(mappingExpression.getBodyExpression());
      replacementString.append(QUOTATION);

      String authenticationEndpoint = buildHeadersExpression(partitionId, userName, password, mappingExpression);

      if (mappingExpression.getHeaderExpression().size() > 0)
      {

         if (logger.isDebugEnabled())
         {
            logger.debug("Adding Headers to route.");
         }
         String headersFragment = "";
         for (String headerName : mappingExpression.getHeaderExpression().keySet())
         {
            headersFragment += setHeader(headerName,
                  buildSimpleExpression(mappingExpression.getHeaderExpression().get(headerName)));
         }
         authenticationEndpoint += headersFragment;
      }
      providedRouteDefinition = injectAuthenticationEndpoint(providedRouteDefinition, authenticationEndpoint);
      route.append(replaceSymbolicEndpoint(providedRouteDefinition, replacementString.toString()));
      route.append(NEW_LINE);
      route.append(SPRING_XML_ROUTE_FOOTER);
      return route;
   }

   /**
    * Creates the Xml configuration of a consumer Route
    *
    * @param routeId
    * @param partition
    * @param modelId
    * @param applicationId
    * @param camelContextId
    * @param providedRouteConfig
    * @return
    */
   public static String createConsumerXmlConfiguration(ConsumerRouteContext routeContext)
   {
      String routeId = routeContext.getRouteId();
      String providedRouteConfig = routeContext.getUserProvidedRouteConfiguration();
      String partition = routeContext.getPartitionId();
      String modelId = routeContext.getModelId();
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
      routeDefinition.append(route(routeId, true));
      Boolean consumerBpmTypeConverter =  routeContext.getConsumerBpmTypeConverter();
      if (!StringUtils.isEmpty(providedRouteConfig))
      {

         if (providedRouteConfig.contains(CamelConstants.IPP_AUTHENTICATE_TAG))
         {

            String[] parts = providedRouteConfig.split(CamelConstants.IPP_AUTHENTICATE_TAG);

            if (parts.length == 2)
            {
               int indexOpenTag = parts[0].lastIndexOf("<");
               int indexCloseTag = parts[1].indexOf("/>");

               String before = parts[0].substring(0, indexOpenTag);
               String after = parts[1].substring(indexCloseTag + 2);

               routeDefinition.append(before);

               routeDefinition.append(setHeader(CamelConstants.MessageProperty.ORIGIN,
                     buildConstantExpression(CamelConstants.OriginValue.APPLICATION_CONSUMER)));
               if (!StringUtils.isEmpty(partition))
                  routeDefinition.append(setHeader(CamelConstants.MessageProperty.PARTITION,
                        buildConstantExpression(partition)));
               if (!StringUtils.isEmpty(modelId))
                  routeDefinition.append(setHeader(CamelConstants.MessageProperty.MODEL_ID,
                        buildConstantExpression(modelId)));
               if (!StringUtils.isEmpty(routeId))
                  routeDefinition.append(setHeader(CamelConstants.MessageProperty.ROUTE_ID,
                        buildConstantExpression(routeId)));// getRouteId(partition,
                                                           // modelId, null,
                                                           // applicationId, false)

               // TODO : Make it more visible (UI)
               routeDefinition.append(transacted("required"));
               routeDefinition.append("<to uri=\"");
               routeDefinition.append(CamelConstants.IPP_AUTHENTICATE_TAG);
               routeDefinition.append(parts[1].replace(after, ""));

               providedRouteConfig = after;
            }
         }

         if (providedRouteConfig.contains(CamelConstants.IPP_DIRECT_TAG))
         {

            String[] parts = providedRouteConfig.split(CamelConstants.IPP_DIRECT_TAG);

            if (parts.length == 2)
            {
               int indexOpenTag = parts[0].lastIndexOf("<");
               int indexCloseTag = parts[1].indexOf("/>");

               String before = parts[0].substring(0, indexOpenTag);
               String after = parts[1].substring(indexCloseTag + 2);

               routeDefinition.append(before);
               routeDefinition.append(to("ipp:activity:find?expectedResultSize=1&dataFiltersMap=$simple{header."
                     + CamelConstants.MessageProperty.DATA_MAP_ID + "}"));
               routeDefinition.append(to("ipp:activity:complete"));
               routeDefinition.append(after);
            }
            else
            {
               throw new RuntimeException("More than one ipp:direct tag specified.");
            }
         }
         else
         {
            routeDefinition.append(providedRouteConfig);
         }

         if(consumerBpmTypeConverter != null && Boolean.TRUE.equals(consumerBpmTypeConverter)) {
             String tmpRoute = routeDefinition.toString();
             routeDefinition.delete(0, routeDefinition.length());
             routeDefinition.append(injectConsumerBpmTypeConverter(routeContext, tmpRoute));
         }

      }
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTE_FOOTER);
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_FOOTER);

      if (logger.isDebugEnabled())
      {
         logger.debug("Route " + routeId + " to be added to context " + routeContext.getCamelContextId()
               + " for partition " + partition);
      }

      return routeDefinition.toString().replace("&", "&amp;");
   }

   /**
    * Creates the route configuration of a Producer Route
    *
    * @param providedRoute
    * @param routeId
    * @param applicationId
    * @param partition
    * @param contextName
    * @return
    * @throws ValidationException
    */
   public static String createProducerXmlConfiguration(ProducerRouteContext routeContext)
   {
      String providedRoute = routeContext.getUserProvidedRouteConfiguration();
      String applicationId = routeContext.getApplicationId();
      boolean producerBpmTypeConverter=routeContext.getProducerBpmTypeConverter();
      String routeId = routeContext.getRouteId();
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
      routeDefinition.append(route(routeId, true));
      // TODO: define the behavior when a required parameter is missing
      if (StringUtils.isEmpty(applicationId))
         logger.error("Application ID is missing");

      if (!StringUtils.isEmpty(providedRoute) && !StringUtils.isEmpty(applicationId))
      {
         if (providedRoute.contains("<from"))
         {
            throw new RuntimeException("From element should not be present in the route configuration");
         }

         String endpointName = "direct://" + applicationId;

         if (!providedRoute.contains("<from"))
         {
            routeDefinition.append(from(endpointName));
            // TODO : Make it more visible (UI)
            routeDefinition.append(transacted("required"));
            if (routeContext.addApplicationAttributesToHeaders() || routeContext.addProcessContextHeaders())
               routeDefinition.append(process("mapAppenderProcessor"));

	         if(Boolean.TRUE.equals(producerBpmTypeConverter)) {
	             routeDefinition.append(injectProducertBpmTypeConverter(routeContext, providedRoute));
	         }
	         else {
	             routeDefinition.append(providedRoute);
	         }
         }
      }

      routeDefinition.append(SPRING_XML_ROUTE_FOOTER);
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_FOOTER);

      if (logger.isDebugEnabled())
      {
         logger.debug("Route " + routeId + " to be added to context " + routeContext.getCamelContextId()
               + " for partition " + routeContext.getPartitionId());
      }
      return routeDefinition.toString();
   }


   private static String injectProducertBpmTypeConverter(ProducerRouteContext routeContext, String route) {
      String producerOutboundConversionMethodName = routeContext.getProducerOutboundConversion();
      String producerInboundConversionMethodName  = routeContext.getProducerInboundConversion();

      StringBuffer buffer = new StringBuffer();
      if(producerOutboundConversionMethodName != null && !producerOutboundConversionMethodName.equals("None")) {
          String injectedOutboundConversionRoute = LESS_THAN_SIGN
                   + "to uri=\"ipp:data:"
                   + producerOutboundConversionMethodName + "\" /"
                   + GREATER_THAN_SIGN;
          int firstToEndpointIndex = route.indexOf("<to");
          if(firstToEndpointIndex > 0) {
              String beforefirstToEndpoint = route.substring(0, firstToEndpointIndex);
              if(beforefirstToEndpoint.contains("setHeader")) {
                  buffer.append(route.substring(0, firstToEndpointIndex));
                  buffer.append(injectedOutboundConversionRoute);
                  buffer.append(route.substring(firstToEndpointIndex, route.length()));
              }
          }

          else {
              buffer.append(injectedOutboundConversionRoute);
              buffer.append(route);
          }
      }

      if(producerInboundConversionMethodName != null && !producerInboundConversionMethodName.equals("None")) {
           String injectedInboundConversionRoute = LESS_THAN_SIGN
                   + "to uri=\"ipp:data:"
                   + producerInboundConversionMethodName + "\" /"
                   + GREATER_THAN_SIGN;

         if(!StringUtils.isEmpty(buffer.toString())) {
             buffer.append(injectedInboundConversionRoute) ;
         }
         else {
             buffer.append(route);
             buffer.append(injectedInboundConversionRoute) ;
         }
      }

      if(!StringUtils.isEmpty(buffer.toString())) {
          return buffer.toString();
      }
      return route;
  }

   private static String injectConsumerBpmTypeConverter(ConsumerRouteContext routeContext, String route) {
       String consumerInboundConversionMethodName = routeContext.getConsumerInboundConversion(); //(String) application.getAttribute(CamelConstants.CONSUMER_INBOUND_CONVERSION);
       if(consumerInboundConversionMethodName != null && !consumerInboundConversionMethodName.equals("None")) {
            String injectedInboundConversionRoute = LESS_THAN_SIGN
                    + "to uri=\"ipp:data:"
                    + consumerInboundConversionMethodName + "\" /"
                    + GREATER_THAN_SIGN;
            int toUriCompleteActivityIndex = getToUriCompleteActivityIndex(route);
            StringBuffer buffer = new StringBuffer();
            buffer.append(route.substring(0, toUriCompleteActivityIndex));
            buffer.append(injectedInboundConversionRoute);
            buffer.append(route.substring(toUriCompleteActivityIndex, route.length()));
            return buffer.toString();
       }
       return route;
   }

   private static int getToUriCompleteActivityIndex(String route)
   {
       int uriCompleteActivityIndex = route.indexOf("ipp:activity:complete");
       if(uriCompleteActivityIndex == -1) {
           throw new RuntimeException("Tag ipp:activity:complete  is not specified.");
       }
       String beforeUriCompleteActivityIndex = route.substring(0,uriCompleteActivityIndex);
       return beforeUriCompleteActivityIndex.lastIndexOf("<");
   }

   private static String removeMultipleSpaceChracters(String routeDefinition){
      if(routeDefinition.contains("  "))
         return cleanRoute(routeDefinition.replace("  ", " "));
      return routeDefinition;
   }

   private static String cleanElement(String routeDefinition){
      if(routeDefinition.contains(" /"))
         return cleanRoute(routeDefinition.replace(" /", "/"));
      return routeDefinition;
   }

   private static String cleanRoute(String routeDefinition){
      routeDefinition= removeMultipleSpaceChracters(routeDefinition);
      routeDefinition  = cleanElement(routeDefinition);
   return routeDefinition;

}

   private static String appendAuthenticationHeaders(String routeDefinition, String authenticationHeaders){
      String ippDirectElement="<to uri=\"ipp:direct\"/>";
      return routeDefinition.replace(ippDirectElement, authenticationHeaders+ippDirectElement);
   }
   private static String injectAuthenticationEndpoint(String routeDefinition, String authenticationEndpoint)
   {

      //Insert authentication headers just before ipp:direct to avoid exception at runtime.
      String providedRouteDefinition =cleanRoute(routeDefinition);
      providedRouteDefinition= appendAuthenticationHeaders(providedRouteDefinition,authenticationEndpoint);

      int fromStartIndex = providedRouteDefinition.indexOf("<from");
      String fromEndpoint = providedRouteDefinition.substring(fromStartIndex);
      int fromEndIndex = fromEndpoint.indexOf(">") + 1;
      fromEndpoint = fromEndpoint.substring(0, fromEndIndex);

      String routeDefinitionEndpoints = providedRouteDefinition.substring(fromEndIndex);

      StringBuffer buffer = new StringBuffer();
      buffer.append(fromEndpoint);
      buffer.append(transacted("required")); // TODO : make it more visible
     // buffer.append(authenticationEndpoint);
      buffer.append(routeDefinitionEndpoints);

      return buffer.toString();
   }

   private static String buildHeadersExpression(String partitionId, String user, String password,
         MappingExpression mappingExpression)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(buildAuthenticationEndpoint(partitionId, user, password));
      buffer.append(insertPostHeaders(mappingExpression));
      return buffer.toString();
   }

   /**
    * Add some headers properties(origin, password, user, partition) the route.
    *
    * @param partition
    * @param user
    * @param password
    * @return
    */
   private static String buildAuthenticationEndpoint(String partition, String user, String password)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(setHeader(ORIGIN, buildConstantExpression(CamelConstants.OriginValue.TRIGGER_CONSUMER)));
      buffer.append(setHeader(PASSWORD, buildConstantExpression(EndpointHelper.sanitizeUri(password))));
      buffer.append(setHeader(USER, buildConstantExpression(EndpointHelper.sanitizeUri(user))));
      buffer.append(setHeader(PARTITION, buildConstantExpression(partition)));
      buffer.append(to("ipp:authenticate:setCurrent"));
      return buffer.toString();
   }

   /**
    * Returns an XML representation of Route Element.
    *
    * @param id
    * @param autoStartUp
    *           flag
    * @return route
    */
   private static String route(String id, boolean autoStartUp)
   {
      StringBuilder route = new StringBuilder(LESS_THAN_SIGN + "route");

      if (!StringUtils.isEmpty(id))
      {
         route.append(BLANK_SPACE);
         route.append("id=\"" + id + "\"");
      }
      if (autoStartUp)
      {
         route.append(BLANK_SPACE);
         route.append("autoStartup=\"true\"");
      }
      else
      {
         route.append(BLANK_SPACE);
         route.append("autoStartup=\"false\"");
      }

      route.append(GREATER_THAN_SIGN);
      return route.toString();
   }

   private static String from(final String uri)
   {
      StringBuilder from = new StringBuilder();
      from.append(LESS_THAN_SIGN + "from uri=\"" + uri + "\" /" + GREATER_THAN_SIGN);
      return from.toString();
   }

   /**
    * Returns an XML representation of to Element. The generated XML is as below:
    * <p>
    * &lt;<to uri="URI " /&gt;
    * </p>
    *
    * @param headerProperty
    * @param constant
    * @return
    */
   private static String to(final String uri)
   {
      StringBuilder to = new StringBuilder();
      to.append(LESS_THAN_SIGN + "to uri=\"" + uri + "\" /" + GREATER_THAN_SIGN);
      return to.toString();
   }

   private static String process(final String beanId)
   {
      StringBuilder process = new StringBuilder();
      process.append(LESS_THAN_SIGN + "process ref=\"" + beanId + "\" /" + GREATER_THAN_SIGN);
      return process.toString();
   }

   private static String transacted(final String tranId)
   {
      StringBuilder transacted = new StringBuilder();
      transacted.append(LESS_THAN_SIGN + "transacted ref=\"" + tranId + "\" /" + GREATER_THAN_SIGN);
      return transacted.toString();
   }

   private static String buildConstantExpression(String expression)
   {
      StringBuffer exp = new StringBuffer();
      exp.append("<constant>" + expression + "</constant>");
      return exp.toString();
   }

   private static String buildSimpleExpression(String expression)
   {
      StringBuffer exp = new StringBuffer();
      exp.append("<simple>$simple{" + expression + "}</simple>\n");
      return exp.toString();
   }

   private static String setHeader(String headerName, String headerValue)
   {
      StringBuffer header = new StringBuffer();

      header.append("<setHeader headerName=\"");
      header.append(headerName);
      header.append("\">");
      header.append(headerValue);
      header.append("</setHeader>");

      return header.toString();
   }

   private static String buildMultiModelDeploymentId(String modelId, String processId)
   {

      if (!StringUtils.isEmpty(modelId) && !StringUtils.isEmpty(processId))
      {
         return "modelId=" + modelId + "&amp;processId=" + processId;
      }
      return "processId=" + processId;
   }

   private static StringBuffer insertPostHeaders(MappingExpression mappingExpression)
   {
      StringBuffer buffer = new StringBuffer();
      if (mappingExpression.getPostHeadersExpression() != null
            && !mappingExpression.getPostHeadersExpression().isEmpty())
      {
         for (String headerExpression : mappingExpression.getPostHeadersExpression())
            buffer.append(headerExpression + "\n");
      }
      return buffer;
   }
}
