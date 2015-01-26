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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.stardust.common.error.BaseErrorCase;
import org.eclipse.stardust.common.error.PublicException;


public final class Encrypter {
	private static String getKey() {
		return String.copyValueOf(new char[]
		                            { 't', 'h', 'e', '*', 'g', 'r', 'e', 'a', 't', '*', 'i', 'p', 'p', '*', 'e' ,'n' ,'c', 'r', 'y', 'p', 't', 'i', 'o', 'n',
		                                  '*', 'k', 'e', 'y'});
	}

	public static String encrypt(String stringToEncrypt) {
        DesEncrypter encrypter = new DesEncrypter(getKey());
        return encrypter.encrypt(stringToEncrypt);
	}

	public static String decrypt(String stringToDecrypt) {
        DesEncrypter encrypter = new DesEncrypter(getKey());
        return encrypter.decrypt(stringToDecrypt);
	}

	public static String decryptFromFile(String path) {
		String result = null;
		BufferedReader in = null;
		try {
			in  = new BufferedReader(new FileReader(path));
			result = in.readLine();
			return Encrypter.decrypt(result);
		} catch (FileNotFoundException e) {
			throw new PublicException(
	                  BaseErrorCase.BASE_PASSWORD_FILE_COULD_NOT_BE_FOUND_AT_THE_SPECIFIED_LOCATION.raise());
		} catch (IOException e) {
         throw new PublicException(
               BaseErrorCase.BASE_PASSWORD_FILE_COULD_NOT_BE_READ.raise());
		}
	}


}
