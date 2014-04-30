package org.eclipse.stardust.engine.core.upgrade.utils.sql;

import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;

public class UpdateColumnInfo
{
   private FieldInfo column;
   private Object value;

   public UpdateColumnInfo(FieldInfo column, Object value)
   {
      this.column = column;
      this.value = value;
   }

   public FieldInfo getColumn()
   {
      return column;
   }

   public Object getValue()
   {
      return value;
   }
}
