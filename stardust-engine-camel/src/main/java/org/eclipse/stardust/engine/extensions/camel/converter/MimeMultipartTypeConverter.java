package org.eclipse.stardust.engine.extensions.camel.converter;

import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Converter;
import org.apache.camel.Handler;

/**
 * 
 * @author Sabri.Bousselmi
 * 
 */

@Converter
public class MimeMultipartTypeConverter {

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
			throws MessagingException, IOException {

		String mailBodyContent = null;
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			// ignore attachment from mail content
			if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
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
	private static String getText(Part part) throws MessagingException, IOException {
		if (part.isMimeType("text/*")) {
			String s = (String) part.getContent();
			return s;
		}

		if (part.isMimeType("multipart/alternative")) {
			// prefer html text over plain text
			Multipart mp = (Multipart) part.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null)
						text = getText(bp);
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null)
						return s;
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null)
					return s;
			}
		}

		return null;
	}

}
