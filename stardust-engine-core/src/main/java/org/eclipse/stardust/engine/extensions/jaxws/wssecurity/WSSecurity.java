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
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.utils.xml.jaxb.Jaxb;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.extensions.jaxws.app.AuthenticationParameters;
import org.eclipse.stardust.engine.extensions.jaxws.app.WSConstants;

/**
 * Utility class to handle WS-Security aspects.
 * <p>
 * This implementation supports UsernameToken Profile 1.0 as described in
 * http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0.pdf
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class WSSecurity
{
   public static final WSSecurity INSTANCE = new WSSecurity(); // singleton

   static final String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
   static final String WS_SECURITY_UTILITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

   private static final String WS_SECURITY_PREFIX = "wsse";
   private static final String WS_SECURITY_UTILITY_PREFIX = "wsu";

   private final static QName Security_QNAME = new QName(WS_SECURITY_NAMESPACE, "Security", WS_SECURITY_PREFIX);

   /**
    * Unique counter for nonce creation.
    */
   private AtomicLong counter;

   /**
    * Random number generator for nonce creation.
    */
   private Random rnd;

   /**
    * Initializes the singleton.
    */
   private WSSecurity()
   {
      rnd = new Random();
      counter = new AtomicLong(1);
   }

   /**
    * Adds the &lt;wsse:Security&gt; element to the SOAP message header with content as specified
    * in the UsernameToken Profile 1.0.
    *
    * @param message the outgoing SOAP message.
    * @param parameters authentication parameters.
    *
    * @throws SOAPException if WS-Security elements could not be appended to the soap message header.
    * @throws JAXBException if JAXB is improperly configured.
    * @throws PublicException if no user name was specified.
    * @throws UnsupportedEncodingException if the UTF-8 character set is not supported by the platform (should never happen).
    * @throws NoSuchAlgorithmException if the SHA-1 hashing algorithm is not supported by the platform.
    * @throws DatatypeConfigurationException if javax.xml.datatype.DatatypeFactory is not properly configured.
    */
   public void setWSSHeaders(SOAPHeader header, AuthenticationParameters parameters)
         throws SOAPException, JAXBException, UnsupportedEncodingException, NoSuchAlgorithmException, DatatypeConfigurationException
   {
      if (parameters != null && WSConstants.WS_SECURITY_AUTHENTICATION.equals(parameters.getMechanism()))
      {
         String user = parameters.getUsername();
         if (user == null)
         {
            throw new PublicException(
                  BpmRuntimeError.IPPWS_WS_SECURITY_AUTHENTICATION_REQUIRES_USERNAME
                        .raise());
         }

         registerPrefix(header, WSSecurity.WS_SECURITY_NAMESPACE, WSSecurity.WS_SECURITY_PREFIX);
         registerPrefix(header, WSSecurity.WS_SECURITY_UTILITY_NAMESPACE, WSSecurity.WS_SECURITY_UTILITY_PREFIX);

         String nonce = createNonce();

         Security security = Security.newInstance(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(header.getNamespaceURI()));
         security.timestamp = new Timestamp();
         security.usernameToken = new UsernameToken();
         security.usernameToken.username = parameters.getUsername();
         security.usernameToken.password = WSConstants.WS_PASSWORD_DIGEST.equals(parameters.getVariant())
               ? new Password.Digest(nonce, security.timestamp, parameters.getPassword())
               : new Password.Text(parameters.getPassword());
         security.usernameToken.nonce = new Nonce(nonce);
         security.usernameToken.created = security.timestamp.created;

         Jaxb.marshall(header, new JAXBElement(Security_QNAME, security.getClass(), null, security));
      }
   }

   private String createNonce()
   {
      int size = rnd.nextInt(8) + 12;
      StringBuilder sb = new StringBuilder(size);
      for (int i = 0; i < size; i++)
      {
         sb.append((char) (rnd.nextInt(128 - 33) + 33));
      }
      sb.append(counter.getAndIncrement());
      return sb.toString();
   }

   private void registerPrefix(SOAPElement header, String namespace, String prefix)
         throws SOAPException
   {
      if (header.lookupPrefix(namespace) == null)
      {
         header.addNamespaceDeclaration(prefix, namespace);
      }
   }
}
