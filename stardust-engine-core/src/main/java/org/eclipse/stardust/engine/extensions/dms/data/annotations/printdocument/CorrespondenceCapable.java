package org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument;

import java.util.Date;

/**
 * Implementors of this interface are capable to hold EMail and fax correspondence information.
 *
 * @author Roland.Stamm
 */
public interface CorrespondenceCapable
{

   /**
    * @return Attachments name and version.
    */
   String getAttachments();

   void setAttachments(String attachments);

   /**
    * @return bcc recipients.
    */
   String getBlindCarbonCopyRecipients();

   void setBlindCarbonCopyRecipients(String bccRecipients);

   /**
    * @return cc recipients.
    */
   String getCarbonCopyRecipients();

   void setCarbonCopyRecipients(String ccRecipients);

   /**
    * @return A fax number.
    */
   String getFaxNumber();

   void setFaxNumber(String faxNumber);

   /**
    * @return The recipients
    */
   String getRecipients();

   void setRecipients(String recipients);

   /**
    * @return The date this document was sent.
    */
   Date getSendDate();

   void setSendDate(Date sendDate);

   /**
    * @return The sender
    */
   String getSender();

   void setSender(String sender);

   /**
    * @return The subject of the email or fax.
    */
   String getSubject();

   void setSubject(String subject);

   boolean isEmailEnabled();

   void setEmailEnabled(boolean emailEnabled);

   boolean isFaxEnabled();

   void setFaxEnabled(boolean faxEnabled);

}
