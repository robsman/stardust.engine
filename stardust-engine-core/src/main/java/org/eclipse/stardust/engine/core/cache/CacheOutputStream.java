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
package org.eclipse.stardust.engine.core.cache;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * @author Florin.Herinean
 */
public class CacheOutputStream extends DataOutputStream
{
   public CacheOutputStream()
   {
      super(new ByteArrayOutputStream());
   }

   public byte[] getBytes()
   {
      return ((ByteArrayOutputStream) out).toByteArray();
   }

   public void writeString(String s) throws IOException
   {
      if (s == null)
      {
         writeShort(-1);
         return;
      }
      writeShort(s.length());
      writeChars(s);
   }

   public void writeDate(Date date) throws IOException
   {
      writeLong(date == null ? Long.MIN_VALUE : date.getTime());
   }
}
