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
package org.eclipse.stardust.engine.core.compatibility.gui;

/**
 * Address is a test class for the test application and represents the city
 * and the zipcode of a persons address
 */
public class Address
{
   private String city;
   private int zipCode;

   /**
    * Gets the city
    */
   public String getCity()
   {
      return city;
   }

   /**
    * Sets the city
    */
   public void setCity(String city)
   {
      this.city = city;
   }

   /**
    * Gets the zipcode
    */
   public int getZipCode()
   {
      return zipCode;
   }

   /**
    * Sets the zipcode
    */
   public void setZipCode(int zipCode)
   {
      this.zipCode = zipCode;
   }
}
