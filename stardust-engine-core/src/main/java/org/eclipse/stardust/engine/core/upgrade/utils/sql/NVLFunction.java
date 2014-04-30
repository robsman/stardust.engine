package org.eclipse.stardust.engine.core.upgrade.utils.sql;

/**
 * Class representing the Oracle NVL function which can
 * replace values
 *
 *
 * @author Holger.Prause
 *
 * @param <K> the type of value to replace
 * @param <V> the type of the value which will replace
 */
public class NVLFunction<K, V> extends BinaryDatabaseFunction<K, V>
{
   public NVLFunction(K valueToReplace, V replaceValue)
   {
      super("NVL", valueToReplace, replaceValue);
   }

   public K getValueToReplace()
   {
      return getFirstArgument();
   }

   public V getReplaceValue()
   {
      return getSecondArgument();
   }
}
