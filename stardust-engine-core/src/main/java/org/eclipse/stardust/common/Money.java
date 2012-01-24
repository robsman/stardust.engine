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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.error.ApplicationException;


/**
 *
 */
public class Money extends Number implements Cloneable
{
   private static final long serialVersionUID = -8255247501111877617L;

   public static final int EUR = 0;
   public static final int ATS = 1;
   public static final int BEF = 2;
   public static final int DM = 3;
   public static final int ESP = 4;
   public static final int FIM = 5;
   public static final int FRF = 6;
   public static final int IEP = 7;
   public static final int ITL = 8;
   public static final int LUF = 9;
   public static final int NLG = 10;
   public static final int PTE = 11;

   private static final Currency[] EU_CURRENCIES = {
         Currency.EUR,
         Currency.ATS,
         Currency.BEF,
         Currency.DEM,
         Currency.ESP,
         Currency.FIM,
         Currency.FRF,
         Currency.IEP,
         Currency.ITL,
         Currency.LUF,
         Currency.NLG,
         Currency.PTE
   };

   private static final Currency DEFAULT_CURRENCY = EU_CURRENCIES[EUR];

   private static final String PATTERN = "###,##0.";

   // @@todo should be *per* currency
   private static NumberFormat moneyFormat = new DecimalFormat(PATTERN + "00");
   // @end todo

   private static final String[] EU_CURRENCY_ABBREVIATIONS = {
      EU_CURRENCIES[EUR].getAbbreviation(),
      EU_CURRENCIES[ATS].getAbbreviation(),
      EU_CURRENCIES[BEF].getAbbreviation(),
      EU_CURRENCIES[DM].getAbbreviation(),
      EU_CURRENCIES[ESP].getAbbreviation(),
      EU_CURRENCIES[FIM].getAbbreviation(),
      EU_CURRENCIES[FRF].getAbbreviation(),
      EU_CURRENCIES[IEP].getAbbreviation(),
      EU_CURRENCIES[ITL].getAbbreviation(),
      EU_CURRENCIES[LUF].getAbbreviation(),
      EU_CURRENCIES[NLG].getAbbreviation(),
      EU_CURRENCIES[PTE].getAbbreviation()
   };

   private static int defaultCurrency;

   /**
    * Hashtables to identify currencies by abbrevaition or itentity number
    */
   private static Hashtable<String, Currency> currencies;
   private static Hashtable<String, Currency> identities;

   private BigDecimal value;
   private int currency;

   static
   {
      Assert.condition(EU_CURRENCIES.length == EU_CURRENCY_ABBREVIATIONS.length);

      currencies = new Hashtable<String, Currency>(EU_CURRENCIES.length);
      identities = new Hashtable<String, Currency>(EU_CURRENCIES.length);

      // Add european currencies

      for (int i = 0; i < EU_CURRENCIES.length; i++)
      {
         currencies.put(EU_CURRENCIES[i].getAbbreviation(), EU_CURRENCIES[i]);
         identities.put(Integer.toString(EU_CURRENCIES[i].getIdentity()),
               EU_CURRENCIES[i]);
      }

      // Default currency

      defaultCurrency = DEFAULT_CURRENCY.getIdentity();
   }

   /** @return all European currencies (abbreviated, e.g. DM) */
   public static String[] getEuropeanCurrencyList()
   {
      return EU_CURRENCY_ABBREVIATIONS;
   }

   /** Returns the corresponding currency for an idenity number */
   public static int upgradeCurrency(int identity)
   {
      // @todo remove after testing or be compatible with older money version ?

      switch (identity)
      {
         case 0:
            identity = EU_CURRENCIES[DM].getIdentity();
            break;
         case 1:
            identity = EU_CURRENCIES[EUR].getIdentity();
            break;
      }

      return identity;
   }

   /** return the corresponding currency for an idenity number */
   public static String getCurrencyFor(int identity)
   {
      identity = upgradeCurrency(identity);

      Currency currency = identities.get("" + identity);

      Assert.isNotNull(currency);

      return currency.getAbbreviation();
   }

   /** return the corresponding currency for an idenity number */
   public static int getCurrencyValueFor(String abbreviation)
   {
      Currency currency = currencies.get(abbreviation);

      Assert.isNotNull(currency);

      return currency.getIdentity();
   }

   /** return the corresponding currency for an idenity number */
   public static void setDefaultCurrency(int currency)
   {
      defaultCurrency = currency;
   }

   /** return the corresponding currency for an idenity number */
   public static int getDefaultCurrency()
   {
      return defaultCurrency;
   }

   /** return the full name (abbreviation & additional information) */
   public static String getLongName(String abbreviation)
   {
      return ((currencies.get(abbreviation)).getType());
   }

   /**
    * @return full name (abbreviation and additional information).
    */
   public static String getFullName(String abbreviation)
   {
      StringBuffer buffer = new StringBuffer(abbreviation);
      buffer.append(" - ");
      buffer.append((currencies.get(abbreviation)).getType());

      return buffer.toString();
   }

   /**
    *	Creates a money object of value 0.0 and the currency DM.
    */
   public Money()
   {
      value = new BigDecimal(0.0);
      currency = defaultCurrency;
   }

   /**
    * Creates a money object of value value and the currency DM.
    */
   public Money(double value)
   {
      this.value = new BigDecimal(value);
      currency = defaultCurrency;
   }

   /**
    *
    */
   public Money(double value, int currency)
   {
      this.value = new BigDecimal(value);
      currency = upgradeCurrency(currency);
      this.currency = currency;
   }

   public Object clone()
   {
      return new Money(value, currency);
   }

   /**
    *	Copy constructor
    */
   public Money(Money m)
   {
      this.value = m.value;
      currency = upgradeCurrency(currency);
      this.currency = m.currency;
   }

   /**
    *
    */
   public Money(String stringValue)
   {
      Assert.isNotNull(stringValue);
      StringTokenizer tkr = new StringTokenizer(stringValue, " ");
      if (!tkr.hasMoreTokens())
      {
         value = new BigDecimal(0.0);
         currency = defaultCurrency;
      }
      else
      {
         String _numberString = tkr.nextToken();
         String _currencyString = DEFAULT_CURRENCY.getAbbreviation();
         if (tkr.hasMoreTokens())
         {
            _currencyString = tkr.nextToken();
         }

         try
         {
            value = new BigDecimal(moneyFormat.parse(_numberString).doubleValue());
            currency = getCurrencyValueFor(_currencyString);
         }
         catch (ParseException e)
         {
            throw new ApplicationException(e);
         }
      }
   }

   /**
    *
    */
   public Money(String stringValue, int currency)
   {
      value = new BigDecimal(stringValue);
      currency = upgradeCurrency(currency);
      this.currency = currency;
   }

   /**
    *
    */
   public Money(BigDecimal value)
   {
      this.value = value;
      currency = defaultCurrency;
   }

   /**
    *
    */
   public Money(BigDecimal value, int currency)
   {
      this.value = value;
      currency = upgradeCurrency(currency);
      this.currency = currency;
   }

   /**
    *
    */
   public String toString()
   {
      return ("" + moneyFormat.format(value) + " " + getCurrencyFor(currency));
   }

   /**
    *	implements Number
    */
   public byte byteValue()
   {
      return value.byteValue();
   }

   /**
    *	implements Number
    */
   public double doubleValue()
   {
      return value.doubleValue();
   }

   /**
    * implements Number
    */
   public float floatValue()
   {
      return value.floatValue();
   }

   /**
    * implements Number
    */
   public int intValue()
   {
      return value.intValue();
   }

   /**
    * implements Number
    */
   public long longValue()
   {
      return value.longValue();
   }

   /**
    * implements Number
    */
   public short shortValue()
   {
      return value.shortValue();
   }

   /**
    *
    */
   public BigDecimal getValue()
   {
      return value;
   }

   /**
    *
    */
   public int getCurrency()
   {
      return currency;
   }

   /**
    *
    */
   public String getCurrencyString()
   {
      return getCurrencyFor(currency);
   }

   /** get converted value only */
   public BigDecimal getCurrencyValue(int wantedCurrency)
   {
      if (wantedCurrency == currency)
      {
         return getValue();
      }

      // @todo remove after testing or be compatible with older money version ?
      currency = upgradeCurrency(currency);
      wantedCurrency = upgradeCurrency(wantedCurrency);

      // real part

      Currency newCurrency = (Currency) identities.get("" + wantedCurrency);
      Currency oldCurrency = (Currency) identities.get("" + getCurrency());

      Assert.isNotNull(newCurrency);
      Assert.isNotNull(oldCurrency);

      BigDecimal result =
            new BigDecimal(value.doubleValue() / oldCurrency.getRate().doubleValue());

      if ("EUR".equals(newCurrency.getAbbreviation()))
      {
         return result;
      }

      result = result.multiply(newCurrency.getRate());

      return result;
   }

   /** Convert any currency into any other <p>
    Note: Does not alter current Money value but returns converted one */
   public Money getConverted(String wantedCurrency)
   {
      Assert.isNotNull(wantedCurrency);

      Currency newCurrency = currencies.get(wantedCurrency);
      Currency oldCurrency = identities.get("" + getCurrency());

      Assert.isNotNull(newCurrency, "Unknown target currency: " + wantedCurrency);
      Assert.isNotNull(oldCurrency, "Unknown source currency: " + oldCurrency);

      if (oldCurrency.getAbbreviation().equals(newCurrency))
      {
         return this;
      }

      // first to euro

      Money result = multiply(1.0 / oldCurrency.getRate().doubleValue());
      result.currency = newCurrency.getIdentity();

      if (Currency.EUR.getAbbreviation().equals(wantedCurrency))
      {
         return result;
      }

      // second to new currency

      result = result.multiply(newCurrency.getRate().doubleValue());

      return result;
   }

   /** Changes the sign of this object's value. */
   public void changeSign()
   {
      value = value.negate();
   }

   /** Returns a money object with the negative value of this object's value
    and the same currency. */
   public Money negate()
   {
      return new Money(value.negate(), currency);
   }

   /** Adds the value of money to this object. */
   public void increaseBy(Money money)
   {
      Assert.isNotNull(money);
      Assert.condition(money.currency == currency);

      this.value = this.value.add(money.value);
   }

   /** Adds the value value to this object. */
   public void increaseBy(double value)
   {
      this.value = this.value.add(new BigDecimal(value));
   }

   /** Adds the value value to this object. */
   public void increaseBy(String value)
   {
      this.value = this.value.add(new BigDecimal(value));
   }

   /** Adds the value of money to the value of this object and creates a new
    money object with the same currency and the sum as value. */
   public Money add(Money money)
   {
      Assert.isNotNull(money);
      Assert.condition(money.currency == currency);

      return new Money(value.add(money.value), currency);
   }

   /** Substracts the value of money from this object. */
   public void decreaseBy(Money money)
   {
      Assert.isNotNull(money);
      Assert.condition(money.currency == currency);

      this.value = this.value.subtract(money.value);
   }

   /** Substracts the value value from this object. */
   public void decreaseBy(double value)
   {
      this.value = this.value.subtract(new BigDecimal(value));
   }

   /** Substracts the value value from this object. */
   public void decreaseBy(String value)
   {
      this.value = this.value.subtract(new BigDecimal(value));
   }

   /** Substract the value of money from the value of this object and creates a new
    money object with the same currency and the sum as value. */
   public Money subtract(Money money)
   {
      Assert.isNotNull(money);
      Assert.condition(money.currency == currency);

      return new Money(value.subtract(money.value), currency);
   }

   /** */
   public void scale(double factor)
   {
      value = value.multiply(new BigDecimal(factor));
   }

   /** */
   public void scale(int factor)
   {
      value = value.multiply(new BigDecimal(factor * 1.0));
   }

   /** */
   public Money multiply(double factor)
   {
      return new Money(value.multiply(new BigDecimal(factor)), currency);
   }

   /** */
   public Money multiply(int factor)
   {
      return new Money(value.multiply(new BigDecimal(factor * 1.0)), currency);
   }

   public boolean equals(Object rhs)
   {
      boolean isEqual = false;
      if (this == rhs)
      {
         isEqual = true;
      }
      else if (rhs instanceof Money)
      {
         final Money money = (Money) rhs;
         isEqual = (currency == money.currency) && value.equals(money.value);
      }
      return isEqual;
   }

   /** */
   public int compareTo(Money money)
   {
      Assert.isNotNull(money);
      Assert.condition(money.currency == currency);

      return value.compareTo(money.getValue());
   }
}

