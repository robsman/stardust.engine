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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.reflect.Reflect;


public class DesEncrypter {
	private static final String PBEWITH_MD5AND_DES = "PBEWithMD5AndDES";

	Cipher ecipher;

	Cipher dcipher;

	// 8-byte Salt
	byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
			(byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

	// Iteration count
	int iterationCount = 19;

	public DesEncrypter(String passPhrase) {
		try {
			initCiphers(passPhrase);
		} catch (Throwable ex3) {
			ex3.printStackTrace();
		}
	}

   private void initCiphers(String passPhrase) throws InvalidKeySpecException,
         NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
         InvalidAlgorithmParameterException
   {
      KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
      		iterationCount);
      SecretKey key = SecretKeyFactory.getInstance(PBEWITH_MD5AND_DES)
      		.generateSecret(keySpec);
      ecipher = Cipher.getInstance(PBEWITH_MD5AND_DES);
      dcipher = Cipher.getInstance(PBEWITH_MD5AND_DES);
      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
      		iterationCount);
      ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
   }

	public String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return new String(Base64.encode(enc));
		} catch (javax.crypto.BadPaddingException e) {
		   e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
		   e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		   e.printStackTrace();
		}
		return null;
	}

	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
			byte[] dec = Base64.decode(str.getBytes());

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (javax.crypto.BadPaddingException e) {
		   e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
		   e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		   e.printStackTrace();
		}
		return null;
	}
}
