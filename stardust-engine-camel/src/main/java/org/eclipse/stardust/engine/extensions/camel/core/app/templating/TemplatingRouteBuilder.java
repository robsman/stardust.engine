package org.eclipse.stardust.engine.extensions.camel.core.app.templating;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DATA_LOCATION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCX_FORMAT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.EMBEDDED_LOCATION;
import static org.eclipse.stardust.engine.extensions.camel.Util.getAccessPointById;
import static org.eclipse.stardust.engine.extensions.camel.Util.isDocumentType;
import static org.eclipse.stardust.engine.extensions.camel.Util.isPrimitiveType;
import static org.eclipse.stardust.engine.extensions.camel.Util.isStringType;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.buildConstantExpression;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.buildSimpleExpression;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.process;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.setHeader;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.to;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;


public class TemplatingRouteBuilder
{
   private TemplatingRouteBuilder()
   {
   }
   
   public static final String generateRoute(final ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      String format = application.getFormat();
      if (format.equalsIgnoreCase(DOCX_FORMAT))
         routeDefinition.append(createRouteForXDocReportTemplates(application));
      else
         routeDefinition.append(createRouteForVelocityTemplates(application));

      if (StringUtils.isNotEmpty(format))
      {
         if (!format.equalsIgnoreCase(DOCX_FORMAT))
         {
            AccessPointBean defaultOutputAp = getAccessPointById("defaultOutputAp",
                  application.getAllOutAccessPoints());
            if (isDocumentType(defaultOutputAp))
            {
               routeDefinition.append(setHeader("ippDmsDocumentName",
                     buildSimpleExpression("header.CamelTemplatingOutputName")));
               routeDefinition.append(to("bean:documentHandler?method=toDocument"));
               routeDefinition
                     .append(setHeader("defaultOutputAp", buildSimpleExpression("body")));
            }
            else if (isPrimitiveType(defaultOutputAp) && isStringType(defaultOutputAp))
            {
               routeDefinition.append(setHeader("defaultOutputAp",
                     buildSimpleExpression("bodyAs(String)")));
            }
            else
            {
               routeDefinition
                     .append(setHeader("defaultOutputAp", buildSimpleExpression("body")));
            }
         }
      }

      return routeDefinition.toString();
   }

   private static String createRouteForVelocityTemplates(final ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(process("customVelocityContextAppender"));

      if (StringUtils.isNotEmpty(application.getLocation()))
      {
         if (application.getLocation().equalsIgnoreCase(EMBEDDED_LOCATION))
            routeDefinition.append(createRouteForEmbeddedVelocityTemplates(application));
         if (application.getLocation().equalsIgnoreCase(DATA_LOCATION))
            routeDefinition.append(createRouteForVelocityTemplatesFromData(application));
      }
      routeDefinition.append(to(createUri(application.getLocation(),
            application.getFormat(), application.getTemplate(),
            application.getOutputName(), application.isConvertToPdf())));
      return routeDefinition.toString();
   }

   private static final String createRouteForXDocReportTemplates(final ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(process("customVelocityContextAppender"));
      if (StringUtils.isNotEmpty(application.getLocation()))
      {
         if (application.getLocation().equalsIgnoreCase(DATA_LOCATION))
            routeDefinition.append(createHeaderContentForStrucutredAccessPoint(""));
      }

      routeDefinition.append(to(createUri(application.getLocation(),
            application.getFormat(), application.getTemplate(),
            application.getOutputName(), application.isConvertToPdf())));
      routeDefinition.append(setHeader("ippDmsDocumentName",
            buildSimpleExpression("header.CamelTemplatingOutputName")));
      routeDefinition.append(to("bean:documentHandler?method=toDocument"));
      routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("body")));

      return routeDefinition.toString();
   }

   private static final  String createUri(final String location, final String format, final String template,
         String outputName, boolean convertToPdf)
   {
      StringBuilder uri = new StringBuilder();
      uri.append("templating:" + location + "?format=" + format);
      if (StringUtils.isNotEmpty(template))
         uri.append("&amp;template=" + template);
      if (StringUtils.isNotEmpty(outputName))
         uri.append("&amp;outputName=" + outputName);
      if (convertToPdf)
         uri.append("&amp;convertToPdf=" + convertToPdf);
      return uri.toString();
   }

   private static final  String createRouteForVelocityTemplatesFromData(final ApplicationWrapper application)
   {
      AccessPointBean defaultInAccessPoint = getAccessPointById("defaultInputAp",
            application.getAllInAccessPoints());
      if (isPrimitiveType(defaultInAccessPoint))
      {
         return createHeaderContentForPrimitiveAccessPoint("defaultInputAp");
      }
      else
      {
         return createHeaderContentForStrucutredAccessPoint("ippDmsDocumentContent");
      }
   }

   private static final  String createHeaderContentForPrimitiveAccessPoint(final String id)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildSimpleExpression("header." + id)));
      return routeDefinition.toString();
   }

   private static final  String createHeaderContentForStrucutredAccessPoint(final String id)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(to("bean:documentHandler?method=retrieveContent"));
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildSimpleExpression("header." + id)));
      return routeDefinition.toString();
   }

   private static final  String createRouteForEmbeddedVelocityTemplates(final ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildConstantExpression("<![CDATA[" + application.getContent() + "]]>")));
      return routeDefinition.toString();
   }
}
