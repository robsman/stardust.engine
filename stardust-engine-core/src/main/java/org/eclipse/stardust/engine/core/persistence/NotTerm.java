/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.core.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;

/**
 *
 *
 * @author Holger.Prause
 * @version $Revision: $
 */
public abstract class NotTerm implements MultiPartPredicateTerm
{
   private final List<PredicateTerm> parts;

   public NotTerm()
   {
      this(null);
   }

   public NotTerm(PredicateTerm[] predicates)
   {
      if (null == predicates)
      {
         this.parts = new ArrayList();
      }
      else
      {
         this.parts = new ArrayList(predicates.length);
         for (int i = 0; i < predicates.length; i++)
         {
            parts.add(predicates[i]);
         }
      }
   }

   /**
    * Adds an <code>PredicateTerm</code> to this <code>AddTerm</code>.
    *
    * @param part The <code>PredicateTerm</code> to be added
    *
    * @return This <code>AddTerm</code>
    */
   public MultiPartPredicateTerm add(PredicateTerm part)
   {
      this.parts.add(part);
      return this;
   }

   /**
    * Returns the <code>PredicateTerm</code>s currently hold by this <code></code>.
    *
    * @return A list <code>PredicateTerm</code>s
    */
   public List<PredicateTerm> getParts()
   {
      return Collections.unmodifiableList(parts);
   }

   /**
    * Returns the condition to apply when the first predicate is processed
    *
    * @return the condition to apply when the first predicate is processed
    */
   public String getSinglePredicateCondition()
   {
      return " NOT ";
   }

   /**
    * Return the condition to apply when the first predicate was processed
    *
    * @return the condition to apply when the first predicate was processed
    */
   public abstract String getMultiPredicateCondition();

   @Override
   public String toString()
   {
      List<PredicateTerm> parts = getParts();

      if (null == parts || parts.size() == 0)
      {
         return "";
      }
      else if (parts.size() == 1)
      {
         return getSinglePredicateCondition() + parts.get(0).toString();
      }
      else
      {
         return StringUtils.join(parts.iterator(), getMultiPredicateCondition());
      }
   }
}
