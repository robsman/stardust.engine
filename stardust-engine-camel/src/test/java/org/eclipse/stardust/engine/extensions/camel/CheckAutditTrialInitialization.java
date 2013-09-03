package org.eclipse.stardust.engine.extensions.camel;

import org.eclipse.stardust.engine.extensions.camel.util.test.SpringTestUtils;

public class CheckAutditTrialInitialization 
{
   private boolean initiated;
   private SpringTestUtils testUtils;

   public CheckAutditTrialInitialization(SpringTestUtils testUtils) throws Exception
   {
      this.testUtils=testUtils;
      if (!initiated)
         setUpGlobal();
   }
   public void setUpGlobal() throws Exception
   {
      // initiate environment
      testUtils.setUpGlobal();
      initiated = true;
   }
}
