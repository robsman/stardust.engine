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

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


public class FieldRef implements Column
{
   public static final FieldRef[] EMPTY_ARRAY = new FieldRef[0];
   
   private final Class boundType;
   private ITableDescriptor type;
   public String fieldName;
   private Boolean literalField;
   private final boolean ignorePreparedStatements;
   
   public FieldRef(Class type, String fieldName)
   {
      this.boundType = type;
      this.type = null;
      this.fieldName = fieldName;
      this.literalField = null;
      this.ignorePreparedStatements = false;
   }
   
   public FieldRef(ITableDescriptor type, String fieldName)
   {
      this(type, fieldName, false);
   }
   
   
   public FieldRef(ITableDescriptor type, String fieldName, boolean ignorePreparedStatements)
   {
      this.boundType = null;
      this.type = type;
      this.fieldName = fieldName;
      this.literalField = null;
      this.ignorePreparedStatements = ignorePreparedStatements;
   }
   
   public Class getBoundType()
   {
      return boundType;
   }

   public ITableDescriptor getType()
   {
      if ((null == type) && (null != boundType))
      {
         this.type = TypeDescriptor.get(boundType);
      }
      return type;
   }

   public boolean equals(Object obj)
   {
      boolean areEqual = false;
      
      if (this == obj)
      {
         areEqual = true;
      }
      else if (obj instanceof FieldRef)
      {
         FieldRef that = (FieldRef) obj;
         
         final String thisAlias = this.getType().getTableAlias();
         final String thatAlias = that.getType().getTableAlias();
         
         areEqual = this.type.getTableName().equalsIgnoreCase(that.type.getTableName())
               && CompareHelper.areEqual(thisAlias, thatAlias)
               && this.fieldName.equalsIgnoreCase(that.fieldName);
      }
      
      return areEqual;
   }
   
   public String toString()
   {
      return (null != type ? type.getTableAlias() + "." : "") + fieldName;
   }
   
   public boolean isLiteralField()
   {
      if(literalField == null)
      {
         literalField = false; // default value
         ITableDescriptor tableDesc = getType();
         if(tableDesc instanceof ITypeDescriptor)
         {
            Class type = ((ITypeDescriptor)tableDesc).getType();
            TypeDescriptor typeDesc = TypeDescriptor.get(type);
            if(typeDesc != null)
            {
               literalField = typeDesc.isLiteralField(fieldName);
            }
         }
      }
      return literalField;
   }

   public boolean isIgnorePreparedStatements()
   {
      return ignorePreparedStatements;
   }
}
