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
 * OrTerm can hold an arbitrary number of <code>PredicateTerm</code>s which are
 * combined by the operator OR.
 *
 * @author sborn
 * @version $Revision$
 */
public class OrTerm implements MultiPartPredicateTerm
{
   private final List<PredicateTerm> parts;

   /**
    * Constructs an empty <code>OrTerm</code>.
    */
   public OrTerm()
   {
      this(null);
   }

   /**
    * Constructs an <code>OrTerm</code> with the given <code>PredicateTerm</code>s.
    *
    * @param predicates An array of <code>PredicateTerm</code>s
    */
   public OrTerm(PredicateTerm[] predicates)
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
    * Adds an <code>PredicateTerm</code> to this <code>OrTerm</code>.
    *
    * @param part The <code>PredicateTerm</code> to be added
    *
    * @return This <code>OrTerm</code>
    */
   public MultiPartPredicateTerm add(PredicateTerm part)
   {
      this.parts.add(part);

      return this;
   }

   /**
    * Returns the <code>PredicateTerm</code>s currently hold by this <code>OrTerm</code>.
    *
    * @return A list <code>PredicateTerm</code>s
    */
   public List<PredicateTerm> getParts()
   {
      return Collections.unmodifiableList(parts);
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
         return StringUtils.join(parts.iterator(), " OR ");
      }
   }
}
