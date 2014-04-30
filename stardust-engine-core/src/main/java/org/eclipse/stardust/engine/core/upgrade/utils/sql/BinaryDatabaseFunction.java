package org.eclipse.stardust.engine.core.upgrade.utils.sql;

public abstract class BinaryDatabaseFunction<K,V> extends DatabaseFunction
{
   private K firstArgument;
   private V secondArgument;

   public BinaryDatabaseFunction(String name, K firstArgument, V secondArgument)
   {
      super(name, firstArgument, secondArgument);
      this.firstArgument = firstArgument;
      this.secondArgument = secondArgument;
   }

   public K getFirstArgument()
   {
      return firstArgument;
   }

   public V getSecondArgument()
   {
      return secondArgument;
   }
}
