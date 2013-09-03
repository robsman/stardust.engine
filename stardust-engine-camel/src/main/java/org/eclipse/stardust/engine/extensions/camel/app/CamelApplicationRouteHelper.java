package org.eclipse.stardust.engine.extensions.camel.app;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.SPRING_XML_ROUTE_FOOTER;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.getRouteId;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.extensions.camel.CamelConstants;

public class CamelApplicationRouteHelper
{

   public static final Logger logger = LogManager.getLogger(CamelApplicationRouteHelper.class);

   public static void createAndStartProducerRoute(IApplication application, CamelContext context, String partition)
         throws Exception
   {
      String routeId = getRouteId(partition, application.getModel().getId(), null, application.getId(), true);

      String route = (String) application.getAttribute(CamelConstants.PRODUCER_ROUTE_ATT);

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);

      if (runningRoute != null)
      {
         context.stopRoute(routeId);
         logger.info("Stopping Producer Route " + routeId + "  defined in Context " + context.getName()
               + " for partition " + partition);
         context.removeRoute(routeId);
         logger.info("Removing Producer Route " + routeId + "  from camel context " + context.getName());
         runningRoute = null;
      }

      StringBuilder routeDefinition = new StringBuilder();

      if (!StringUtils.isEmpty(route))
      {
         routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
         routeDefinition.append("<route autoStartup=\"true\" id=\"");
         routeDefinition.append(routeId).append("\">");

         String endpointName = "direct://" + application.getId();

         if (!route.startsWith("<from"))
         {
            routeDefinition.append("<from uri=\"");
            routeDefinition.append(endpointName);
            routeDefinition.append("\"/>");
            
            // TODO : Make it more visible (UI)
            routeDefinition.append("<transacted ref=\"required\"/>");
            
            routeDefinition.append("<process ref=\"mapAppenderProcessor\" />");
            routeDefinition.append(route);
         }
         else
         {
            routeDefinition.append("<process ref=\"mapAppenderProcessor\" />");

            routeDefinition.append(createHeader(CamelConstants.MessageProperty.ORIGIN,
                  CamelConstants.OriginValue.APPLICATION_PRODUCER));

            if (route.contains("ipp:direct"))
            {
               route = route.replace("ipp:direct", endpointName);
            }

            routeDefinition.append(route);
         }

         routeDefinition.append(SPRING_XML_ROUTE_FOOTER);
         routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_FOOTER);

         logger.info("Route " + routeId + " to be added to context " + context.getName() + " for partition "
               + partition);

         if (logger.isDebugEnabled())
         {
            logger.debug(routeDefinition);
         }
      }

      RoutesDefinition routes = ((ModelCamelContext) context).loadRoutesDefinition(IOUtils
            .toInputStream(routeDefinition.toString()));

      if (routes != null && routes.getRoutes() != null && !routes.getRoutes().isEmpty())
      {
         for (RouteDefinition routeToBeStarted : routes.getRoutes())
         {
            ((ModelCamelContext) context).addRouteDefinition(routeToBeStarted);
         }
      }
   }

   public static void createAndStartConsumerRoute(IApplication application, CamelContext context, String partition)
         throws Exception
   {
      String routeId = getRouteId(partition, application.getModel().getId(), null, application.getId(), false);

      String route = (String) application.getAttribute(CamelConstants.CONSUMER_ROUTE_ATT);

      RouteDefinition runningRoute = ((ModelCamelContext) context).getRouteDefinition(routeId);

      if (runningRoute != null)
      {
         context.stopRoute(routeId);
         logger.info("Stopping Consumer Route " + routeId + "  defined in Context " + context.getName()
               + " for partition " + partition);
         context.removeRoute(routeId);
         logger.info("Removing Consumer Route " + routeId + "  from camel context " + context.getName());
         runningRoute = null;
      }

      StringBuilder routeDefinition = new StringBuilder();

      if (!StringUtils.isEmpty(route) && runningRoute == null)
      {
         routeDefinition.append(CamelConstants.SPRING_XML_ROUTES_HEADER);
         routeDefinition.append("<route autoStartup=\"true\" id=\"");
         routeDefinition.append(routeId).append("\">");

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

               routeDefinition.append(createHeader(CamelConstants.MessageProperty.ORIGIN,
                     CamelConstants.OriginValue.APPLICATION_CONSUMER));

               routeDefinition.append(createHeader(CamelConstants.MessageProperty.PARTITION, partition));

               routeDefinition.append(createHeader(CamelConstants.MessageProperty.MODEL_ID, application.getModel()
                     .getId()));

               routeDefinition.append(createHeader(CamelConstants.MessageProperty.ROUTE_ID,
                     getRouteId(partition, application.getModel().getId(), null, application.getId(), false)));

               // TODO : Make it more visible (UI)
               routeDefinition.append("<transacted ref=\"required\"/>"); 

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

               routeDefinition.append("<to uri=\"ipp:activity:find?expectedResultSize=1");

               routeDefinition.append("&");
               routeDefinition.append("dataFiltersMap");
               routeDefinition.append("=$simple{header.");
               routeDefinition.append(CamelConstants.MessageProperty.DATA_MAP_ID);
               routeDefinition.append("}");

               routeDefinition.append("\" />");

               routeDefinition.append("<to uri=\"ipp:activity:complete\" />");
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

         RoutesDefinition routes = ((ModelCamelContext) context)
               .loadRoutesDefinition(IOUtils.toInputStream(finalRoute));

         if (routes != null && routes.getRoutes() != null && !routes.getRoutes().isEmpty())
         {
            for (RouteDefinition routeToBeStarted : routes.getRoutes())
            {
               ((ModelCamelContext) context).addRouteDefinition(routeToBeStarted);
            }
         }
      }
   }

   private static String createHeader(String headerName, Object headerValue)
   {
      StringBuffer header = new StringBuffer();

      header.append("<setHeader headerName=\"");
      header.append(headerName);
      header.append("\">");
      header.append("<constant>");
      header.append(headerValue);
      header.append("</constant>");
      header.append("</setHeader>");

      return header.toString();
   }
}
