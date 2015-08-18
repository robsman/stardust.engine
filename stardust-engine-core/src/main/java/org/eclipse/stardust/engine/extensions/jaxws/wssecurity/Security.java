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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.soap.SOAPConstants;

/**
 * Abstract JAXB class implementing wsse:Security element.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
abstract class Security
{
   static Security newInstance(boolean forSOAP12)
   {
      return forSOAP12 ? new Security12() : new Security11();
   }
   
   /**
    * The wsu:Timestamp element.
    */
   @XmlElement(name = "Timestamp", namespace = WSSecurity.WS_SECURITY_UTILITY_NAMESPACE)
   Timestamp timestamp;

   /**
    * The wsse:UsernameToken element.
    */
   @XmlElement(name = "UsernameToken", namespace = WSSecurity.WS_SECURITY_NAMESPACE)
   UsernameToken usernameToken;

   /**
    * JAXB class implementing wsse:Security element for SOAP 1.1 envelopes.
    * 
    * @author Florin.Herinean
    * @version $Revision: $
    */
   static class Security11 extends Security
   {
      /**
       * Hardcoded required attribute in the SOAP 1.1 envelope namespace.
       */
      @XmlAttribute(name = "mustUnderstand", namespace = SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)
      final String mustUnderstand = "1";
   }
   
   /**
    * JAXB class implementing wsse:Security element for SOAP 1.2 envelopes.
    * 
    * @author Florin.Herinean
    * @version $Revision: $
    */
   static class Security12 extends Security
   {
      /**
       * Hardcoded required attribute in the SOAP 1.2 envelope namespace.
       */
      @XmlAttribute(name = "mustUnderstand", namespace = SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)
      final String mustUnderstand = "1";
   }
}
