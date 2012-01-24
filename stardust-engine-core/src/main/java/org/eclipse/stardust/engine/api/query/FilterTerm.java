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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;



/**
 * Filter criterion providing filter criteria groups. Grouped can be built by either
 * {@link #AND}ing or {@link #OR}ing the contained criteria.
 *
 * <p>For convenience there exist subclasses {@link FilterAndTerm} and
 * {@link FilterOrTerm} providing smoother handling.</p>
 *
 * @author rsauer
 * @version $Revision$
 */
public abstract class FilterTerm implements FilterCriterion
{
   private static final long serialVersionUID = -8556008405429115283L;

   private static final Logger trace = LogManager.getLogger(FilterTerm.class);

   /**
    * Constant marking AND-terms.
    *
    * @see #OR
    */
   public static final Kind AND = new Kind("AND");

   /**
    * Constant marking OR-terms.
    *
    * @see #AND
    */
   public static final Kind OR = new Kind("OR");

   private final FilterVerifier verifier;

   private final Kind kind;
   private final List parts;

   /**
    * Initializes a new filter term instance.
    *
    * @param verifier The verifier to be used for criteria verification.
    * @param kind The kind of this term, either {@link #AND} or {@link #OR}.
    */
   protected FilterTerm(FilterVerifier verifier, Kind kind)
   {
      this.verifier = verifier;
      this.kind = kind;
      this.parts = new LinkedList();
   }

   /**
    * Gets the kind of this filter term.
    *
    * @return The kind, either {@link #AND} or {@link #OR}.
    */
   public Kind getKind()
   {
      return kind;
   }

   /**
    * Gets the list of filter criteria this term contains.
    *
    * @return The unmodifiable list of contained filter criteria.
    */
   public List getParts()
   {
      return Collections.unmodifiableList(parts);
   }

   /**
    * Obtains the verifyer used by this filter term.
    *
    * @return The verifyer.
    */
   FilterVerifier getVerifier()
   {
      return verifier;
   }

   /**
    * Adds the given filter to the list of criteria.
    *
    * @param filter The filter criterion to add.
    * @return The callee, thus allowing chained calls.
    * 
    * @throws UnsupportedFilterException if the filter criterion to be added is not valid
    *       for thes query this filter term belongs to
    */
   public FilterTerm add(FilterCriterion filter) throws UnsupportedFilterException
   {
      FilterVerifier.VerificationKey filterApplicability = verifier
            .verifyFilter(filter);
      if (FilterVerifier.FILTER_SUPPORTED.equals(filterApplicability))
      {
         parts.add(filter);
      }
      else
      {
         throw new UnsupportedFilterException(
               BpmRuntimeError.QUERY_FILTER_IS_XXX_FOR_QUERY.raise(filter,
                     filterApplicability), filter);
      }
      return this;
   }

   /**
    * Creates a new AND-term and adds it to callee term.
    *
    * @return The newly created AND-term, thus allowing chained calls.
    */
   public FilterAndTerm addAndTerm()
   {
      FilterAndTerm term = new FilterAndTerm(verifier);
      add(term);
      return term;
   }

   /**
    * Creates a new OR-term and adds and adds it to callee term.
    *
    * @return The newly created OR-term, thus allowing chained calls.
    */
   public FilterOrTerm addOrTerm()
   {
      FilterOrTerm term = new FilterOrTerm(verifier);
      add(term);
      return term;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   /**
    * Factory method to create instances of the same kind of filter terms as the callee.
    *
    * @param verifier The verifier to use for criteria verification. <code>null</code> if
    *        the term's original verifier is to be used.
    *
    * @return A new filter term of the same kind as the callee.
    */
   protected abstract FilterTerm createOfSameKind(FilterVerifier verifier);

   /**
    * Enumeration for filter term kind definition.
    *
    * @author rsauer
    * @version $Revision$
    */
   public static final class Kind extends StringKey
   {
      private static final long serialVersionUID = -5980076869145491642L;

      private Kind(String tag)
      {
         super(tag, tag);
      }
   }
}
