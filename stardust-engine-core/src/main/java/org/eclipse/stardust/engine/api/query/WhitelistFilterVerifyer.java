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
package org.eclipse.stardust.engine.api.query;

/**
 * @author rsauer
 * @version $Revision$
 */
public class WhitelistFilterVerifyer implements FilterVerifier
{
   private final FilterVerifier predecessor;
   private final Class[] whitelist;

   public WhitelistFilterVerifyer(Class[] whiteList)
   {
      this(null, whiteList);
   }

   public WhitelistFilterVerifyer(FilterVerifier predecessor, Class[] whiteList)
   {
      this.predecessor = predecessor;
      this.whitelist = whiteList;
   }

   public VerificationKey verifyFilter(FilterCriterion filter)
   {
      FilterVerifier.VerificationKey result;
      if (null != predecessor)
      {
         result = predecessor.verifyFilter(filter);
      }
      else
      {
         result = FILTER_UNSUPPORTED;
      }

      if (FILTER_UNSUPPORTED.equals(result) && isInWhitelist(filter.getClass()))
      {
         result = FilterVerifier.FILTER_SUPPORTED;
      }
      return result;
   }

   private final boolean isInWhitelist(Class candidate)
   {
      boolean isElement = false;
      for (int i = 0; i < whitelist.length; i++)
      {
         if (whitelist[i].isAssignableFrom(candidate))
         {
            isElement = true;
            break;
         }
      }
      return isElement;
   }
}
