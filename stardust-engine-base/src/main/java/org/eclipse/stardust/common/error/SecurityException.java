package org.eclipse.stardust.common.error;

/**
 * Thrown to indicate that security related issue occurred
 * 
 * @author Thomas.Wolfram
 *
 */
public class SecurityException extends PublicException
{

   private static final long serialVersionUID = 905995439950464335L;

   public SecurityException(Throwable e)
   {
      super(e);
   }

   public SecurityException(String message)
   {
      super(message);
   }

}
