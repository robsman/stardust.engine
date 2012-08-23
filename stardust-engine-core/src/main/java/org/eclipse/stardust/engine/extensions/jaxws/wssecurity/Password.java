/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.jaxws.wssecurity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Common interface for the wsse:Password implementations.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
interface Password
{
   /**
    * JAXB class implementing wsse:Password element in the variant PasswordText.
    * 
    * @author Florin.Herinean
    * @version $Revision: $
    */
   static class Text implements Password
   {
      /**
       * Hardcoded value specified by the UsernameToken Profile 1.0
       */
      @XmlAttribute(name = "Type")
      final String type = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";

      /**
       * The password as plain text.
       */
      @XmlValue
      String value;

      /* JAXB doesn't accept classes without a default constructor */
      @SuppressWarnings("unused")
      private Text()
      {
      }

      /**
       * Creates a new PasswordText.
       * 
       * @param password the plain text password.
       */
      Text(String password)
      {
         value = password;
      }
   }

   /**
    * JAXB class implementing wsse:Password element in the variant PasswordDigest.
    * 
    * @author Florin.Herinean
    * @version $Revision: $
    */
   static class Digest implements Password
   {
      /**
       * Hardcoded value specified by the UsernameToken Profile 1.0
       */
      @XmlAttribute(name = "Type")
      final String type = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest";

      /**
       * The digest of the password.
       */
      @XmlSchemaType(name = "base64Binary")
      @XmlValue
      byte[] value;

      /* JAXB doesn't accept classes without a default constructor */
      @SuppressWarnings("unused")
      private Digest()
      {
      }

      /**
       * Creates a new PasswordDigest.
       * 
       * @param nonce the nonce for the digest in plain text.
       * @param timestamp the timestamp when the security header was created.
       * @param password the plain text password.
       * 
       * @throws UnsupportedEncodingException if the UTF-8 character set is not supported by the platform (should never happen).
       * @throws NoSuchAlgorithmException if the SHA-1 hashing algorithm is not supported by the platform.
       */
      Digest(String nonce, Timestamp timestamp, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
      {
         String ref = nonce + timestamp.created.toXMLFormat() + password;
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         md.update(ref.getBytes("UTF-8"));
         value = md.digest();
      }
   }
}
