package org.eclipse.stardust.engine.extensions.camel.core.app.templating;

import static org.eclipse.stardust.engine.extensions.camel.Util.getAttributeValue;

import java.io.Serializable;
import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IApplication;

public class ApplicationWrapper implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 324662104620451062L;

   private static final String getFormat(IApplication application)
   {
      return getAttributeValue("stardust:templatingIntegrationOverlay::format",
            application);
   }

   private static final String getLocation(IApplication application)
   {
      return getAttributeValue("stardust:templatingIntegrationOverlay::location",
            application);
   }

   private static final String getContent(IApplication application)
   {
      return getAttributeValue("stardust:templatingIntegrationOverlay::content",
            application);
   }

   private static final String getTemplate(IApplication application)
   {
      return getAttributeValue("stardust:templatingIntegrationOverlay::template",
            application);
   }

   private static final String getOutputName(IApplication application)
   {
      return getAttributeValue("stardust:templatingIntegrationOverlay::outputName",
            application);
   }

   private static final Boolean isConvertToPdf(IApplication application)
   {
      boolean value = false;
      if (application.getAllAttributes()
            .containsKey("stardust:templatingIntegrationOverlay::convertToPdf"))
         value = (Boolean)getAttributeValue("stardust:templatingIntegrationOverlay::convertToPdf",
               application);
      return value;
   }

   private String format;

   private String location;

   private String content;

   private String template;

   private String outputName;

   private Boolean convertToPdf;

   Iterator< ? > allInAccessPoints;

   Iterator< ? > allOutAccessPoints;

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

   public ApplicationWrapper(IApplication application)
   {
      this.format = getFormat(application);
      this.location = getLocation(application);
      this.content = getContent(application);
      this.template = getTemplate(application);
      this.outputName = getOutputName(application);
      this.convertToPdf = isConvertToPdf(application);
      this.allInAccessPoints = application.getAllInAccessPoints();
      this.allOutAccessPoints = application.getAllOutAccessPoints();
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
