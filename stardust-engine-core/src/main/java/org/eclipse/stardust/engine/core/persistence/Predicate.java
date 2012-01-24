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

/**
 * @author rsauer
 * @version $Revision$
 */
public class Predicate
{
   private final String lhsAlias;
   private final String lhsAttr;
   private final Operator operator;
   private final Object valueExpr;
   
   public Predicate(String lhsAttr, Operator operator, Object valueExpr)
   {
      this(null, lhsAttr, operator, valueExpr);
   }

   public Predicate(String lhsAlias, String lhsAttr, Operator operator, Object valueExpr)
   {
      this.lhsAlias = lhsAlias;
      this.lhsAttr = lhsAttr;
      this.operator = operator;
      this.valueExpr = valueExpr;
   }

   public String getLhsAlias()
   {
      return lhsAlias;
   }

   public String getLhsAttr()
   {
      return lhsAttr;
   }

   public Operator getOperator()
   {
      return operator;
   }

   public Object getValueExpr()
   {
      return valueExpr;
   }
}
