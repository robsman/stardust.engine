package org.eclipse.stardust.engine.extensions.camel.component.exception;

public class MissingEndpointException extends Exception
{

    private static final long serialVersionUID = -3318345202369806362L;

   public MissingEndpointException()
   {
      super();
   }

   public MissingEndpointException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public MissingEndpointException(String message)
   {
      super(message);
   }

   public MissingEndpointException(Throwable cause)
   {
      super(cause);
   }

}
