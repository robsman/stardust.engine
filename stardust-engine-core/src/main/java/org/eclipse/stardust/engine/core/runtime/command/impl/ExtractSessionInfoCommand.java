package org.eclipse.stardust.engine.core.runtime.command.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;

public class ExtractSessionInfoCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      Map<String, String> credentials = SessionManager.instance().getSessionTokens();
      return new SessionInfo(credentials);
   }

   public static class SessionInfo implements Serializable
   {
      private static final long serialVersionUID = 1L;

      public final Map<String, String> tokens;

      public SessionInfo(Map<String, String> tokens)
      {
         this.tokens = (null != tokens) //
               ? tokens
               : Collections.<String, String> emptyMap();
      }
   }
}
