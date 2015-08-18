package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.io.Serializable;
import java.util.Date;

public class CorrespondenceCapableImpl implements CorrespondenceCapable, Serializable
{
   private static final long serialVersionUID = 4801463629810193269L;

   private String attachments;

   private String blindCarbonCopyRecipients;

   private String carbonCopyRecipients;

   private String faxNumber;

   private String recipients;

   private Date sendDate;

   private String sender;

   private String subject;

   private boolean emailEnabled;

   private boolean faxEnabled;

   public String getAttachments()
   {
      return attachments;
   }

   public void setAttachments(String attachments)
   {
      this.attachments = attachments;
   }

   public String getBlindCarbonCopyRecipients()
   {
      return blindCarbonCopyRecipients;
   }

   public void setBlindCarbonCopyRecipients(String bccRecipients)
   {
      blindCarbonCopyRecipients = bccRecipients;
   }

   public String getCarbonCopyRecipients()
   {
      return carbonCopyRecipients;
   }

   public void setCarbonCopyRecipients(String ccRecipients)
   {
      carbonCopyRecipients = ccRecipients;
   }

   public String getFaxNumber()
   {
      return faxNumber;
   }

   public void setFaxNumber(String faxNumber)
   {
      this.faxNumber = faxNumber;
   }

   public String getRecipients()
   {
      return recipients;
   }

   public void setRecipients(String recipients)
   {
      this.recipients = recipients;
   }

   public Date getSendDate()
   {
      return sendDate;
   }

   public void setSendDate(Date sendDate)
   {
      this.sendDate = sendDate;
   }

   public String getSender()
   {
      return sender;
   }

   public void setSender(String sender)
   {
      this.sender = sender;
   }

   public String getSubject()
   {
      return subject;
   }

   public void setSubject(String subject)
   {
      this.subject = subject;
   }

   public boolean isEmailEnabled()
   {
      return emailEnabled;
   }

   public void setEmailEnabled(boolean emailEnabled)
   {
      this.emailEnabled = emailEnabled;
   }

   public boolean isFaxEnabled()
   {
      return faxEnabled;
   }

   public void setFaxEnabled(boolean faxEnabled)
   {
      this.faxEnabled = faxEnabled;
   }

}
