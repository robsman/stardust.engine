package org.eclipse.stardust.engine.extensions.camel.app.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * utility class for template configuration
 * 
 * @author Sabri.Bousselmi
 * @version $Revision: $
 */
public class TemplateConfigurationUtils
{
   public static final Logger logger = LogManager
         .getLogger(TemplateConfigurationUtils.class);

   public static List<TemplateConfiguration> toTemplateConfigurations(
         List<Map<String, Object>> configuration)
   {
      List<TemplateConfiguration> templateConfigurations = new ArrayList<TemplateConfiguration>();
      if (!configuration.isEmpty())
      {
         for (Map<String, Object> elt : configuration)
         {
            TemplateConfiguration templateConfiguration = new TemplateConfiguration();
            templateConfiguration.setTemplate((Boolean) elt.get("tTemplate"));
            templateConfiguration.setName((String) elt.get("tName"));
            templateConfiguration.setFormat((String) elt.get("tFormat"));
            templateConfiguration.setPath((String) elt.get("tPath"));
            templateConfiguration.setSource((String) elt.get("tSource"));
            templateConfigurations.add(templateConfiguration);
         }

      }
      return templateConfigurations;
   }

   @SuppressWarnings("unchecked")
   public static List<TemplateConfiguration> toTemplateConfigurations(
         Map<String, Object> documentRequest)
   {
      List<TemplateConfiguration> templateConfigurations = new ArrayList<TemplateConfiguration>();
      List<Map<String, Object>> documents = getDocuments(documentRequest);
      if (documents != null && !documents.isEmpty())
      {
         for (Map<String, Object> requestItem : documents)
         {
            if (IsAttachment(requestItem))
            {
               // generate template configuration
               templateConfigurations.add(createTemplateConfiguration(requestItem));
            }
         }
      }
      return templateConfigurations;
   }

   private static TemplateConfiguration createTemplateConfiguration(
         Map<String, Object> requestItem)
   {
      TemplateConfiguration templateConfiguration = new TemplateConfiguration();
      templateConfiguration.setTemplate(IsTemplate(requestItem));
      templateConfiguration.setName(getName(requestItem));
      templateConfiguration.setSource("repository");
      templateConfiguration.setPath(getTemplateId(requestItem));
      templateConfiguration.setFormat(IsConvertToPDF(requestItem) ? "pdf" : "plain");
      return templateConfiguration;
   }

   private static String getStringField(Map<String, Object> requestItem, String key)
   {

      String value = (String) requestItem.get(key);
      if (StringUtils.isNotEmpty((String) value))
      {
         return value;
      }
      return value;
   }

   private static boolean getBooleanField(Map<String, Object> requestItem, String key)
   {

      Object value = requestItem.get(key);
      if (value instanceof Boolean)
         return (Boolean) value;
      else
      {
         if (value != null)
         {
            return Boolean.parseBoolean(value.toString());
         }
         return false;
      }
   }

   public static boolean IsAttachment(Map<String, Object> requestItem)
   {
      return getBooleanField(requestItem, "IsAttachment");
   }

   public static boolean IsConvertToPDF(Map<String, Object> requestItem)
   {
      return getBooleanField(requestItem, "ConvertToPDF");
   }

   public static String getOutgoingDocumentId(Map<String, Object> requestItem)
   {
      return getStringField(requestItem, "OutgoingDocumentID");
   }

   public static String getTemplateId(Map<String, Object> requestItem)
   {
      return getStringField(requestItem, "TemplateID");
   }

   public static String getName(Map<String, Object> requestItem)
   {
      return getStringField(requestItem, "Name");
   }
   /**
    * TemplateID should null if user what to skip templating execution
    * 
    * @param requestItem
    * @return
    */
   public static boolean IsTemplate(Map<String, Object> requestItem)
   {
      boolean isTemplate = false;
      if (StringUtils.isEmpty(getOutgoingDocumentId(requestItem))
            && StringUtils.isNotEmpty(getTemplateId(requestItem)))
      {
         isTemplate = true;
      }
      if (StringUtils.isNotEmpty(getOutgoingDocumentId(requestItem))
            || StringUtils.isEmpty(getTemplateId(requestItem)))
      {
         isTemplate = false;
      }
      return isTemplate;
   }

   public static List<Map<String, Object>> getDocuments(
         Map<String, Object> documentRequest)
   {
      List<Map<String, Object>> documents = null;
      if (documentRequest != null)
         documents = (List<Map<String, Object>>) documentRequest.get("Documents");
      return documents;
   }

}
