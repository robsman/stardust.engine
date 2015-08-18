package org.eclipse.stardust.engine.api.ejb3.beans;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;

class ForkingServiceActionRunner implements ActionRunner
   {
      public Object execute(Action action)
      {
         return action.execute();
      }
   }