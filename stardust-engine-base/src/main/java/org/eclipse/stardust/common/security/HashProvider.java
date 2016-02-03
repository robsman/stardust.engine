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

import java.security.NoSuchAlgorithmException;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;

/**
 * 
 * @author Thomas.Wolfram
 *
 */

@SPI(status = Status.Internal, useRestriction = UseRestriction.Internal)
public interface HashProvider
{

   /**
    * Factory for {@link HashProvider}.
    */
   public interface Factory
   {
      HashProvider getInstance() throws NoSuchAlgorithmException;
   }
   
   /**
    * Returns algorithm used in implementation
    * 
    * @return the string representation of the Hash Algorithm used in the Provider Implementation
    */
   public String getAlgorithm();

   /**
    * 
    * hashes a value with a key/salt
    * 
    * @param key
    * @param value
    * @return hashed value
    */
   public byte[] hash(byte[] key, byte[] value);


   /**
    * Compares a given string with an existing hash
    * 
    * @param key
    * @param value
    * @param hash
    * @return true, if hashed value of the string equals the existing hash
    */
   public boolean compare(byte[] key, byte[] value, byte[] hash);

}
