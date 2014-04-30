package org.eclipse.stardust.engine.core.upgrade.utils.sql;

import org.eclipse.stardust.engine.core.upgrade.framework.AbstractTableInfo.FieldInfo;
import org.eclipse.stardust.engine.core.upgrade.framework.DatabaseHelper;

public abstract class DatabaseFunction extends FieldInfo
{
   private String functionName;

   private Object[] arguments;

   public DatabaseFunction(String functionName, Object... args)
   {
      super(null, null);
      this.functionName = functionName;
      arguments = args;
   }

   @Override
   public String getName()
   {
      return toSql();
   }

   public String getFunctionName()
   {
      return functionName;
   }

   public Object[] getArguments()
   {
      return arguments;
   }

   public String toSql()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(functionName);
      buffer.append("(");

      int count = 0;
      for (Object o : arguments)
      {
         if (count > 0)
         {
            buffer.append(", ");
         }
         buffer.append(DatabaseHelper.getSqlValue(o));
         count++;
      }

      buffer.append(")");
      return buffer.toString();
   }

   @Override
   public String toString()
   {
      return toSql();
   }

}
