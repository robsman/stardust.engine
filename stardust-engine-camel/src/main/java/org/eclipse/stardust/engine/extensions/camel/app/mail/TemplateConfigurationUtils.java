package org.eclipse.stardust.engine.extensions.camel.app.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
   public static final Logger logger = LogManager.getLogger(TemplateConfigurationUtils.class);

   @SuppressWarnings("unchecked")
   public static List<TemplateConfiguration> toTemplateConfigurations(Map<String, Object> documentRequest)
   {
      List<TemplateConfiguration> templateConfigurations = new ArrayList<TemplateConfiguration>();
      if (documentRequest.size() == 1)
      {
         List<Map<String, Object>> documents = (List<Map<String, Object>>) documentRequest
               .get("Documents");

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

   private static TemplateConfiguration createTemplateConfiguration(Map<String, Object> requestItem)
   {
      TemplateConfiguration templateConfiguration = new TemplateConfiguration();
      templateConfiguration.setTemplate((Boolean) requestItem.get("IsTemplate"));
      templateConfiguration.setName((String) requestItem.get("Name"));
      templateConfiguration.setSource("repository");
      templateConfiguration.setPath((String) requestItem.get("DocumentLocation"));
      templateConfiguration.setFormat((Boolean) requestItem.get("ConvertToPDF") == true ? "pdf" : "plain");
      return templateConfiguration;
   }

   public static boolean IsAttachment(Map<String, Object> requestItem)
   {
      Object IsAttachment = requestItem.get("IsAttachment");
      if (IsAttachment != null)
      {
         return Boolean.parseBoolean(IsAttachment.toString());
      }
      return false;
   }
}
