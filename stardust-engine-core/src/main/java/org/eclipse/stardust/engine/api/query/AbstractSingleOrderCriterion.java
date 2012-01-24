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
 * @author born
 * @version $Revision$
 */
public abstract class AbstractSingleOrderCriterion implements OrderCriterion
{
   private static final long serialVersionUID = 1L;
   
   private final boolean ascending;
   
   protected AbstractSingleOrderCriterion(boolean ascending)
   {
      super();
      this.ascending = ascending;
   }

   /**
    * Flag indicating if ordering should be performed by either ascending or descending
    * attribute value.
    *
    * @return <code>true</code> if ordering should be performed by ascending attribute
    *         values, <code>false</code> if by descending.
    */
   public boolean isAscending()
   {
      return ascending;
   }
}
