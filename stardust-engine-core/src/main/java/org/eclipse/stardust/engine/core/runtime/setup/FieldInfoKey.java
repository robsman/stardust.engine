/*
 * $Id$
 * (C) 2000 - 2013 CARNOT AG
 */
package org.eclipse.stardust.engine.core.runtime.setup;

import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;

public class FieldInfoKey
{
   private String name;
   public FieldInfoKey(FieldInfo fieldInfo)
   {
      if(fieldInfo != null)
      {
         name = fieldInfo.getName();
      }
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      FieldInfoKey other = (FieldInfoKey) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }
}
