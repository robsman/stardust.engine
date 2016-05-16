package org.eclipse.stardust.engine.extensions.camel;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.Util.*;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.createProducerXmlConfiguration;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.createConsumerXmlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.language.simple.SimpleExpressionParser;
import org.apache.camel.language.simple.types.SimpleIllegalSyntaxException;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.extensions.camel.core.ConsumerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContextFactory;
import org.eclipse.stardust.engine.extensions.camel.core.app.ConsumerApplicationRouteContext;
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
   public static final Logger logger = LogManager.getLogger(RouteHelper.class.getCanonicalName());

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
      if (camelContext != null)
      {
         if (!camelContext.getRoutes().isEmpty())
         {
            try
            {
               stopRunningRoute(camelContext, routeId);
               camelContext.removeRoute(routeId);

               if (logger.isInfoEnabled())
                  logger.info("Route " + routeId + " is removed from context " + camelContext.getName());

            }
            catch (Exception e)
            {
               logger.error("Failed removing route from context.", e);
            }
         }
         else
         {
            // clear route definition related to the same routeId
            removeRouteDefinition((ModelCamelContext) camelContext, routeId);
         }
      }

   }

   /**
    * Removes a route definition in camelContext
    *
    * @param camelContext
    * @param routeId
    */
   private static void removeRouteDefinition(ModelCamelContext camelContext, String routeId)
   {
      RouteDefinition routeDefinition = camelContext.getRouteDefinition(routeId);
      try
      {
         camelContext.removeRouteDefinition(routeDefinition);
      }
      catch (Exception e)
      {
         logger.error("Failed removing route definition from context.", e);
      }
   }

   /**
    * takes a standard spring xml file and loads its content.
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
         if (logger.isDebugEnabled())
            logger.debug("<" + beansDefinitions.size() + "> bean definition declared in Camel application type.");
         if (beansDefinitions.size() > 0)
         {
            for (Element elt : beansDefinitions)
            {
               if (elt.getAttribute("id") != null && applicationcontext.containsBean(elt.getAttribute("id").getValue()))
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Bean <" + elt.getAttribute("id").getValue()
                           + "> is already defined in the spring context");

               }
               else
               {
                  XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
                  String xmlRepresentation = outputter.outputString(elt);
                  StringBuilder generatedXmlRepresentation = new StringBuilder();
                  generatedXmlRepresentation.append(SPRING_XML_HEADER);
                  generatedXmlRepresentation.append(xmlRepresentation);
                  generatedXmlRepresentation.append(SPRING_XML_FOOTER);
                  if (logger.isDebugEnabled())
                     logger.debug("Generated spring definition :" + generatedXmlRepresentation);
                  BeanDefinitionRegistry beanFactory = (DefaultListableBeanFactory) applicationcontext.getBeanFactory();
                  XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
                  beanDefinitionReader.setResourceLoader(new DefaultResourceLoader());
                  beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(beanDefinitionReader
                        .getResourceLoader()));
                  // beanDefinitionReader.setBeanClassLoader(beanDefinitionReader.getBeanClassLoader());
                  int res = beanDefinitionReader.loadBeanDefinitions(new ByteArrayResource(generatedXmlRepresentation
                        .toString().getBytes()));
                  if (logger.isDebugEnabled())
                     logger.debug(res + "beans loaded in the application context.");
               }
            }
         }
      }

   }

   /**
    *
    * @param application
    * @param context
    * @param partition
    * @throws Exception
    */
   public static void createAndStartProducerRoute(IApplication application, CamelContext context, String partition)
         throws Exception
   {
      ProducerRouteContext routeContext = ProducerRouteContextFactory.getContext(application,context, partition);//new ProducerRouteContext(application, partition, context.getName());
      String routeId = routeContext.getRouteId();

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);
      if (runningRoute != null)
      {
         stopAndRemoveRunningRoute(context, routeId);//
         if (logger.isDebugEnabled())
         {
            logger.debug("Stopping Producer Route " + routeId + "  defined in Context " + context.getName()
                  + " for partition " + partition);
         }
         runningRoute = null;
      }

      String routeDefinition = createProducerXmlConfiguration(routeContext);
      if(StringUtils.isNotEmpty(routeDefinition)){
         if (logger.isDebugEnabled())
         {
            logger.debug("Starting Producer Route " + routeId + "  will be added to " + context.getName() + " for partition " + partition);
            logger.debug("Route Content "+routeDefinition);
         }
         loadRouteDefinition(routeDefinition, context);
      }
   }

   /**
    *
    *
    * @param routeDefinition
    * @param camelContext
    * @throws Exception
    */
   public static void loadRouteDefinition(String routeDefinition, CamelContext camelContext) throws Exception
   {
      RoutesDefinition routes = ((ModelCamelContext) camelContext).loadRoutesDefinition(IOUtils
            .toInputStream(routeDefinition.toString(),"UTF-8"));

      if (routes != null && routes.getRoutes() != null && !routes.getRoutes().isEmpty())
      {
         for (RouteDefinition routeToBeStarted : routes.getRoutes())
         {
            ((ModelCamelContext) camelContext).addRouteDefinition(routeToBeStarted);

         }
      }

   }

   /**
    *
    * @param application
    * @param context
    * @param partitionId
    * @throws Exception
    */
   public static void createAndStartConsumerRoute(IApplication application, CamelContext context, String partitionId)
         throws Exception
   {
      ConsumerRouteContext consumerContext=new ConsumerApplicationRouteContext(application,partitionId,context.getName());
      String routeId =consumerContext.getRouteId();

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);

      if (runningRoute != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Stopping Consumer Route " + routeId + "  defined in Context " + context.getName()
                  + " for partition " + partitionId);

         stopAndRemoveRunningRoute(context, routeId);
         if (logger.isDebugEnabled())
            logger.debug("Removing Consumer Route " + routeId + "  from camel context " + context.getName());
         runningRoute = null;
      }

      String routeDefinition = createConsumerXmlConfiguration(consumerContext);

      if (logger.isDebugEnabled())
      {
         logger.debug("Consumer Route " + routeId + "  will be added to " + context.getName() + " for partition " + partitionId);
         logger.debug("The generated route content  is \n"+routeDefinition);
      }
      loadRouteDefinition(routeDefinition, context);

   }

   /**
    *
    * @param expression
    * @return
    */
   public static Expression parseSimpleExpression(String expression)
   {
      if (logger.isDebugEnabled())
         logger.debug("Parsing Simple Expression <" + expression + ">");
      SimpleExpressionParser parser = new SimpleExpressionParser(expression, false);
      try
      {
         return parser.parseExpression();
      }
      catch (SimpleIllegalSyntaxException e)
      {
         if (logger.isDebugEnabled())
            logger.debug("Invalid Simple Expression provided <" + expression + ">", e);
         return null;
      }
   }

   /**
    *
    * @param springContext
    * @param partition
    */
   @SuppressWarnings("unchecked")
   public static void createRouteForAllApplications(org.springframework.context.ApplicationContext springContext,
         String partition)
   {

      List<IModel> models = ModelManagerFactory.getCurrent().getModels();

      for (int i = 0; i < models.size(); i++)
      {

         IModel m = models.get(i);

         if (ModelManagerFactory.getCurrent().isActive(m))
         {

            ModelElementList<IApplication> apps = m.getApplications();

            for (int ai = 0; ai < apps.size(); ai++)
            {

               IApplication app = apps.get(ai);

               if (app != null
                     && app.getType() != null
                     && (app.getType().getId().equals(CamelConstants.CAMEL_CONSUMER_APPLICATION_TYPE) || app.getType()
                           .getId().equals(CamelConstants.CAMEL_PRODUCER_APPLICATION_TYPE)))
               {
                  logger.debug("Processing Routes defined in " + app.getId());
                  createRouteForApplication(app, springContext, partition);
               }

            }
         }
      }
   }

   /**
    *
    * @param application
    * @param springContext
    * @param partition
    */
   public static void createRouteForApplication(IApplication application,
         org.springframework.context.ApplicationContext springContext, String partition)
   {

      try
      {
         String contextId = getCamelContextId(application);

         String springBeans = getAdditionalBeansDefinition(application);

         CamelContext camelContext = (CamelContext) springContext.getBean(contextId);

         if (!StringUtils.isEmpty(springBeans))
         {
            loadBeanDefinition(createSpringFileContent(springBeans, false, null),
                  (AbstractApplicationContext) springContext);
         }


         if (isConsumerApplication(application))
         {
            createAndStartConsumerRoute(application, camelContext, partition);
         }

         if (isProducerApplication(application))
         {
            createAndStartProducerRoute(application, camelContext, partition);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception creating route for application " + application.getId(), e);
      }
   }
   public static void removeRouteDefinitionWithoutRunningRoute(ModelCamelContext camelContext, String routeId){
      //using e.getCause() since e is RTE thrown by the Action class
        if(camelContext!=null && !camelContext.getRouteDefinitions().isEmpty()){
           List<RouteDefinition> routesDefinitions=new ArrayList<RouteDefinition>();
           routesDefinitions.addAll(camelContext.getRouteDefinitions());

           for(RouteDefinition routeDefinition:routesDefinitions){
              try{
              if(routeDefinition.getId().equalsIgnoreCase(routeId))
                 camelContext.removeRouteDefinition(routeDefinition);
              }
              catch (Exception e1)
              {
                 throw new RuntimeException(e1);
              }
           }
        }
     }

   public static void startRoute(ModelCamelContext camelContext,String routeId) throws Exception{
	   camelContext.startRoute(routeId);
   }
}
