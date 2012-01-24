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
package org.eclipse.stardust.common;

import java.math.BigDecimal;

/**
 * Class to store a valid currency in.
 *
 * @author jmelzer
 * @version $Revision$
 */
class Currency
{
   public static final Currency EUR = new Currency("EUR", "Euro",
         new BigDecimal(1.0), 2);
   public static final Currency ATS = new Currency("ATS", "Öster. Schilling",
         new BigDecimal(13.7603), 2);
   public static final Currency BEF = new Currency("BEF", "Belg. Franc",
         new BigDecimal(40.3399), 2);
   public static final Currency DEM = new Currency("DM", "Deutsche Mark",
         new BigDecimal(1.95583), 2);
   public static final Currency ESP = new Currency("ESP", "Span. Peseta",
         new BigDecimal(166.386), 2);
   public static final Currency FIM = new Currency("FIM", "Finmark",
         new BigDecimal(5.94573), 2);
   public static final Currency FRF = new Currency("FRF", "Franz. Franc",
         new BigDecimal(6.55957), 2);
   public static final Currency IEP = new Currency("IEP", "Irisches Punt",
         new BigDecimal(0.787564), 2);
   public static final Currency ITL = new Currency("ITL", "Ital. Lira",
         new BigDecimal(1936.27), 0);
   public static final Currency LUF = new Currency("LUF", "Lux. Franc",
         new BigDecimal(40.3399), 2);
   public static final Currency NLG = new Currency("NLG", "Holl. Gulden",
         new BigDecimal(2.20371), 2);
   public static final Currency PTE = new Currency("PTE", "Port. Escudo",
         new BigDecimal(200.482), 2);

   String abbreviation;
   String type;
   BigDecimal euroExchangeRate;
   int digits;

   int identity;

   /**
    *	Constructor
    *	@param abbreviation abbreviation of this currency (2 or 3 chars)
    *	@param type the full name of this currency
    *	@param euroExchangeRate one euro is worth xxx in this currency
    *	@param digits of digits to be displayed after separator
    */
   public Currency(String abbreviation,
         String type,
         BigDecimal euroExchangeRate,
         int digits)
   {
      Assert.condition(abbreviation != null
            && abbreviation.length() > 1 && abbreviation.length() < 4);
      Assert.condition(type != null && type.length() > 0);
      Assert.condition(euroExchangeRate != null && euroExchangeRate.intValue() >= 0);
      Assert.condition(digits >= 0);

      this.abbreviation = abbreviation;
      this.type = type;
      this.euroExchangeRate = euroExchangeRate;
      this.digits = digits;

      identity = 0;

      for (int i = 0; i < abbreviation.length(); i++)
      {
         identity <<= 8;
         identity += abbreviation.charAt(i);
      }
   }

   /**
    *	returns the abbreviation of this currency
    */
   public String getAbbreviation()
   {
      return abbreviation;
   }

   /**
    *	returns the full name of this currency
    */
   public String getType()
   {
      return type;
   }

   /**
    *	returns the exchange rate euro to this currency
    */
   public BigDecimal getRate()
   {
      return euroExchangeRate;
   }

   /**
    * returns the number of digits after separator
    */
   public int getDigits()
   {
      return digits;
   }

   /**
    *
    */
   public int getIdentity()
   {
      return identity;
   }
}
