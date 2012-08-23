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

import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.stardust.common.utils.xml.jaxb.Jaxb;

/**
 * JAXB class implementing wsu:Timestamp element.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
class Timestamp
{
   /**
    * Hardcoded value since WS-I Basic Profile 1.0 allows only one wsu:Timestamp to be specified in the security header.
    */
   @XmlAttribute(name = "Id", namespace = WSSecurity.WS_SECURITY_UTILITY_NAMESPACE)
   final String id = "Timestamp-1";

   /**
    * The wsu:Created element.
    */
   @XmlElement(name = "Created", namespace = WSSecurity.WS_SECURITY_UTILITY_NAMESPACE)
   XMLGregorianCalendar created;

   /**
    * The wsu:Expires element.
    */
   @XmlElement(name = "Expires", namespace = WSSecurity.WS_SECURITY_UTILITY_NAMESPACE)
   XMLGregorianCalendar expires;

   /**
    * Creates a new Timestamp based on the current system time normalized to UTC where
    * wsu:Expires is 5 minutes after wsu:Created (recommended value).
    * 
    * @throws DatatypeConfigurationException if javax.xml.datatype.DatatypeFactory is not properly configured. 
    */
   Timestamp() throws DatatypeConfigurationException
   {
      DatatypeFactory datatypeFactory = Jaxb.getDatatypeFactory();
      XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
      created = xmlCalendar.normalize();
      xmlCalendar.add(datatypeFactory.newDuration(300000)); // 5 minutes to expire
      expires = xmlCalendar.normalize();
   }
}
