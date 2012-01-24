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
package org.eclipse.stardust.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author Marc Gille
 * @version $Revision$
 */
public class HMAC
{
   public static final String SHA1 = "SHA-1";
   public static final String MD5 = "MD5";
   public static final String UTF8_ENCODING = "UTF-8";

   private String hashType;
   private static MessageDigest myDigest;

   /**
    * Creates new instance for HMAC hashing.
    */
   public HMAC(String hashType)
         throws java.security.NoSuchAlgorithmException, NoSuchProviderException
   {
      this.changeHashType(hashType);
   }

   /**
    * Creates new instance for HMAC hashing.
    */
   public HMAC()
         throws java.security.NoSuchAlgorithmException, NoSuchProviderException
   {
      this.changeHashType(SHA1);
   }

   protected void changeHashType(String hashType)
         throws java.security.NoSuchAlgorithmException,
         NoSuchProviderException
   {

      if (hashType == null)
      {
         throw new RuntimeException("Hash Type may not be null");
      }

      if ((hashType.equals(SHA1)) || (hashType.equals(MD5)))
      {
         this.hashType = hashType;
         try
         {
            myDigest = MessageDigest.getInstance(hashType);
         }
         catch (NoSuchAlgorithmException e) 
         {
            Object bouncyCastleProvider = Reflect.createInstance(
                  "org.bouncycastle.jce.provider.BouncyCastleProvider", 
                  this.getClass().getClassLoader());
            if(bouncyCastleProvider != null)
            {
               Security.addProvider((Provider)bouncyCastleProvider);
               myDigest = MessageDigest.getInstance(hashType, "BE");
            }
            else
            {
               throw e;
            }
         }
      }
      else
      {
         throw new java.security.NoSuchAlgorithmException(
               "Algorithm must be 'SHA-1' or 'MD5'");
      }
   }

   /**
    *
    */
   public String getAlgorithm()
   {
      return hashType;
   }

   /**
    *
    */
   public byte[] hash(byte[] key, byte[] text)
         throws java.security.NoSuchAlgorithmException
   {
      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      byte[] pad = new byte[64];
      byte[] tempDigest = new byte[20];
      byte[] tempKey;

      if (key.length <= pad.length)
      {
         tempKey = key;
      }
      else
      {
         myDigest.reset();
         tempKey = myDigest.digest(key);
      }

      Arrays.fill(pad, (byte) (0x36));

      for (int i = 0; i < tempKey.length; ++i)
      {
         pad[i] ^= tempKey[i];
      }

      myDigest.reset();
      myDigest.update(pad);
      tempDigest = myDigest.digest(text);

      Arrays.fill(pad, (byte) (0x5c));

      for (int i = 0; i < tempKey.length; ++i)
      {
         pad[i] ^= tempKey[i];
      }

      myDigest.reset();
      myDigest.update(pad);
      tempDigest = myDigest.digest(tempDigest);

      return tempDigest;
   }

   /**
    *
    */
   protected byte[] hash(String key, String text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      return this.hash(
            key.getBytes(UTF8_ENCODING), text.getBytes(UTF8_ENCODING));
   }

   /**
    * Returns a string representing the parameters of this hashing.
    */
   protected String getPropertiesString()
   {
      return hashType;
   }

   /**
    * Hashes the text in <tt>text</tt> using the "salt" <tt>key</tt> and
    * prepends additional information on the hashing key. The hash value
    * is within parenthesis and is encoded in HEX.
    */
   public String hashToString(long salt, String key)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      if (key == null)
      {
         throw new RuntimeException("passed key may not be null");
      }

      byte[] _saltArray = {(byte) (salt >>> 24), 
                           (byte) (salt >>> 16), 
                           (byte) (salt >>> 8),
                           (byte) salt};

      byte[] hash = this.hash(key.getBytes(UTF8_ENCODING), _saltArray);
      String props = getPropertiesString();
      StringBuilder hashHexString = new StringBuilder(hash.length + props.length() + 2);
      hashHexString.append(props).append("{");
      for (int i = 0; i < hash.length; i++)
      {
         int value = (hash[i] & 0x7F) + (hash[i] < 0 ? 128 : 0);
         if(value < 16)
         {
            hashHexString.append("0");
         }
         hashHexString.append(Integer.toHexString(value).toUpperCase());
      }
      hashHexString.append("}");
      return hashHexString.toString();
   }

   /**
    *
    */
   protected byte[] hash(String key, byte[] text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      if (key == null)
      {
         throw new RuntimeException("passed key may not be null");
      }

      return hash(key.getBytes(UTF8_ENCODING), text);
   }

   /**
    *
    */
   protected byte[] hash(byte[] key, String text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      return hash(key, text.getBytes(UTF8_ENCODING));
   }

   /**
    *
    */
   protected byte[] hash(StringBuffer key, StringBuffer text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      
      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      return this.hash(key.toString(), text.toString());
   }

   /**
    *
    */
   protected byte[] hash(StringBuffer key, byte[] text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {
      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      return this.hash(key.toString(), text);
   }

   /**
    *
    */
   protected byte[] hash(byte[] key, StringBuffer text)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {

      if ((key == null) || (text == null))
      {
         throw new RuntimeException(
               "passed key/text may not be null, key: " + key
               + " text: " + text);
      }

      return this.hash(key, text.toString());
   }

   /**
    * Checks, wether the string <tt>hash</tt> is (likely) created by this class.
    */
   public boolean isHashed(String hash)
   {
      if (hash == null)
      {
         throw new RuntimeException(
               "passed hash may not be null, hash: " + hash);
      }

      int _start = hash.indexOf('{');
      int _end = hash.indexOf('}');

      if ((_start < 0 || _end < 0)
            || _end <= _start)
      {
         return false;
      }

      String hexHashValue = hash.substring(_start + 1, _end);
      String algorithm = hash.substring(0, _start).toUpperCase();
      if( !(MD5.equals(algorithm) || SHA1.equals(algorithm)))
      {
         return false;
      }
      
      if(hexHashValue.length() != 32)
      {
         return false;
      }

      return true;
   }

   /**
    * If the hash does not contain the properties information <tt>{...}</tt>
    * up front, <tt>hash</tt> is considered to be an unhashed password.
    */
   public boolean compare(long salt, String text, String hash)
         throws java.security.NoSuchAlgorithmException,
         java.io.UnsupportedEncodingException
   {

      if ((hash == null) || (text == null))
      {
         throw new RuntimeException(
               "passed hash/text may not be null, hash: " + hash
               + " text: " + text);
      }

      // Check, wether the hash is hashed; this is used to allow working with
      // existing unhashed password databases

      if (!isHashed(hash))
      {
         return text.equals(hash);
      }

      String _propertiesString;
      String _strippedHash;
      int _start = hash.indexOf('{');
      int _end = hash.indexOf('}');

      _strippedHash = hash.substring(_start + 1, _end);

      return hashToString(salt, text).equals(hash);
   }
}