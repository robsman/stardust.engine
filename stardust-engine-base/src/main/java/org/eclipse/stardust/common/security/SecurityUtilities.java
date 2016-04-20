package org.eclipse.stardust.common.security;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;

/**
 * 
 * @author thomas.wolfram
 *
 */
public class SecurityUtilities
{
   
   public static SecurityProvider getSecurityProvider()
   {
      SecurityProvider.Factory provider = ExtensionProviderUtils.getFirstExtensionProvider(SecurityProvider.Factory.class);
      if (provider == null)
      {         
         return new DefaultSecurityProvider();
      }
      return provider.getInstance();
   }

}
