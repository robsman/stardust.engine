package org.eclipse.stardust.engine.core.upgrade.utils.sql;

/**
 * Class used to mark constants so they will not be quoted when building the sql
 *
 * @author Holger.Prause
 *
 */
public class DatabaseConstant
{
   private Object o;

   public DatabaseConstant(Object o)
   {
      this.o = o;
   }

   @Override
   public String toString()
   {
      if(o != null)
      {
         return o.toString();
      }

      return null;
   }
}
