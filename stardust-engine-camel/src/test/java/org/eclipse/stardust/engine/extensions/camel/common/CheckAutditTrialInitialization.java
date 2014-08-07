package org.eclipse.stardust.engine.extensions.camel.common;

import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class CheckAutditTrialInitialization 
{
   private static boolean initiated;
   private SpringTestUtils testUtils;
   private DerbyPropertyManager derbyPropertyManager;

   public CheckAutditTrialInitialization(SpringTestUtils testUtils, DerbyPropertyManager derbyPropertyManager) throws Exception
   {
      this.testUtils=testUtils;
      this.derbyPropertyManager = derbyPropertyManager;
      if (!initiated)
         setUpGlobal();
   }
   public void setUpGlobal() throws Exception
   {
      // initiate environment
      testUtils.setUpGlobal();
      // set Derby properties
      derbyPropertyManager.setDerbyProperties();
      initiated = true;
   }
}
