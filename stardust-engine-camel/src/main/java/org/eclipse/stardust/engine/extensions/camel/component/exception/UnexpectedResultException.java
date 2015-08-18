package org.eclipse.stardust.engine.extensions.camel.component.exception;

public class UnexpectedResultException extends Exception
{
   private static final long serialVersionUID = 1L;

   public UnexpectedResultException()
   {
      super();
   }

   public UnexpectedResultException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public UnexpectedResultException(String message)
   {
      super(message);
   }

   public UnexpectedResultException(Throwable cause)
   {
      super(cause);
   }
}
