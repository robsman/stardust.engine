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
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.JoinElement.JoinConditionType;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * Join describes how a join between two entities can be build.
 *
 * @author sborn
 * @version $Revision$
 */
public class Join extends TableDescriptor
{
   private final List<JoinElement> joinConditions;

   private final ITableDescriptor rhsTable;
   private final String rhsAlias;

   private AndTerm restriction;

   private Join dependency;
   private boolean required;

   /**
    * Constructs a join description.
    *
    * @param rhsType Type of the new entity
    */
   public Join(Class rhsType)
   {
      this(rhsType, TypeDescriptor.get(rhsType).getTableAlias());
   }

   /**
    * Constructs a join description.
    *
    * @param rhsType Type of the new entity
    * @param rhsAlias The alias to be used when referencing field of the joined type
    */
   public Join(Class rhsType, String rhsAlias)
   {
      this(null, TypeDescriptor.get(rhsType), rhsAlias);
   }

   /**
    * Constructs a join description.
    *
    * @param rhsType Type of the new entity
    * @param rhsAlias The alias to be used when referencing field of the joined type
    */
   public Join(String schemaName, Class rhsType, String rhsAlias)
   {
      this(schemaName, TypeDescriptor.get(rhsType), rhsAlias);
   }

   /**
    * Constructs a join description.
    *
    * @param rhsType Type of the new entity
    * @param rhsAlias The alias to be used when referencing field of the joined type
    */
   public Join(ITableDescriptor rhsType, String rhsAlias)
   {
      this(null, rhsType, rhsAlias);
   }

   /**
    * Constructs a join description.
    *
    * @param rhsType Type of the new entity
    * @param rhsAlias The alias to be used when referencing field of the joined type
    */
   public Join(String schemaName, ITableDescriptor rhsType, String rhsAlias)
   {
      super(schemaName);

      this.joinConditions = new ArrayList();

      this.rhsTable = rhsType;
      this.rhsAlias = rhsAlias;

      this.required = true;
   }

   public Join on(FieldRef lhsField, String rhsFieldName)
   {
      addJoinCondition(lhsField, rhsFieldName, JoinConditionType.AND);

      return this;
   }

   public Join andOn(FieldRef lhsField, String rhsFieldName)
   {
      addJoinCondition(lhsField, rhsFieldName, JoinConditionType.AND);

      return this;
   }

   public Join orOn(FieldRef lhsField, String rhsFieldName)
   {
      addJoinCondition(lhsField, rhsFieldName, JoinConditionType.OR);

      return this;
   }

   public Join onConstant(FieldRef lhsField, String constant)
   {
      addJoinConditionConstant(lhsField, constant, JoinConditionType.AND);

      return this;
   }

   public Join andOnConstant(FieldRef lhsField, String constant)
   {
      addJoinConditionConstant(lhsField, constant, JoinConditionType.AND);

      return this;
   }

   public Join orOnConstant(FieldRef lhsField, String constant)
   {
      addJoinConditionConstant(lhsField, constant, JoinConditionType.OR);

      return this;
   }

   public Join where(PredicateTerm predicate)
   {
      if (null == restriction)
      {
         this.restriction = new AndTerm();
      }
      restriction.add(predicate);

      return this;
   }

   public Join andWhere(PredicateTerm predicate)
   {
      return where(predicate);
   }

   public void addJoinCondition(FieldRef lhsField, String rhsFieldName, JoinConditionType joinTermType)
   {
      this.joinConditions.add(new JoinElement(new Pair(lhsField, this.fieldRef(rhsFieldName)),joinTermType));
   }

   public void addJoinConditionConstant(FieldRef lhsField, String constant, JoinConditionType joinTermType)
   {
      this.joinConditions.add(new JoinElement(new Pair(lhsField, constant),joinTermType));
   }

   public String getTableName()
   {
      return getRhsTableDescriptor().getTableName();
   }

   public String getTableAlias()
   {
      return StringUtils.isEmpty(rhsAlias) ? rhsTable.getTableAlias() : rhsAlias;
   }

   public List<JoinElement> getJoinConditions()
   {
      return Collections.unmodifiableList(joinConditions);
   }

   public AndTerm getRestriction()
   {
      return restriction;
   }

   public ITableDescriptor getRhsTableDescriptor()
   {
      return rhsTable;
   }

   public Join getDependency()
   {
      return dependency;
   }

   public void setDependency(Join dependency)
   {
      this.dependency = dependency;
   }

   public boolean isRequired()
   {
      return required;
   }

   public void setRequired(boolean required)
   {
      this.required = required;
   }

   public boolean equals(Object obj)
   {
      boolean areEqual = false;

      if (this == obj)
      {
         areEqual = true;
      }
      else if (obj instanceof Join)
      {
         Join that = (Join) obj;

         final String thisAlias = StringUtils.isEmpty(this.rhsAlias) ? this.rhsAlias : this.rhsTable.getTableAlias();
         final String thatAlias = StringUtils.isEmpty(that.rhsAlias) ? that.rhsAlias : that.rhsTable.getTableAlias();

         areEqual = this.rhsTable.getTableName().equalsIgnoreCase(that.rhsTable.getTableName())
               && CompareHelper.areEqual(thisAlias, thatAlias)
               && this.joinConditions.size() == that.joinConditions.size();

         if (areEqual)
         {
            for (int i = 0; i < this.joinConditions.size(); ++i )
            {
               JoinElement thisJoinElement = this.joinConditions.get(i);
               Pair<FieldRef, ?> thisCondition = thisJoinElement.getJoinCondition();
               JoinElement thatJoinElement = that.joinConditions.get(i);
               Pair<FieldRef, ?> thatCondition = thatJoinElement.getJoinCondition();

               areEqual &= thisCondition.getFirst().equals(thatCondition.getFirst())
                     && thisCondition.getSecond().equals(thatCondition.getSecond())
                     && thisJoinElement.getJoinConditionType().equals(
                           thatJoinElement.getJoinConditionType());
            }
         }
      }

      return areEqual;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("JOIN [")
            .append(getTableName()).append(" ").append(getTableAlias());

      if (null != joinConditions && !joinConditions.isEmpty())
      {
         buffer.append(" ON (");
         String delimiter = "";
         for (Iterator<JoinElement> iter = joinConditions.iterator(); iter.hasNext();)
         {
            Pair<FieldRef, ?> condition = iter.next().getJoinCondition();
            buffer.append(delimiter);
            buffer.append(condition.getFirst()).append(" = ").append(condition.getSecond());
            delimiter = ", ";
         }
         buffer.append(")");
      }

      buffer.append("]");

      return buffer.toString();
   }
}
