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
package org.eclipse.stardust.engine.core.struct.spi;

import org.w3c.dom.DOMImplementation;

/**
 * Provides a thread local keeping an instance of org.w3c.dom.DOMImplementation
 */
public class DOMImplementationThreadLocal
{

   private static ThreadLocal instance = new ThreadLocal()
   {
      protected Object initialValue()
      {
         javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         try
         {
            return factory.newDocumentBuilder().getDOMImplementation();
         }
         catch (javax.xml.parsers.ParserConfigurationException e)
         {
            throw new RuntimeException(e);
         }
      }
   };

   public static DOMImplementation get()
   {
      return (DOMImplementation) instance.get();
   }
}
