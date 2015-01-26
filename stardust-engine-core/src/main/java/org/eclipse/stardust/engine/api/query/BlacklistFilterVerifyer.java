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
public class BlacklistFilterVerifyer implements FilterVerifier
{
   private final FilterVerifier predecessor;
   private final Class[] blacklist;

   public BlacklistFilterVerifyer(Class[] blacklist)
   {
      this(null, blacklist);
   }

   public BlacklistFilterVerifyer(FilterVerifier precondition, Class[] blacklist)
   {
      this.predecessor = precondition;
      this.blacklist = blacklist;
   }

   public FilterVerifier.VerificationKey verifyFilter(FilterCriterion filter)
   {
      FilterVerifier.VerificationKey result;
      if (null != predecessor)
      {
         result = predecessor.verifyFilter(filter);
      }
      else
      {
         result = FILTER_SUPPORTED;
      }

      if (FILTER_SUPPORTED.equals(result) && isInBlacklist(filter.getClass()))
      {
         result = FilterVerifier.FILTER_IGNORED;
      }
      return result;
   }

   private final boolean isInBlacklist(Class candidate)
   {
      boolean isElement = false;
      for (int i = 0; i < blacklist.length; i++)
      {
         if (candidate.equals(blacklist[i]))
         {
            isElement = true;
            break;
         }
      }
      return isElement;
   }
}
