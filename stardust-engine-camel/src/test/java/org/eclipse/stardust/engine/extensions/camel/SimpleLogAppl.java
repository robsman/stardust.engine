package org.eclipse.stardust.engine.extensions.camel;

public class SimpleLogAppl
{

   public void log(Object a)
   {
      System.out.println(a);
   }

   public void log(Object a, Object b)
   {
      System.out.println(a + "/" + b);
   }
}
