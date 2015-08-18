package org.eclipse.stardust.test.dms;

/**
 * Pojo for java application type that causes an Exception.
 *
 * @author Roland.Stamm
 *
 */
public class PojoException
{

   public PojoException()
   {
   }

   public void throwException() throws Exception
   {
      throw new Exception("This should cause a roll back");
   }
}
