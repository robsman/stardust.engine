package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.junit.Assert;
import org.junit.Test;

public class PiRtClassesArePiOrAiAwareTest
{
   @Test
   public void testIt()
   {
      for (Class<? extends Persistent> p : Constants.PERSISTENT_RUNTIME_PI_CLASSES)
      {
         boolean piAware = IProcessInstanceAware.class.isAssignableFrom(p);
         boolean aiAware = IActivityInstanceAware.class.isAssignableFrom(p);
         Assert.assertTrue(piAware || aiAware);
      }
   }
}
