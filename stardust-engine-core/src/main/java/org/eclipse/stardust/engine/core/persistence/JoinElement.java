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

import org.eclipse.stardust.common.Pair;

public class JoinElement
{
   public enum JoinConditionType
   {
      AND,
      OR;
   }

   private JoinConditionType joinConditionType;

   private Pair<FieldRef, ?> joinCondition;

   public JoinElement(Pair<FieldRef, ?> joinCondition, JoinConditionType joinConditionType)
   {
      super();
      this.joinConditionType = joinConditionType;
      this.joinCondition = joinCondition;
   }

   public JoinConditionType getJoinConditionType()
   {
      return joinConditionType;
   }

   public Pair<FieldRef, ?> getJoinCondition()
   {
      return joinCondition;
   }

}


