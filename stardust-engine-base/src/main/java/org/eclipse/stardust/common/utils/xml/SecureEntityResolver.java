/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.common.utils.xml;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SecureEntityResolver implements EntityResolver
{
   private static final InputSource INPUT_SOURCE = new InputSource(new StringReader(""));

   public static final EntityResolver INSTANCE = new SecureEntityResolver(null);

   private EntityResolver delegate;

   public SecureEntityResolver(EntityResolver delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public InputSource resolveEntity(String publicId, String systemId)
         throws SAXException, IOException
   {
      if (delegate != null)
      {
         InputSource result = delegate.resolveEntity(publicId, systemId);
         if (result != null)
         {
            return result;
         }
      }
      return INPUT_SOURCE;
   }
}