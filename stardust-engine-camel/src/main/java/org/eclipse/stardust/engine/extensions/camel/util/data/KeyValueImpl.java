package org.eclipse.stardust.engine.extensions.camel.util.data;

public class KeyValueImpl implements KeyValue
{

   private final String key;
   private final String value;
   private final String type;

   public KeyValueImpl(String key, String value, String type)
   {
      this.key = key;
      this.value = value;
      this.type = type;
   }

   public KeyValueImpl(String key, String value)
   {
      this.key = key;
      this.value = value;
      this.type = "string";
   }

   public String getKey()
   {
      return key;
   }

   public String getType()
   {
      return type;
   }

   public String getValue()
   {
      return value;
   }

}
