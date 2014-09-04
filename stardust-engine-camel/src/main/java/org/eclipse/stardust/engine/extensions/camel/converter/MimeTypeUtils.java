package org.eclipse.stardust.engine.extensions.camel.converter;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for MimeType
 * @author Sabri.Bousselmi
 * @version $Revision: $
 */
public class MimeTypeUtils
{
   private static Properties mimeMap = new Properties();
   static
   {
      mimeMap.put("application/pdf", "pdf");
      mimeMap.put("application/postscript", "ai");
      mimeMap.put("image/tiff", "tiff");
      mimeMap.put("audio/x-aiff", "aifc");
      mimeMap.put("text/xml", "xml");
      mimeMap.put("text/xhtml", "xhtml");
      mimeMap.put("text/html", "html");
      mimeMap.put("image/jpg", "jpg");
      mimeMap.put("image/jpeg", "jpeg");
      mimeMap.put("image/x-png", "png");
      mimeMap.put("image/gif", "gif");
      mimeMap.put("text/rtf", "rtf");
      mimeMap.put("application/msword", "doc");
      mimeMap.put("video/quicktime", "mov");
      mimeMap.put("video/x-ms-wmv", "wmf");
      mimeMap.put("video/x-msvideo", "avi");
      mimeMap.put("application/x-shockwave-flash", "swf");
      mimeMap.put("audio/x-ms-wma", "wma");
      mimeMap.put("audio/mpeg", "mp3");
      mimeMap.put("application/zip", "zip");
      mimeMap.put("text/plain", "txt");
      mimeMap.put("application/vnd.ms-powerpoint", "ppt");
      mimeMap.put("application/vnd.ms-excel", "xls");
      mimeMap.put(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "docx");
      mimeMap.put(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "pptx");
      mimeMap.put("application/rptdesign", "rptdesign");
      mimeMap.put("text/css", "css");
      mimeMap.put("text/csv", "csv");
      mimeMap.put("video/x-ms-asf", "asf");
      mimeMap.put("", "");
      mimeMap.put("audio/basic", "au");
      mimeMap.put("video/x-msvideo", "avi");
      mimeMap.put("image/bmp", "bmp");
      mimeMap.put("application/java", "class");
      mimeMap.put("application/x-csh", "csh");
      mimeMap.put("application/x-dvi", "dvi");
      mimeMap.put("application/java-archive", "jar");
      mimeMap.put("text/javascript", "js");

   }

   public static String getFileExtensionFromMimeType(String mimeType)
   {
      if (mimeMap.getProperty(mimeType) == null)
      {
         return StringUtils.EMPTY;
      }
      return mimeMap.getProperty(mimeType);
   }

   public static String getBodyPartContentType(MimeMultipart mimeMultipart)
         throws MessagingException
   {
      for (int i = 0; i < mimeMultipart.getCount(); i++)
      {
         BodyPart bodyPart = mimeMultipart.getBodyPart(i);
         // ignore attachment
         if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()))
         {
            return bodyPart.getContentType();
         }
      }
      return StringUtils.EMPTY;
   }

   public static String getExtensionFromBodyPartContentType(MimeMultipart mimeMultipart)
   {
      try
      {
         String bodyPartContentType = MimeTypeUtils.getBodyPartContentType(mimeMultipart);
         
         if(bodyPartContentType.contains(";"))
         {
            bodyPartContentType = bodyPartContentType.split(";")[0];
         }
         
         if (!bodyPartContentType.isEmpty())
         {
            return MimeTypeUtils.getFileExtensionFromMimeType(bodyPartContentType);
         }
      }
      catch (MessagingException e)
      {
         throw new RuntimeException("Failed retrieving content type.", e);
      }
      return "";
   }
}
