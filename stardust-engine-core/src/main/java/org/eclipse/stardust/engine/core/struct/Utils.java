/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.struct;

import java.util.Date;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class Utils
{

   public static String getNodeName(Node node)
   {
      if (node.getLocalName() == null)
      {
         return node.getNodeName();
      }
      else
      {
         return node.getLocalName();
      }
   }

   public static Element convert(String xmlString)
   {
      if (xmlString != null)
      {
         Document document = XmlUtils.parseString(xmlString, true);
         return document.getDocumentElement();
      }
      return null;
   }

   public static Class getJavaTypeForTypedXPath(TypedXPath xPath)
   {
      Class clazz = String.class;
      if (xPath.getType() == BigData.SHORT)
      {
         clazz = Short.class;
      }
      else if (xPath.getType() == BigData.INTEGER)
      {
         clazz = Integer.class;
      }
      else if (xPath.getType() == BigData.LONG)
      {
         clazz = Long.class;
      }
      else if (xPath.getType() == BigData.BYTE)
      {
         clazz = Byte.class;
      }
      else if (xPath.getType() == BigData.BOOLEAN)
      {
         clazz = Boolean.class;
      }
      else if (xPath.getType() == BigData.DATE)
      {
         clazz = Date.class;
      }
      else if (xPath.getType() == BigData.FLOAT)
      {
         clazz = Float.class;
      }
      else if (xPath.getType() == BigData.DOUBLE)
      {
         clazz = Double.class;
      }
      else if (xPath.getType() == BigData.STRING || xPath.getType() == BigData.BIG_STRING)
      {
         clazz = String.class;
      }
      else if (xPath.getType() == BigData.PERIOD)
      {
         clazz = Period.class;
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.SDT_BIGDATA_TYPE_IS_NOT_SUPPORTED_YET.raise(xPath
                     .getType()));
      }

      return clazz;
   }
}
