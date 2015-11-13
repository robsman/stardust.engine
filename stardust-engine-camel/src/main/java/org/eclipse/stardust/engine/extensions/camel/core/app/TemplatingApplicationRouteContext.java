package org.eclipse.stardust.engine.extensions.camel.core.app;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;
import static org.eclipse.stardust.engine.extensions.camel.Util.*;
import static org.eclipse.stardust.engine.extensions.camel.core.RouteDefinitionBuilder.*;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.model.beans.AccessPointBean;
import org.eclipse.stardust.engine.extensions.camel.core.ProducerRouteContext;

public class TemplatingApplicationRouteContext extends ProducerRouteContext
{
   public static final Logger logger = LogManager.getLogger(TemplatingApplicationRouteContext.class);
   public TemplatingApplicationRouteContext(IApplication application, String partitionId,
         String camelContextId)
   {
      super(application, partitionId, camelContextId);
   }

   protected String generateRoute(IApplication application)
   {
      StringBuilder routeDefinition=new StringBuilder();
      String format=getFormat(application);
      if(format.equalsIgnoreCase(DOCX_FORMAT))
         routeDefinition.append(createRouteForXDocReportTemplates(application));
      else
         routeDefinition.append(createRouteForVelocityTemplates(application));
      
      
      if(StringUtils.isNotEmpty(format)){
         if(!format.equalsIgnoreCase(DOCX_FORMAT)){
            AccessPointBean defaultOutputAp=getAccessPointById("defaultOutputAp",application.getAllOutAccessPoints());
            if(isDocumentType(defaultOutputAp)){
               routeDefinition.append(setHeader("ippDmsDocumentName", buildSimpleExpression("header.CamelTemplatingOutputName")));
               routeDefinition.append(to("bean:documentHandler?method=toDocument"));
               routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("body")));
            }else if(isPrimitiveType(defaultOutputAp) && isStringType(defaultOutputAp)){
               routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("bodyAs(String)")));
            }else{
               routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("body")));
            }
         }
      }
      
      return routeDefinition.toString();
   }

   private String createRouteForVelocityTemplates(IApplication application){
      
      StringBuilder routeDefinition=new StringBuilder();
      String format=getFormat(application);
      String location=getLocation(application);
      String template=getTemplate(application);
      String outputName=getOutputName(application);
      Boolean convertToPdf=isConvertToPdf(application);
      
      routeDefinition.append(process("customVelocityContextAppender"));
      
      if(StringUtils.isNotEmpty(location)){
         if(location.equalsIgnoreCase(EMBEDDED_LOCATION))
            routeDefinition.append(createRouteForEmbeddedVelocityTemplates(application));
         if(location.equalsIgnoreCase(DATA_LOCATION))
            routeDefinition.append(createRouteForVelocityTemplatesFromData(application));
      }
      routeDefinition.append(to(createUri(location,format, template, outputName, convertToPdf)));
      return routeDefinition.toString();
   }
   private String createRouteForXDocReportTemplates(IApplication application){
      StringBuilder routeDefinition=new StringBuilder();
      String format=getFormat(application);
      String location=getLocation(application);
      String template=getTemplate(application);
      String outputName=getOutputName(application);
      Boolean convertToPdf=isConvertToPdf(application);
      
      routeDefinition.append(process("customVelocityContextAppender"));
      if(StringUtils.isNotEmpty(location)){
         if(location.equalsIgnoreCase(DATA_LOCATION))
            routeDefinition.append(createHeaderContentForStrucutredAccessPoint(""));
      }

      routeDefinition.append(to(createUri(location,format, template, outputName, convertToPdf)));
      routeDefinition.append(setHeader("ippDmsDocumentName", buildSimpleExpression("header.CamelTemplatingOutputName")));
      routeDefinition.append(to("bean:documentHandler?method=toDocument"));
      routeDefinition.append(setHeader("defaultOutputAp", buildSimpleExpression("body")));
      
      return routeDefinition.toString();
   }
   
   private String createUri(String location, String format, String template, String outputName, boolean convertToPdf){
      StringBuilder uri=new StringBuilder();
      uri.append("templating:"+location+"?format="+format);
      if(StringUtils.isNotEmpty(template))
         uri.append("&amp;template="+template);
      if(StringUtils.isNotEmpty(outputName))
         uri.append("&amp;outputName="+outputName);
      if(convertToPdf)
         uri.append("&amp;convertToPdf="+convertToPdf);
      return uri.toString();
   }
   
   private String createRouteForVelocityTemplatesFromData(IApplication application){
      AccessPointBean defaultInAccessPoint=getAccessPointById("defaultInputAp",application.getAllInAccessPoints());
      if(isPrimitiveType(defaultInAccessPoint)){
         return createHeaderContentForPrimitiveAccessPoint("defaultInputAp");
      }else{
         return createHeaderContentForStrucutredAccessPoint("ippDmsDocumentContent");
      }
   }
   private String createHeaderContentForPrimitiveAccessPoint(String id){
      StringBuilder routeDefinition=new StringBuilder();
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent", buildSimpleExpression("header."+id)));
      return routeDefinition.toString();
   }
   private String createHeaderContentForStrucutredAccessPoint(String id){
      StringBuilder routeDefinition=new StringBuilder();
      routeDefinition.append(to("bean:documentHandler?method=retrieveContent"));
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent", buildSimpleExpression("header."+id)));
      return routeDefinition.toString();
   }

   private String createRouteForEmbeddedVelocityTemplates(IApplication application){
      StringBuilder routeDefinition=new StringBuilder();
      String content=getContent(application);
      routeDefinition.append(setHeader("CamelTemplatingTemplateContent", buildConstantExpression("<![CDATA["+content+"]]>")));
      return routeDefinition.toString();
   }
  private String getFormat(IApplication application){
     return getAttributeValue("stardust:templatingIntegrationOverlay::format",application);
  }
  private String getLocation(IApplication application){
     return getAttributeValue("stardust:templatingIntegrationOverlay::location",application);
  }
  private String getContent(IApplication application){
     return getAttributeValue("stardust:templatingIntegrationOverlay::content",application);
  }
  private String getTemplate(IApplication application){
     return getAttributeValue("stardust:templatingIntegrationOverlay::template",application);
  }
  private String getOutputName(IApplication application){
     return getAttributeValue("stardust:templatingIntegrationOverlay::outputName",application);
  }
  private Boolean isConvertToPdf(IApplication application){
     boolean value=false;
    if(application.getAllAttributes().containsKey("stardust:templatingIntegrationOverlay::convertToPdf")) 
       value=getAttributeValue("stardust:templatingIntegrationOverlay::convertToPdf",application);
     return value;
  }
  private Boolean isAutoStatup(IApplication application){
     return getAttributeValue("carnot:engine:camel::autoStartup",application);
  }
  //
}
