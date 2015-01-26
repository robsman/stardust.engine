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
 * Convenience class providing smoother handling of OR filter terms.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see #or
 */
public final class FilterOrTerm extends FilterTerm
{
   FilterOrTerm(FilterVerifier filterGuard)
   {
      super(filterGuard, OR);
   }

   /**
    * Convenience method for adding another filter criterion to this OR term, especially
    * useful for chained calls like <code>term.or(A).or(B)</code>.
    *
    * @param filter The filter criterion to be added.
    * @return <code>this</code> to allow for chained calls.
    * 
    * @throws UnsupportedFilterException if the filter criterion to be added is not valid
    *       for thes query this filter term belongs to
    */
   public FilterOrTerm or(FilterCriterion filter) throws UnsupportedFilterException
   {
      add(filter);
      return this;
   }

   protected FilterTerm createOfSameKind(FilterVerifier verifier)
   {
      if (null == verifier)
      {
         verifier = getVerifier();
      }
      return new FilterOrTerm(verifier);
   }
}
