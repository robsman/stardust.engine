package org.eclipse.stardust.engine.extensions.jaxws.wssecurity;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlValue;

/**
 * JAXB class implementing wsse:Nonce element.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
class Nonce
{
   /**
    * Hardcoded value specified by the WS-I Basic Profile 1.0.
    */
   @XmlAttribute(name = "EncodingType")
   final String encodingType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

   /**
    * Binary value of the nonce.
    */
   @XmlSchemaType(name = "base64Binary")
   @XmlValue
   byte[] value;

   /* JAXB doesn't accept classes without a default constructor */
   @SuppressWarnings("unused")
   private Nonce()
   {
   }
   
   /**
    * Creates a new Nonce element.
    * 
    * @param nonce the string specifying the Nonce value.
    * 
    * @throws UnsupportedEncodingException if UTF-8 character set is not supported by the platform (should never happen).
    */
   Nonce(String nonce) throws UnsupportedEncodingException
   {
      value = nonce.getBytes("UTF-8");
   }
}
