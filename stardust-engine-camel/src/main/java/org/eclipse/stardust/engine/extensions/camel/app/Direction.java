package org.eclipse.stardust.engine.extensions.camel.app;
import org.eclipse.stardust.common.StringKey;

public class Direction extends StringKey
{
   public static final Direction IN = new Direction("in", "Response");
   public static final Direction OUT = new Direction("out", "Request");
   public static final Direction INOUT = new Direction("inout", "Request / Response");

   public Direction(String id, String defaultName)
   {
      super(id, defaultName);
   }

   public boolean isSending()
   {
      return this.equals(OUT) || this.equals(INOUT);
   }

   public boolean isReceiving()
   {
      return this.equals(IN) || this.equals(INOUT);
   }
}
