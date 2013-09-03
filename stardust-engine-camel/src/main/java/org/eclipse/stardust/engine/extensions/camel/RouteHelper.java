package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.ENDPOINT_PKG;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.GENERIC_ENDPOINT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_HEADER;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.language.constant.ConstantLanguage;
import org.apache.camel.language.simple.SimpleExpressionParser;
import org.apache.camel.language.simple.types.SimpleIllegalSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.app.CamelProducerSpringBeanApplicationInstance;
import org.eclipse.stardust.engine.extensions.camel.runtime.Endpoint;
import org.eclipse.stardust.engine.extensions.camel.trigger.exceptions.UndefinedEndpointException;
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
   public static final String SPRING_XML_ROUTES_HEADER = "<routes xmlns=\"http://camel.apache.org/schema/spring\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n";
   public static final String SPRING_XML_ROUTES_FOOTER = "\n</routes>";
   public static final String END_ROUTE_ELT = "</route>";
   public static final String GREATER_THAN_SIGN = ">";
   public static final String BLANK_SPACE = ">";
   
   public static final Logger logger = LogManager.getLogger(CamelProducerSpringBeanApplicationInstance.class
         .getCanonicalName());

   public static boolean isProducerApplication(IApplication application)
   {
      return application.getType().getId().equalsIgnoreCase("camelSpringProducerApplication");
   }

   public static String getRouteId(String partition, String modelId, String parentModelElementId,
         String modelElementId, boolean isProducer)
   {
      logger.debug("Calculating RouteId for Camel Application Type <"+modelElementId+"> with the following parameters :");
      logger.debug("< Partition = "+partition+", modelId = "+modelId+", parentModelElementId = "+((parentModelElementId==null)?"":parentModelElementId)+", Is Producer Application = "+isProducer+">");
      
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

   public static String convertMapToXml(Map<String, String> input)
   {
      StringBuilder xml = new StringBuilder("<map>");
      if (input != null && !input.isEmpty())
      {
         for (String key : input.keySet())
         {
            xml.append("<entry key=\"" + key + "\">");
            xml.append("<value>" + StringEscapeUtils.escapeHtml(input.get(key)) + "</value>");
            xml.append("</entry>");
         }
      }
      xml.append("</map>");
      logger.debug("Convert Map To Xml :" + xml);
      return xml.toString();
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
      StringBuilder route = new StringBuilder("<route");

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
      from.append("<from uri=\"" + uri + "\" />");
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
   public static String setHeader(final String headerProperty, String constant)
   {
      StringBuilder setHeader = new StringBuilder();
      setHeader.append("\n<setHeader headerName=\"" + headerProperty + "\">");
      setHeader.append("\n\t<constant>" + ConstantLanguage.constant(constant) + "</constant>");
      setHeader.append("\n</setHeader>");
      return setHeader.toString();
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
   public static String to(final String uri)
   {
      StringBuilder to = new StringBuilder();
      to.append("<to uri=\"" + uri + "\" />");
      return to.toString();
   }

   /**
    * @param endpointUri
    * @return file endpoint
    * @throws UndefinedEndpointException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   public static Endpoint extractEndpoint(String endpointUri) throws UndefinedEndpointException,
         InstantiationException, IllegalAccessException
   {
      // logger.debug("--> Method extractEndpoint invoked with Parameters <endpointUri="
      // +
      // endpointUri + ">");
      Endpoint endpoint = null;
      String endPointType = null;
      Class< ? > clazz = null;
      Pattern endPointPattern = Pattern.compile("^\\w*[-]*\\w*:");
      Matcher endPointMatcher = endPointPattern.matcher(endpointUri);

      if (endPointMatcher.find())
      {
         endPointType = endPointMatcher.group().substring(endPointMatcher.start(), endPointMatcher.end() - 1);
         char leadChar = endPointType.toCharArray()[0];
         endPointType = String.valueOf(leadChar).toUpperCase() + endPointType.substring(1);
         String className = ENDPOINT_PKG + "." + endPointType + "Endpoint";
         // logger.debug("Endpoint ClassName :"+className);
         try
         {
            clazz = Class.forName(className);
         }
         catch (ClassNotFoundException e)
         {
            // logger.error("Error Occured"+e.getMessage());
         }
      }
      if (clazz != null)
      {
         // Handle Managed Endpoints
         endpoint = (Endpoint) clazz.newInstance();
      }
      else
      {// Generic Endpoint Implementation
       // logger.debug("Camel Component<"+endPointType+"> is not handled; Generic Endpoint  will be used instead.");
         String className = ENDPOINT_PKG + ".GenericEndpoint";
         try
         {
            clazz = Class.forName(className);
         }
         catch (ClassNotFoundException e)
         {
            // logger.error("Error Occured"+e.getMessage());
         }
         endpoint = (Endpoint) clazz.newInstance();
      }
      return endpoint;

   }

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
   public static Endpoint initializeEndpoint(String selectedClassName) throws InstantiationException,
         IllegalAccessException, ClassNotFoundException
   {
      if (StringUtils.isEmpty(selectedClassName))
         selectedClassName = GENERIC_ENDPOINT;

      return (Endpoint) Class.forName(selectedClassName).newInstance();

   }

   public static String replaceSymbolicEndpoint(String providedRouteDefinition, String replacementUri)
   {
      if (!StringUtils.isEmpty(providedRouteDefinition))
      {
         if (providedRouteDefinition.contains("ipp:direct"))
         {
            int indexOfUri = providedRouteDefinition.indexOf("ipp:direct");
            int indexOfEndStatement = providedRouteDefinition.substring(indexOfUri, providedRouteDefinition.length())
                  .indexOf("/>");
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
      StringBuilder beanDefinition = new StringBuilder("<bean id=\"" + beanName + "\" class=\"" + className + "\">");
      if (properties != null && !properties.isEmpty())
         for (BeanProperty property : properties)
         {

            beanDefinition.append("<property name=\"" + property.getPropertyName() + "\">");
            if (!property.isMap())
               beanDefinition.append("<value>" + property.getPropertyValue() + "</value>");
            else
               beanDefinition.append(property.getPropertyValue());
            beanDefinition.append("</property>\n");
         }
      beanDefinition.append("</bean>");
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
}
