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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

/**
 * @author Florin.Herinean
 */
public class CacheInputStream extends DataInputStream
{
   public CacheInputStream(byte[] bytes)
   {
      super(new ByteArrayInputStream(bytes));
   }

   public String readString() throws IOException
   {
      short len = readShort();
      if (len < 0)
      {
         return null;
      }
      char[] chars = new char[len];
      for (int i = 0; i < len; i++)
      {
         chars[i] = readChar();
      }
      return new String(chars);
   }

   public Date readDate() throws IOException
   {
      long date = readLong();
      return date == Long.MIN_VALUE ? null : new Date(date);
   }
}
