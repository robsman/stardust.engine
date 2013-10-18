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
package org.eclipse.stardust.engine.core.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;


/**
 * AddTerm can hold an arbitrary number of <code>PredicateTerm</code>s which are
 * combined by the operator AND.
 *
 * @author sborn
 * @version $Revision$
 */
public class AndTerm implements MultiPartPredicateTerm
{
   private final List<PredicateTerm> parts;

   private String tag;


   public static AndTerm shallowCopy(AndTerm rhs)
   {
      return new AndTerm(rhs);
   }

   /**
    * Constructs an empty <code>AddTerm</code>.
    */
   public AndTerm()
   {
      this((PredicateTerm[]) null);
   }

   /**
    * Constructs an <code>AddTerm</code> with the given <code>PredicateTerm</code>s.
    *
    * @param predicates An array of <code>PredicateTerm</code>s
    */
   public AndTerm(PredicateTerm[] predicates)
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

   protected AndTerm(AndTerm rhs)
   {
      this.parts = Collections.unmodifiableList(rhs.parts);
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
    * Returns the <code>PredicateTerm</code>s currently hold by this <code>AddTerm</code>.
    *
    * @return A list <code>PredicateTerm</code>s
    */
   public List<PredicateTerm> getParts()
   {
      return Collections.unmodifiableList(parts);
   }

   @Override
   public String getTag()
   {
      return tag;
   }

   @Override
   public void setTag(String tag)
   {
      this.tag = tag;
   }

   public String toString()
   {
      if (null == parts || parts.size() == 0)
      {
         return "";
      }
      else if (parts.size() == 1)
      {
         return parts.get(0).toString();
      }
      else
      {
         return StringUtils.join(parts.iterator(), " AND ");
      }
   }
}
