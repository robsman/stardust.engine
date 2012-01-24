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
public class FilterScopeVerifier implements FilterVerifier
{
   private final FilterVerifier predecessor;
   private final Class scope;

   public FilterScopeVerifier(Class scope)
   {
      this(null, scope);
   }

   public FilterScopeVerifier(FilterVerifier predecessor, Class scope)
   {
      this.predecessor = predecessor;
      this.scope = scope;
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
         result = FILTER_UNSUPPORTED;
      }

      if (FILTER_SUPPORTED.equals(result) && (filter instanceof ScopedFilter))
      {
         ScopedFilter valueFilter = (ScopedFilter) filter;
         if (!scope.equals(valueFilter.getScope()))
         {
            result = FilterVerifier.FILTER_IGNORED;
         }
      }
      return result;
   }
}
