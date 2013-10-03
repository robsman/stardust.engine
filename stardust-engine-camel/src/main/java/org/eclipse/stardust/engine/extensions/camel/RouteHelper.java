package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.MessageProperty.*;
import static org.eclipse.stardust.engine.extensions.camel.Util.getProvidedRouteConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.language.simple.SimpleExpressionParser;
import org.apache.camel.language.simple.types.SimpleIllegalSyntaxException;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.app.CamelProducerSpringBeanApplicationInstance;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;

public class RouteHelper
{
   public static final Logger logger = LogManager.getLogger(CamelProducerSpringBeanApplicationInstance.class
         .getCanonicalName());

   public static boolean isProducerApplication(IApplication application)
   {
      return application.getType().getId().equalsIgnoreCase(CAMEL_PRODUCER_APPLICATION_TYPE);
   }

   private static String buildMultiModelDeploymentId(String modelId, String processId)
   {

      if (!StringUtils.isEmpty(modelId) && !StringUtils.isEmpty(processId))
      {
         return "modelId=" + modelId + "&amp;processId=" + processId;
      }
      return "processId=" + processId;
   }

   /**
    * Creates the corresponding Spring DLS Route Definition
    * 
    * @return
    */
   public static StringBuilder createRouteDefintion(String providedRouteDefinition, String partitionId, String modelId,
         String processId, String triggerName, String userName, String password, MappingExpression mappingExpression)
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
                  "<simple>$simple{" + mappingExpression.getHeaderExpression().get(headerName) + "}</simple>\n");

            // headersFragment += "<setHeader headerName=\"" + headerName + "\">\n";
            // headersFragment +=
            // + "}</simple>\n";
            // headersFragment += "</setHeader>\n";
         }
         authenticationEndpoint += headersFragment;
      }
      providedRouteDefinition = injectAuthenticationEndpoint(providedRouteDefinition, authenticationEndpoint);
      route.append(replaceSymbolicEndpoint(providedRouteDefinition, replacementString.toString()));

      // if (mappingExpression.getIncludeMoveEndpoint())
      // {
      // route.append("<to uri=\"ipp:dms:move?documentId=${header."+CamelConstants.MessageProperty.DOCUMENT_Id+"}\"/>"+NEW_LINE);
      //
      // }
      route.append(NEW_LINE);
      route.append(SPRING_XML_ROUTE_FOOTER);
      return route;
   }

   private static String injectAuthenticationEndpoint(String routeDefinition, String authenticationEndpoint)
   {
      int fromStartIndex = routeDefinition.indexOf("<from");
      String fromEndpoint = routeDefinition.substring(fromStartIndex);
      int fromEndIndex = fromEndpoint.indexOf(">") + 1;
      fromEndpoint = fromEndpoint.substring(0, fromEndIndex);

      String routeDefinitionEndpoints = routeDefinition.substring(fromEndIndex);

      StringBuffer buffer = new StringBuffer();
      buffer.append(fromEndpoint);
      buffer.append(transacted("required")); // TODO : make it more visible
      buffer.append(authenticationEndpoint);
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

   private static String buildAuthenticationEndpoint(String partition, String user, String password)
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(setHeader(CamelConstants.MessageProperty.ORIGIN, "<constant>"
            + CamelConstants.OriginValue.TRIGGER_CONSUMER + "</constant>"));
      buffer.append(setHeader(PASSWORD, "<constant>" + EndpointHelper.sanitizeUri(password) + "</constant>"));
      buffer.append(setHeader(USER, "<constant>" + EndpointHelper.sanitizeUri(password) + "</constant>"));
      buffer.append(setHeader(PARTITION, "<constant>" + partition + "</constant>"));
      buffer.append(to("ipp:authenticate:setCurrent"));
      return buffer.toString();
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

   public static String getRouteId(String partition, String modelId, String parentModelElementId,
         String modelElementId, boolean isProducer)
   {
      logger.debug("Calculating RouteId for Camel Application Type <" + modelElementId
            + "> with the following parameters :");
      logger.debug("< Partition = " + partition + ", modelId = " + modelId + ", parentModelElementId = "
            + ((parentModelElementId == null) ? "" : parentModelElementId) + ", Is Producer Application = "
            + isProducer + ">");

      String type = isProducer ? "Producer" : "Consumer";
      StringBuffer buffer = new StringBuffer(200);
      buffer.append(partition);
      buffer.append("|");
      buffer.append(modelId);
      buffer.append("|");

      if (parentModelElementId != null)
      {
         buffer.append(parentModelElementId);
         buffer.append("|");
      }

      buffer.append(modelElementId);

      return type + buffer.toString().hashCode();
   }

   /**
    * Stops a running route in a Camel Context
    * 
    * @param camelContext
    * @param runningRouteId
    */
   public static void stopRunningRoute(CamelContext camelContext, String runningRouteId)
   {
      if (camelContext != null && !camelContext.getRoutes().isEmpty())
      {

         try
         {
            camelContext.stopRoute(runningRouteId);

            logger.info("Route " + runningRouteId + " is stopped in context " + camelContext.getName());
         }
         catch (Exception e)
         {
            logger.error("Failed removing route from context.", e);
         }

         // }
      }
   }

   /**
    * Stops and removes a running route in camelContext
    * 
    * @param camelContext
    * @param routeId
    */
   public static void stopAndRemoveRunningRoute(CamelContext camelContext, String routeId)
   {
      if (camelContext != null && !camelContext.getRoutes().isEmpty())
      {

         try
         {
            stopRunningRoute(camelContext, routeId);

            camelContext.removeRoute(routeId);

            logger.info("Route " + routeId + " is removed from context " + camelContext.getName());

         }
         catch (Exception e)
         {
            logger.error("Failed removing route from context.", e);
         }
      }

   }

   private static String value(String value)
   {
      return new StringBuilder(SPRING_XML_VALUE_ELT_HEADER + value + SPRING_XML_VALUE_ELT_FOOTER).toString();
   }

   public static String convertMapToXml(Map<String, String> input)
   {
      StringBuilder xml = new StringBuilder(SPRING_XML_MAP_ELT_HEADER);
      if (input != null && !input.isEmpty())
      {
         for (String key : input.keySet())
         {
            xml.append(entry(key));
            xml.append(value(StringEscapeUtils.escapeHtml(input.get(key))));
            xml.append(SPRING_XML_ENTRY_ELT_FOOTER);
         }
      }
      xml.append(SPRING_XML_MAP_ELT_FOOTER);
      logger.debug("Convert Map To Xml :" + xml);
      return xml.toString();
   }

   public static String entry(String key)
   {
      return new StringBuilder(LESS_THAN_SIGN + "" + SPRING_XML_ENTRY_ELT_HEADER + " key=\"" + key + "\""
            + GREATER_THAN_SIGN).toString();
   }

   /**
    * Returns an XML representation of Route Element.
    * 
    * @param id
    * @param autoStartUp
    *           flag
    * @return route
    */
   public static String route(String id, boolean autoStartUp)
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

   public static String from(final String uri)
   {
      StringBuilder from = new StringBuilder();
      from.append(LESS_THAN_SIGN + "from uri=\"" + uri + "\" /" + GREATER_THAN_SIGN);
      return from.toString();
   }

   /**
    * Returns an XML representation of setHeader Element. The generated XML is as below:
    * <p>
    * &lt;setHeader headerName=" HEADER_PROPERTY"&gt;
    * </p>
    * <p>
    * &lt;constant&gt;ConstantLanguage.constant( CONSTANT ) &lt;/constant&gt;
    * </p>
    * <p>
    * &lt;/setHeader&gt;
    * </p>
    * 
    * @param headerProperty
    * @param constant
    * @return
    */
   // public static String setHeader(final String headerProperty, String constant)
   // {
   // StringBuilder setHeader = new StringBuilder();
   // setHeader.append(NEW_LINE+LESS_THAN_SIGN+"setHeader headerName=\"" + headerProperty
   // + "\""+GREATER_THAN_SIGN);
   // setHeader.append(NEW_LINE+HORIZONTAL_TAB+LESS_THAN_SIGN+"constant"+GREATER_THAN_SIGN
   // + ConstantLanguage.constant(constant) +
   // LESS_THAN_SIGN+"/constant"+GREATER_THAN_SIGN);
   // setHeader.append(NEW_LINE+LESS_THAN_SIGN+"/setHeader"+GREATER_THAN_SIGN);
   // return setHeader.toString();
   // }

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
   public static String to(final String uri)
   {
      StringBuilder to = new StringBuilder();
      to.append(LESS_THAN_SIGN + "to uri=\"" + uri + "\" /" + GREATER_THAN_SIGN);
      return to.toString();
   }

   public static String process(final String beanId)
   {
      StringBuilder process = new StringBuilder();
      process.append(LESS_THAN_SIGN + "process ref=\"" + beanId + "\" /" + GREATER_THAN_SIGN);
      return process.toString();
   }

   public static String transacted(final String tranId)
   {
      StringBuilder transacted = new StringBuilder();
      transacted.append(LESS_THAN_SIGN + "transacted ref=\"" + tranId + "\" /" + GREATER_THAN_SIGN);
      return transacted.toString();
   }

   /**
    * @param endpointUri
    * @return file endpoint
    * @throws UndefinedEndpointException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   // public static Endpoint extractEndpoint(String endpointUri) throws
   // UndefinedEndpointException,
   // InstantiationException, IllegalAccessException
   // {
   // // logger.debug("--> Method extractEndpoint invoked with Parameters <endpointUri="
   // // +
   // // endpointUri + ">");
   // Endpoint endpoint = null;
   // String endPointType = null;
   // Class< ? > clazz = null;
   // Pattern endPointPattern = Pattern.compile("^\\w*[-]*\\w*:");
   // Matcher endPointMatcher = endPointPattern.matcher(endpointUri);
   //
   // if (endPointMatcher.find())
   // {
   // endPointType = endPointMatcher.group().substring(endPointMatcher.start(),
   // endPointMatcher.end() - 1);
   // char leadChar = endPointType.toCharArray()[0];
   // endPointType = String.valueOf(leadChar).toUpperCase() + endPointType.substring(1);
   // String className = ENDPOINT_PKG + "." + endPointType + "Endpoint";
   // // logger.debug("Endpoint ClassName :"+className);
   // try
   // {
   // clazz = Class.forName(className);
   // }
   // catch (ClassNotFoundException e)
   // {
   // // logger.error("Error Occured"+e.getMessage());
   // }
   // }
   // if (clazz != null)
   // {
   // // Handle Managed Endpoints
   // endpoint = (Endpoint) clazz.newInstance();
   // }
   // else
   // {// Generic Endpoint Implementation
   // //
   // logger.debug("Camel Component<"+endPointType+"> is not handled; Generic Endpoint  will be used instead.");
   // String className = ENDPOINT_PKG + ".GenericEndpoint";
   // try
   // {
   // clazz = Class.forName(className);
   // }
   // catch (ClassNotFoundException e)
   // {
   // // logger.error("Error Occured"+e.getMessage());
   // }
   // endpoint = (Endpoint) clazz.newInstance();
   // }
   // return endpoint;
   //
   // }

   /**
    * initialize an endpoint with a specified class name. If no class name is selected, it
    * returns a generic endpoint
    * 
    * @param selectedClassName
    * @return a new instance of Endpoint
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    */
   // public static Endpoint initializeEndpoint(String selectedClassName) throws
   // InstantiationException,
   // IllegalAccessException, ClassNotFoundException
   // {
   // if (StringUtils.isEmpty(selectedClassName))
   // selectedClassName = GENERIC_ENDPOINT;
   //
   // return (Endpoint) Class.forName(selectedClassName).newInstance();
   //
   // }

   public static String replaceSymbolicEndpoint(String providedRouteDefinition, String replacementUri)
   {
      if (!StringUtils.isEmpty(providedRouteDefinition))
      {
         if (providedRouteDefinition.contains(IPP_DIRECT_TAG))
         {
            int indexOfUri = providedRouteDefinition.indexOf(IPP_DIRECT_TAG);
            int indexOfEndStatement = providedRouteDefinition.substring(indexOfUri, providedRouteDefinition.length())
                  .indexOf("/" + GREATER_THAN_SIGN);
            String partToBeReplaced = providedRouteDefinition.substring(indexOfUri, indexOfUri + indexOfEndStatement);
            String replacedUri = providedRouteDefinition.replace(partToBeReplaced, replacementUri);
            return replacedUri;
         }

      }
      return providedRouteDefinition;
   }

   /**
    * create a new bean definition
    * 
    * @param beanName
    *           the bean id
    * @param className
    *           specify the bean class to be used to create the bean
    * @param propertyName
    *           the property name
    * @param propertyValue
    *           the property value
    * @return a new bean definition
    */
   public static StringBuilder createBeanDefinition(String beanName, String className, List<BeanProperty> properties)
   {
      StringBuilder beanDefinition = new StringBuilder(LESS_THAN_SIGN + "bean id=\"" + beanName + "\" class=\""
            + className + "\"" + GREATER_THAN_SIGN);
      if (properties != null && !properties.isEmpty())
         for (BeanProperty property : properties)
         {

            beanDefinition.append(LESS_THAN_SIGN + "property name=\"" + property.getPropertyName() + "\""
                  + GREATER_THAN_SIGN);
            if (!property.isMap())
               beanDefinition.append(value(property.getPropertyValue()));
            else
               beanDefinition.append(property.getPropertyValue());
            beanDefinition.append(LESS_THAN_SIGN + "/property" + GREATER_THAN_SIGN + NEW_LINE);
         }
      beanDefinition.append(LESS_THAN_SIGN + "/bean" + GREATER_THAN_SIGN);
      return beanDefinition;
   }

   /**
    * create the content of a spring file
    * 
    * @param providedBeanConfiguration
    * @param fieldMappingProvided
    * @param mapAppenderBeanDefinition
    * @return beanDefinition the content of a spring file
    */
   public static StringBuilder createSpringFileContent(String providedBeanConfiguration, boolean fieldMappingProvided,
         StringBuilder mapAppenderBeanDefinition)
   {

      StringBuilder beanDefinition = new StringBuilder();
      beanDefinition.append(SPRING_XML_HEADER);
      beanDefinition.append(providedBeanConfiguration);
      if (fieldMappingProvided)
         beanDefinition.append(mapAppenderBeanDefinition);
      beanDefinition.append(SPRING_XML_FOOTER);
      return beanDefinition;
   }

   /**
    * load new beans definition in the application context
    * 
    * @param beanDefinition
    *           the bean definition
    * @param applicationcontext
    *           the application context
    * @throws JDOMException
    * @throws IOException
    */

   public static void loadBeanDefinition(StringBuilder beanDefinition, AbstractApplicationContext applicationcontext)
         throws JDOMException, IOException
   {

      if (beanDefinition != null)
      {
         XmlParserUtil parser = new XmlParserUtil(IOUtils.toInputStream(beanDefinition.toString()));
         List<Element> beansDefinitions = parser.getBeanDefinition();
         logger.debug("<" + beansDefinitions.size() + "> bean definition declared in Camel application type.");
         if (beansDefinitions.size() > 0)
         {
            for (Element elt : beansDefinitions)
            {
               if (elt.getAttribute("id") != null && applicationcontext.containsBean(elt.getAttribute("id").getValue()))
               {
                  logger.debug("Bean <" + elt.getAttribute("id").getValue()
                        + "> is already in the application context, it will not be added twice");
               }
               else
               {
                  XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
                  String xmlRepresentation = outputter.outputString(elt);
                  StringBuilder generatedXmlRepresentation = new StringBuilder();
                  generatedXmlRepresentation.append(SPRING_XML_HEADER);
                  generatedXmlRepresentation.append(xmlRepresentation);
                  generatedXmlRepresentation.append(SPRING_XML_FOOTER);
                  logger.debug("Generated spring definition :" + generatedXmlRepresentation);
                  BeanDefinitionRegistry beanFactory = (DefaultListableBeanFactory) applicationcontext.getBeanFactory();
                  XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
                  beanDefinitionReader.setResourceLoader(new DefaultResourceLoader());
                  beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(beanDefinitionReader
                        .getResourceLoader()));
                  // beanDefinitionReader.setBeanClassLoader(beanDefinitionReader.getBeanClassLoader());
                  int res = beanDefinitionReader.loadBeanDefinitions(new ByteArrayResource(generatedXmlRepresentation
                        .toString().getBytes()));
                  logger.debug(res + "beans loaded in the application context.");
               }

            }
         }
      }

   }

   public static Expression parseSimpleExpression(String expression)
   {
      logger.debug("Parsing Simple Expression <" + expression + ">");
      SimpleExpressionParser parser = new SimpleExpressionParser(expression);
      try
      {
         return parser.parseExpression();
      }
      catch (SimpleIllegalSyntaxException e)
      {
         logger.debug("Invalid Simple Expression provided <" + expression + ">");
         return null;
      }
   }

   public static String setHeader(String headerName, String headerValue)
   {
      StringBuffer header = new StringBuffer();

      header.append("<setHeader headerName=\"");
      header.append(headerName);
      header.append("\">");
      header.append(headerValue);
      header.append("</setHeader>");

      return header.toString();
   }

   public static void createAndStartProducerRoute(IApplication application, CamelContext context, String partition)
         throws Exception
   {
      String routeId = getRouteId(partition, application.getModel().getId(), null, application.getId(), true);

      String route =(String) application.getAttribute(CamelConstants.PRODUCER_ROUTE_ATT);//getProvidedRouteConfiguration(application);// (String)
                                                                // application.getAttribute(CamelConstants.PRODUCER_ROUTE_ATT);

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);

      if (runningRoute != null)
      {
         stopAndRemoveRunningRoute(context, routeId);
         logger.info("Stopping Producer Route " + routeId + "  defined in Context " + context.getName()
               + " for partition " + partition);
         // logger.info("Removing Producer Route " + routeId + "  from camel context " +
         // context.getName());
         runningRoute = null;
      }

      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
      if (!StringUtils.isEmpty(route))
      {
         
         routeDefinition.append(route(routeId,true));


         String endpointName = "direct://" + application.getId();

         if (!route.startsWith("<from"))
         {
            routeDefinition.append(from(endpointName));
            // routeDefinition.append("<from uri=\"");
            // routeDefinition.append(endpointName);
            // routeDefinition.append("\"/>");

            // TODO : Make it more visible (UI)
            routeDefinition.append(transacted("required"));

            routeDefinition.append(process("mapAppenderProcessor"));
            routeDefinition.append(route);
         }
         else
         {
            routeDefinition.append(process("mapAppenderProcessor"));

            routeDefinition.append(setHeader(CamelConstants.MessageProperty.ORIGIN, "<constant>"
                  + CamelConstants.OriginValue.APPLICATION_PRODUCER + "</constant>"));

            if (route.contains(IPP_DIRECT_TAG))
            {
               route = route.replace(IPP_DIRECT_TAG, endpointName);
            }

            routeDefinition.append(route);
         }

         routeDefinition.append(SPRING_XML_ROUTE_FOOTER);
         

         logger.info("Route " + routeId + " to be added to context " + context.getName() + " for partition "
               + partition);

         if (logger.isDebugEnabled())
         {
            logger.debug(routeDefinition);
         }
         
         
      }
      routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_FOOTER);
      loadRouteDefinition(routeDefinition.toString(),context);
}

   public static void loadRouteDefinition(String routeDefinition, CamelContext camelContext) throws Exception{
      RoutesDefinition routes = ((ModelCamelContext) camelContext).loadRoutesDefinition(IOUtils
            .toInputStream(routeDefinition.toString()));

      if (routes != null && routes.getRoutes() != null && !routes.getRoutes().isEmpty())
      {
         for (RouteDefinition routeToBeStarted : routes.getRoutes())
         {
            ((ModelCamelContext) camelContext).addRouteDefinition(routeToBeStarted);
         }
      }
      
   }
   
   public static void createAndStartConsumerRoute(IApplication application, CamelContext context, String partition)
         throws Exception
   {
      String routeId = getRouteId(partition, application.getModel().getId(), null, application.getId(), false);

      String route = getProvidedRouteConfiguration(application);

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);

      if (runningRoute != null)
      {
         logger.info("Stopping Consumer Route " + routeId + "  defined in Context " + context.getName()
               + " for partition " + partition);

         stopAndRemoveRunningRoute(context, routeId);
         logger.info("Removing Consumer Route " + routeId + "  from camel context " + context.getName());
         runningRoute = null;
      }

      StringBuilder routeDefinition = new StringBuilder();

      if (!StringUtils.isEmpty(route) && runningRoute == null)
      {
         routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
         routeDefinition.append(route(routeId,true));

         if (route.contains(CamelConstants.IPP_AUTHENTICATE_TAG))
         {

            String[] parts = route.split(CamelConstants.IPP_AUTHENTICATE_TAG);

            if (parts.length == 2)
            {
               int indexOpenTag = parts[0].lastIndexOf("<");
               int indexCloseTag = parts[1].indexOf("/>");

               String before = parts[0].substring(0, indexOpenTag);
               String after = parts[1].substring(indexCloseTag + 2);

               routeDefinition.append(before);

               routeDefinition.append(setHeader(CamelConstants.MessageProperty.ORIGIN, "<constant>"
                     + CamelConstants.OriginValue.APPLICATION_CONSUMER + "</constant>"));

               routeDefinition.append(setHeader(CamelConstants.MessageProperty.PARTITION, "<constant>" + partition
                     + "</constant>"));

               routeDefinition.append(setHeader(CamelConstants.MessageProperty.MODEL_ID, "<constant>"
                     + application.getModel().getId()+ "</constant>")
                     );

               routeDefinition.append(setHeader(
                     CamelConstants.MessageProperty.ROUTE_ID,
                     "<constant>"
                           + getRouteId(partition, application.getModel().getId(), null, application.getId(), false)
                           + "</constant>"));

               // TODO : Make it more visible (UI)
               routeDefinition.append(transacted("required"));
               routeDefinition.append("<to uri=\"");
               routeDefinition.append(CamelConstants.IPP_AUTHENTICATE_TAG);
               routeDefinition.append(parts[1].replace(after, ""));

               route = after;
            }
         }

         if (route.contains(CamelConstants.IPP_DIRECT_TAG))
         {

            String[] parts = route.split(CamelConstants.IPP_DIRECT_TAG);

            if (parts.length == 2)
            {
               int indexOpenTag = parts[0].lastIndexOf("<");
               int indexCloseTag = parts[1].indexOf("/>");

               String before = parts[0].substring(0, indexOpenTag);
               String after = parts[1].substring(indexCloseTag + 2);

               routeDefinition.append(before);
               routeDefinition.append(to("ipp:activity:find?expectedResultSize=1&dataFiltersMap=$simple{header."+CamelConstants.MessageProperty.DATA_MAP_ID+"}"));
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
            routeDefinition.append(route);
         }

         routeDefinition.append(CamelConstants.SPRING_XML_ROUTE_FOOTER);
         routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_FOOTER);

         logger.info("Route " + routeId + " to be added to context " + context.getName() + " for partition "
               + partition);

         if (logger.isDebugEnabled())
         {
            logger.debug(routeDefinition);
         }

         String finalRoute = routeDefinition.toString();
         finalRoute = finalRoute.replace("&", "&amp;");

         loadRouteDefinition(finalRoute,context);
      }
   }

}
