/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.common;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class TestBean2
{
   public String complete(String person)
   {
      StringBuffer address = new StringBuffer();

      address.append("<Address>");
      address.append("<addrLine1>addrLine1</addrLine1>");
      address.append("<addrLine2>addrLine2</addrLine2>");
      address.append("<zipCode>zipCode</zipCode>");
      address.append("<city>city</city>");
      address.append("</Address>");

      return address.toString();
   }
}
