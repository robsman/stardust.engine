/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.stardust.common.Base64;

/**
 * 
 * @author Thomas.Wolfram
 *
 */
public class DefaultHashProvider implements HashProvider
{

   private static final String SHA512 = "SHA-512";

   private MessageDigest messageDigest;

   public DefaultHashProvider() throws NoSuchAlgorithmException
   {
      this.messageDigest = MessageDigest.getInstance(SHA512);
   }

   @Override
   public String getAlgorithm()
   {
      return SHA512;
   }

   @Override
   public byte[] hash(byte[] key, byte[] value)
   {
      if ((key == null) || (value == null))
      {
         throw new RuntimeException("passed key/text may not be null, key: " + key
               + " text: " + value);
      }

      byte[] digest;

      messageDigest.reset();
      messageDigest.update(key);
      digest = messageDigest.digest(value);

      return digest;
   }

   @Override
   public boolean compare(byte[] key, byte[] value, byte[] hash)
   {
      if ((hash == null) || (value == null))
      {
         throw new RuntimeException("passed hash/text may not be null, hash: " + hash
               + " text: " + value);
      }

      return hash(key, value).equals(hash);
   }

}
