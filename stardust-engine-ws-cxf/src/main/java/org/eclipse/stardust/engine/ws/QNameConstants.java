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
package org.eclipse.stardust.engine.ws;

import javax.xml.namespace.QName;

public class QNameConstants
{
   public static final QName QN_CHAR = new QName("http://eclipse.org/stardust/ws/v2012a/api","Char");
   
   //public static final QName QN_CALENDAR = new QName("http://eclipse.org/stardust/ws/v2012a/api","Calendar");
   
   public static final QName QN_QNAME = new QName("http://www.w3.org/2001/XMLSchema",
         "QName");

   public static final QName QN_STRING = new QName("http://www.w3.org/2001/XMLSchema",
         "string");

   public static final QName QN_BOOLEAN = new QName("http://www.w3.org/2001/XMLSchema",
         "boolean");

   public static final QName QN_BYTE = new QName("http://www.w3.org/2001/XMLSchema",
         "byte");

   public static final QName QN_SHORT = new QName("http://www.w3.org/2001/XMLSchema",
         "short");

   public static final QName QN_INT = new QName("http://www.w3.org/2001/XMLSchema", "int");

   public static final QName QN_LONG = new QName("http://www.w3.org/2001/XMLSchema",
         "long");

   public static final QName QN_FLOAT = new QName("http://www.w3.org/2001/XMLSchema",
         "float");

   public static final QName QN_DOUBLE = new QName("http://www.w3.org/2001/XMLSchema",
         "double");

   public static final QName QN_DATETIME = new QName("http://www.w3.org/2001/XMLSchema",
         "dateTime");

   public static final QName QN_DATE = new QName("http://www.w3.org/2001/XMLSchema",
         "date");

   public static final QName QN_BASE64BINARY = new QName(
         "http://www.w3.org/2001/XMLSchema", "base64Binary");
}
