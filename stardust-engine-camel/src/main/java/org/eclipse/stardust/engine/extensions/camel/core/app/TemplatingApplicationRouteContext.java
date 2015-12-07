package org.eclipse.stardust.engine.extensions.camel.core.app;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DATA_LOCATION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.DOCX_FORMAT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.EMBEDDED_LOCATION;
import static org.eclipse.stardust.engine.extensions.camel.Util.getAccessPointById;
import static org.eclipse.stardust.engine.extensions.camel.Util.getAttributeValue;
import static org.eclipse.stardust.engine.extensions.camel.Util.isDocumentType;
import static org.eclipse.stardust.engine.extensions.camel.Util.isPrimitiveType;
import static org.eclipse.stardust.engine.extensions.camel.Util.isStringType;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.buildConstantExpression;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.buildSimpleExpression;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.process;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.setHeader;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.to;

import java.util.Iterator;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;

public class TemplatingApplicationRouteContext extends ProducerRouteContext
{
   public static final Logger logger = LogManager
         .getLogger(TemplatingApplicationRouteContext.class);

   public TemplatingApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   @Override
   protected String generateRoute(IApplication application)
   {

      return generateRoute(new ApplicationWrapper(application) );
   }
   
   public String generateRoute(ApplicationWrapper application)
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

   private String createRouteForVelocityTemplates(ApplicationWrapper application)
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
      routeDefinition
            .append(to(createUri(application.getLocation(), application.getFormat(), application.getTemplate(), application.getOutputName(), application.isConvertToPdf())));
      return routeDefinition.toString();
   }

   private String createRouteForXDocReportTemplates(ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(process("customVelocityContextAppender"));
      if (StringUtils.isNotEmpty(application.getLocation()))
      {
         if (application.getLocation().equalsIgnoreCase(DATA_LOCATION))
            routeDefinition.append(createHeaderContentForStrucutredAccessPoint(""));
      }

      routeDefinition
            .append(to(createUri(application.getLocation(), application.getFormat(), application.getTemplate(), application.getOutputName(), application.isConvertToPdf())));
      routeDefinition.append(setHeader("ippDmsDocumentName",
            buildSimpleExpression("header.CamelTemplatingOutputName")));
      routeDefinition.append(to("bean:documentHandler?method=toDocument"));
      routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("body")));

      return routeDefinition.toString();
   }

   private String createUri(String location, String format, String template,
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

   private String createRouteForVelocityTemplatesFromData(ApplicationWrapper application)
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

   private String createHeaderContentForPrimitiveAccessPoint(String id)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildSimpleExpression("header." + id)));
      return routeDefinition.toString();
   }

   private String createHeaderContentForStrucutredAccessPoint(String id)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(to("bean:documentHandler?method=retrieveContent"));
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildSimpleExpression("header." + id)));
      return routeDefinition.toString();
   }

   private String createRouteForEmbeddedVelocityTemplates(ApplicationWrapper application)
   {
      StringBuilder routeDefinition = new StringBuilder();
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent",
            buildConstantExpression("<![CDATA[" + application.getContent() + "]]>")));
      return routeDefinition.toString();
   }

   class ApplicationWrapper
   {
      private String getFormat(IApplication application)
      {
         return getAttributeValue("stardust:templatingIntegrationOverlay::format",
               application);
      }
      private String getLocation(IApplication application)
      {
         return getAttributeValue("stardust:templatingIntegrationOverlay::location",
               application);
      }

      private String getContent(IApplication application)
      {
         return getAttributeValue("stardust:templatingIntegrationOverlay::content",
               application);
      }

      private String getTemplate(IApplication application)
      {
         return getAttributeValue("stardust:templatingIntegrationOverlay::template",
               application);
      }

      private String getOutputName(IApplication application)
      {
         return getAttributeValue("stardust:templatingIntegrationOverlay::outputName",
               application);
      }

      private Boolean isConvertToPdf(IApplication application)
      {
         boolean value = false;
         if (application.getAllAttributes()
               .containsKey("stardust:templatingIntegrationOverlay::convertToPdf"))
            value = getAttributeValue("stardust:templatingIntegrationOverlay::convertToPdf",
                  application);
         return value;
      }
      private String format;

      private String location;

      private String content;

      private String template;

      private String outputName;

      private Boolean convertToPdf;

      Iterator<?> allInAccessPoints;

      Iterator<?> allOutAccessPoints;

      public ApplicationWrapper(String format, String location, String content,
            String template, String outputName, Boolean convertToPdf)
      {
         this.format = format;
         this.location = location;
         this.content = content;
         this.template = template;
         this.outputName = outputName;
         this.convertToPdf = convertToPdf;
      }

      public ApplicationWrapper(IApplication application){
         this.format = this.getFormat(application);
         this.location = this.getLocation(application);
         this.content = this.getContent(application);
         this.template = this.getTemplate(application);
         this.outputName = this.getOutputName(application);
         this.convertToPdf = this.isConvertToPdf(application);
         this.allInAccessPoints=application.getAllInAccessPoints();
         this.allOutAccessPoints=application.getAllOutAccessPoints();
      }
      
      public String getFormat()
      {
         return format;
      }

      public void setFormat(String format)
      {
         this.format = format;
      }

      public String getLocation()
      {
         return location;
      }

      public void setLocation(String location)
      {
         this.location = location;
      }

      public String getContent()
      {
         return content;
      }

      public void setContent(String content)
      {
         this.content = content;
      }

      public String getTemplate()
      {
         return template;
      }

      public void setTemplate(String template)
      {
         this.template = template;
      }

      public String getOutputName()
      {
         return outputName;
      }

      public void setOutputName(String outputName)
      {
         this.outputName = outputName;
      }

      public Boolean isConvertToPdf()
      {
         return convertToPdf;
      }

      public void setConvertToPdf(Boolean convertToPdf)
      {
         this.convertToPdf = convertToPdf;
      }
      public Iterator< ? > getAllInAccessPoints()
      {
         return allInAccessPoints;
      }
      public void setAllInAccessPoints(Iterator< ? > allInAccessPoints)
      {
         this.allInAccessPoints = allInAccessPoints;
      }
      public Iterator< ? > getAllOutAccessPoints()
      {
         return allOutAccessPoints;
      }
      public void setAllOutAccessPoints(Iterator< ? > allOutAccessPoints)
      {
         this.allOutAccessPoints = allOutAccessPoints;
      }
      
   }





  
}
