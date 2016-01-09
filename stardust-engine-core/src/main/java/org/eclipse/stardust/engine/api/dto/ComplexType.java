/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.engine.core.model.beans.XMLConstants;

public class ComplexType extends HashMap
{
   private static final long serialVersionUID = 1L;

   public static final QName XSI_TYPE = new QName(XMLConstants.NS_XSI, "type");

   private Map<QName, String> properties;

   public ComplexType()
   {
   }

   public ComplexType(Map<QName, String> properties)
   {
      this.properties = properties;
   }

   public String getProperty(QName name)
   {
      return properties == null ? null : properties.get(name);
   }

   public boolean hasProperty(QName name)
   {
      return properties == null ? false : properties.containsKey(name);
   }

   public String toString()
   {
      return properties == null ? super.toString() : "[" + super.toString() + properties.toString() + "]";
   }

   public static ComplexType withXSIType(QName type)
   {
      return new ComplexType(Collections.singletonMap(ComplexType.XSI_TYPE, type.toString()));
   }
}