package org.eclipse.stardust.engine.extensions.jaxws.app.spi;

import org.eclipse.stardust.common.annotations.SPI;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.extensions.jaxws.app.WebserviceApplicationInstance.JaxwsInvocationContext;

@SPI(status = Status.Experimental, useRestriction = UseRestriction.Public)
public abstract class JaxwsClientConfigurer
{
   @SPI(status = Status.Experimental, useRestriction = UseRestriction.Public)
   public static interface Factory
   {
      JaxwsClientConfigurer createConfigurer(JaxwsInvocationContext ctx);
   }

   public abstract void initializeClientEnvironment(JaxwsInvocationContext ctx);

   public abstract void cleanupClientEnvironment(JaxwsInvocationContext ctx);
}
