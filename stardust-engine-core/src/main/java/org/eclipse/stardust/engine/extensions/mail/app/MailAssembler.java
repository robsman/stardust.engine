/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.mail.app;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;


public class MailAssembler
{
   private static final Logger trace = LogManager.getLogger(MailAssembler.class);
   
   private String mailHost;
   private String fromSpec;
   private String toSpec;
   private String ccSpec;
   private String bccSpec;
   private String priority;
   private String subject;
   private String plainTextTemplate;
   private boolean useHTML;
   private String htmlHeader;
   private String htmlTemplate;
   private String htmlFooter;
   private boolean createProcessHistoryLink;
   private boolean mailResponse;
   private Object[] inputValues;
   private Map outputValueSetMap;
   private String rootURL;
   private long processInstanceOID;
   private long activityInstanceOID;
   private List attachmentList;
   private MimeMessage msg;

   public MailAssembler(String mailHost, String from, String to,
         String cc, String bcc, String priority,
         String subject, String plainTextTemplate, boolean useHTML,
         String htmlHeader, String htmlTemplate, String htmlFooter,
         boolean createProcessHistoryLink, boolean mailResponse,
         Object[] inputValues, Map outputValueSetMap, String rootURL,
         long processInstanceOID, long activityInstanceOID, List attachments)
   {
      super();

      this.mailHost = mailHost;
      this.fromSpec = from;
      this.toSpec = to;
      this.ccSpec = cc;
      this.bccSpec = bcc;
      this.priority = priority;
      this.subject = subject;
      this.plainTextTemplate = plainTextTemplate;
      this.useHTML = useHTML;
      this.htmlHeader = htmlHeader;
      this.htmlTemplate = htmlTemplate;
      this.htmlFooter = htmlFooter;
      this.createProcessHistoryLink = createProcessHistoryLink;
      this.mailResponse = mailResponse;
      this.inputValues = inputValues;
      this.outputValueSetMap = outputValueSetMap;
      this.rootURL = rootURL;
      this.processInstanceOID = processInstanceOID;
      this.activityInstanceOID = activityInstanceOID;
      this.attachmentList = attachments;
   }

   public void sendMail() throws MessagingException
   {
      prepareMsg();

      // root level Multipart 
      Multipart mp;

      if (attachmentList.equals(Collections.EMPTY_LIST))
      {
         // No attachments. Create Multipart with alternative content text/html
         mp = createAlternativeMultipart(); 
      }
      else
      {
         // Create Multipart containing mixed content: attachments + text/html
         // The mixed Multipart will wrap an alternative Multipart containing text/html 
         // all part of a mixed Multipart will be display in the email client
         mp = new MimeMultipart("mixed");

         // add alternative Multipart (actual message) first
         MimeBodyPart mbp1 = new MimeBodyPart();
         mbp1.setContent(createAlternativeMultipart());
         mp.addBodyPart(mbp1);

         // add MimeBodyPart containing mixed MimeMultipart with attachments
         MimeBodyPart mbp2 = new MimeBodyPart();
         Multipart attachmentsMP = addAttachmentsMP(new MimeMultipart("mixed"), attachmentList);
         mbp2.setContent(attachmentsMP);
         mp.addBodyPart(mbp2);
      }

      // Set the content for the message and transmit
      msg.setContent(mp);
      Transport.send(msg);
   }

   /** Sets smtp host, target mail adresses, priority, subject
    * @throws MessagingException
    */
   private void prepareMsg() throws MessagingException
   {
      Properties props = new Properties();

      props.put("mail.smtp.host", mailHost);
      props.put("mail.debug", "true");
      Session session = Session.getInstance(props, null);

      msg = new MimeMessage(session);

      InternetAddress fromAddress = new InternetAddress(fromSpec);

      // split multiple receivers

      InternetAddress[] toAddresses = parseRecipientList(toSpec);
      InternetAddress[] ccAddresses = parseRecipientList(ccSpec);
      InternetAddress[] bccAddresses = parseRecipientList(bccSpec);

      msg.setFrom(fromAddress);
      msg.addRecipients(Message.RecipientType.TO, toAddresses);
      if (0 < ccAddresses.length)
      {
         msg.addRecipients(Message.RecipientType.CC, ccAddresses);
      }
      if (0 < bccAddresses.length)
      {
         msg.addRecipients(Message.RecipientType.BCC, bccAddresses);
      }

      msg.addHeader("X-Priority", evaluateMailPriority(priority));
      msg.setSubject(subject);
   }

   /** Creates an "Alternative" Multipart message that may contain text and html.
    * MimeBodyParts in alternative Multiparts are displayed alternatively.
    * The email client settings specify which MimeBodyParts will be displayed.
    * @return alternative MimeMultipart possibly containing text and html MimeBodyParts
    * @throws MessagingException
    */
   private Multipart createAlternativeMultipart() throws MessagingException
   {
      Multipart mp = new MimeMultipart("alternative");

      // add text content
      MimeBodyPart text = new MimeBodyPart();
      text.setHeader("MIME-Version", "1.0");
      text.setHeader("Content-Type", text.getContentType());
      text.setText(getPlainTextContent());

      mp.addBodyPart(text);

      // add html content
      if (useHTML)
      {
         MimeBodyPart html = new MimeBodyPart();
         html.setHeader("MIME-Version", "1.0");
         html.setHeader("Content-Type", "text/html");
         html.setContent(getHTMLContent(), "text/html");

         mp.addBodyPart(html);
      }
      return mp;  
   }

   private Multipart addAttachmentsMP(Multipart mp, List attachmentlist) throws MessagingException
   {
      DocumentManagementServiceImpl dmsService = new DocumentManagementServiceImpl();
      for (Iterator iter = attachmentlist.iterator(); iter.hasNext();)
      {
         Object obj = iter.next();
         
         if (obj instanceof Document) 
         {
            Document doc = (Document) obj;
            String docId = doc.getId();

            byte[] docContent = dmsService.retrieveDocumentContent(docId);
            String contentType = doc.getContentType();

            if (trace.isDebugEnabled())
            {
               trace.debug("Content length in bytes: " + docContent.length);
            }
            
            MimeBodyPart mbp = new MimeBodyPart();
            DataSource ds = new ByteArrayDataSource(docContent, contentType);          
            mbp.setDataHandler(new DataHandler(ds));
            mbp.setFileName(doc.getName());
            mbp.setDisposition(MimeBodyPart.ATTACHMENT);
            mp.addBodyPart(mbp);
         }
      }        
      return mp;
   }

   private InternetAddress[] parseRecipientList(String recipientsSpec)
   throws AddressException
   {
      List/*<InternetAddress>*/ recipients = new ArrayList();

      if ( !StringUtils.isEmpty(recipientsSpec))
      {
         for (Iterator/*<String>*/ i = StringUtils.split(recipientsSpec, ';'); i.hasNext();)
         {
            recipients.add(new InternetAddress((String) i.next()));
         }
      }

      return (InternetAddress[]) recipients.toArray(new InternetAddress[recipients.size()]);
   }

   public BodyPart getFileBodyPart(String filename, String contentType)
   throws javax.mail.MessagingException
   {
      BodyPart bp = new MimeBodyPart();

      bp.setDataHandler(new DataHandler(new FileDataSource(filename)));

      return bp;
   }

   private String getPlainTextContent()
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append(getPlainTextMessageSection());
      buffer.append("\n");

      if (mailResponse)
      {
         buffer.append(getPlainTextMailResponseSection());
      }
      else
      {
         buffer.append(getPlainTextLinkSection());
      }

      return buffer.toString();
   }

   private String getHTMLContent()
   {
      StringBuffer buffer = new StringBuffer();

      buffer.append("<html>");
      buffer.append("<head>");
      if(htmlHeader != null)
      {
         buffer.append(htmlHeader);
      }
      buffer.append("</head>");
      buffer.append("<body>");
      buffer.append(getHTMLMessageSection());

      if (mailResponse)
      {
         buffer.append(getHTMLMailResponseSection());
      }
      else
      {
         buffer.append(getHTMLLinkSection());
      }

      if(htmlFooter != null)
      {
         buffer.append(htmlFooter);
      }
      buffer.append("</body>");
      buffer.append("</html>");

      return buffer.toString();
   }

   private String getPlainTextMessageSection()
   {
      StringBuffer buffer = new StringBuffer();
      MessageFormat format = new MessageFormat(plainTextTemplate);

      buffer.append(format.format(inputValues));

      return buffer.toString();
   }

   private String getHTMLMessageSection()
   {
      StringBuffer buffer = new StringBuffer();
      if(htmlTemplate != null)
      {
         MessageFormat format = new MessageFormat(htmlTemplate);

         buffer.append(format.format(inputValues));
      }

      return buffer.toString();
   }

   private String getPlainTextLinkSection()
   {
      StringBuffer buffer = new StringBuffer();

      // TODO NLS

      if (createProcessHistoryLink)
      {
         buffer.append(MailMessages.getString("linkForStatusRequest") + ": " + rootURL + "?"
               + "processInstanceOID=" + processInstanceOID
               + "&activityInstanceOID=" + activityInstanceOID
               + "&investigate=true\n");
      }

      for (Iterator iterator = outputValueSetMap.keySet().iterator(); iterator
      .hasNext();)
      {
         String outputValue = (String) iterator.next();

         buffer.append(MailMessages.getString("linkFor") + " " + outputValueSetMap.get(outputValue) + ": "
               + rootURL + "?" + "processInstanceOID=" + processInstanceOID
               + "&activityInstanceOID=" + activityInstanceOID
               + "&outputValue=" + outputValue + "\n");
      }

      return buffer.toString();
   }

   private String getPlainTextMailResponseSection()
   {
      StringBuffer buffer = new StringBuffer();

      // TODO NLS

      buffer
      .append(MailMessages.getString("replyMessage") + ".\n\n");

      if (createProcessHistoryLink)
      {
         buffer.append(MailMessages.getString("linkForStatusRequest") + ": " + rootURL + "?"
               + "processInstanceOID=" + processInstanceOID
               + "&activityInstanceOID=" + activityInstanceOID
               + "&investigate=true\n");
      }

      return buffer.toString();
   }

   private String getHTMLLinkSection()
   {
      StringBuffer buffer = new StringBuffer();

      // TODO NLS

      if (createProcessHistoryLink)
      {
         buffer
         .append("<a href=\""
               + rootURL
               + "?"
               + "processInstanceOID="
               + processInstanceOID
               + "&activityInstanceOID="
               + activityInstanceOID
               + "&investigate=true\" target=\"_blank\">" 
               + MailMessages.getString("statusRequest")
               + "</a><br><br>");
      }

      for (Iterator iterator = outputValueSetMap.keySet().iterator(); iterator
      .hasNext();)
      {
         String outputValue = (String) iterator.next();

         buffer.append(MailMessages.getString("linkFor") + ": <a href=\"" + rootURL + "?" + "processInstanceOID="
               + processInstanceOID + "&activityInstanceOID="
               + activityInstanceOID + "&outputValue=" + outputValue
               + "\" target=\"_blank\">"
               + outputValueSetMap.get(outputValue) + "</a><br>");
      }

      return buffer.toString();
   }

   private String getHTMLMailResponseSection()
   {
      StringBuffer buffer = new StringBuffer();

      // TODO NLS

      if (createProcessHistoryLink)
      {
         buffer
         .append("<a href=\""
               + rootURL
               + "?"
               + "processInstanceOID="
               + processInstanceOID
               + "&activityInstanceOID="
               + activityInstanceOID
               + "&investigate=true\" target=\"_blank\">" 
               + MailMessages.getString("statusRequst")
               + "</a><br><br>");
      }

      buffer
      .append("<p>" + MailMessages.getString("replyMessage") + ".</p>");

      return buffer.toString();
   }

   private String evaluateMailPriority (String priority)
   {
      String priorityString = "3";
      //Fill my map     
      String[] prioValues = {"Highest","High","Normal","Low","Lowest"};
      Map/*<String, String>*/ map=new HashMap();
      for (int x=0;x<prioValues.length;x++)
      {
         map.put(prioValues[x], String.valueOf(x + 1));
      }
      priorityString = (String) map.get(priority);
      return priorityString;
   }
}