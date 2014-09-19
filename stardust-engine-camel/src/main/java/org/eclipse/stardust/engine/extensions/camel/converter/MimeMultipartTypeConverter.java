package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

/**
 *
 * @author Sabri.Bousselmi
 *
 */

@Converter
public class MimeMultipartTypeConverter
{

   /**
    *
    * @param mimeMultipart
    * @return Convert MimeMultipart To String
    * @throws MessagingException
    * @throws IOException
    */
   @Converter
   @Handler
   public static String mimeMultipartToString(MimeMultipart mimeMultipart)
         throws MessagingException, IOException
   {

      String mailBodyContent = null;
      for (int i = 0; i < mimeMultipart.getCount(); i++)
      {
         BodyPart bodyPart = mimeMultipart.getBodyPart(i);
         // ignore attachment from mail content
         if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()))
         {
            mailBodyContent = getText(bodyPart);
            return mailBodyContent;
         }
      }
      return mailBodyContent;
   }

   /**
    *
    * @param part
    * @return The text content of the message.
    * @throws MessagingException
    * @throws IOException
    */
   private static String getText(Part part) throws MessagingException, IOException
   {
      if (part.isMimeType("text/*"))
      {
         String s = (String) part.getContent();
         return s;
      }

      if (part.isMimeType("multipart/alternative"))
      {
         // prefer html text over plain text
         Multipart mp = (Multipart) part.getContent();
         String text = null;
         for (int i = 0; i < mp.getCount(); i++)
         {
            Part bp = mp.getBodyPart(i);
            if (bp.isMimeType("text/plain"))
            {
               if (text == null)
                  text = getText(bp);
               continue;
            }
            else if (bp.isMimeType("text/html"))
            {
               String s = getText(bp);
               if (s != null)
                  return s;
            }
            else
            {
               return getText(bp);
            }
         }
         return text;
      }
      else if (part.isMimeType("multipart/*"))
      {
         Multipart mp = (Multipart) part.getContent();
         for (int i = 0; i < mp.getCount(); i++)
         {
            String s = getText(mp.getBodyPart(i));
            if (s != null)
               return s;
         }
      }

      return null;
   }

   private static InternetAddress[] createAddresses(String... addresses)
         throws AddressException
   {
    //  List<InternetAddress> addressesList = new ArrayList<InternetAddress>();
      InternetAddress[] addressesList=new InternetAddress[addresses.length];
      int i=0;
      for (String address : addresses)
      {
         for (InternetAddress parsedAddress : InternetAddress.parse(address)){
            addressesList[i]=parsedAddress;
            i++;
         }
      }

      return addressesList;
   }

   @Converter
   public static MimeMessage StringToMimeMultipart(Object content, Exchange exchange)
         throws MessagingException
   {
      MimeMessage message = new MimeMessage((Session)null);

//      new ByteArrayDataSource(exchange.getIn()
//            .getBody(byte[].class), (String) exchange.getIn().getHeader("contentType"))
//      multipart/mixed

      String subject = (String) exchange.getIn().getHeader("subject");

      InternetAddress[] to = null;
      if(exchange.getIn().getHeader("to")!=null)
       to = createAddresses((String) exchange.getIn().getHeader("to"));
      InternetAddress[] cc = null;
      if(exchange.getIn().getHeader("cc")!=null)
       cc = createAddresses((String) exchange.getIn().getHeader("cc"));
      InternetAddress[] bcc = null;
      if(exchange.getIn().getHeader("bcc")!=null)
      bcc = createAddresses((String) exchange.getIn().getHeader("bcc"));

      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setText(exchange.getIn().getBody(String.class));
      if(exchange.getIn().getHeader("contentType")!=null && ((String)exchange.getIn().getHeader("contentType")).equalsIgnoreCase("text/plain"))
      bodyPart.setHeader("content-type",(String) exchange.getIn().getHeader("contentType"));
      else{

         bodyPart.setHeader("content-type","text/html; charset=utf-8");
      }
      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(bodyPart);

      if(!exchange.getIn().getAttachments().isEmpty()){
         for(String fileName:exchange.getIn().getAttachments().keySet()){
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setFileName(fileName);
            attachment.setDataHandler(exchange.getIn().getAttachments().get(fileName));
            multipart.addBodyPart(attachment);
         }
      }


      message.setContent(multipart);

      message.setFrom(new InternetAddress((String) exchange.getIn().getHeader("from")));
      message.setRecipients(Message.RecipientType.TO, to);
      message.setRecipients(Message.RecipientType.CC, cc);
      message.setRecipients(Message.RecipientType.BCC, bcc);
      message.setSubject(subject);



      return message;
   }
}
