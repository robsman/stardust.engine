package org.eclipse.stardust.test.spi;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.junit.Assert.assertNotNull;

import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.monitoring.PartitionMonitorSpiTestLog;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * /**
 * <p>
 * This class tests the monitoring functionality of the PartitionMonitor
 * </p>
 * 
 * @author Thomas.Wolfram
 *
 */
public class PartitionMonitorTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory serviceFactory = new TestServiceFactory(
         ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BasicWorkflowModel");

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(
         serviceFactory);

   @Test
   public void testMonitorDeployments()
   {
      PartitionMonitorSpiTestLog partitionLog = PartitionMonitorSpiTestLog.getInstance();

      // Test if a log entry has been written before model deployment
      assertNotNull(partitionLog.findLogEntryForMethod("beforeModelDeployment"));

      // Test if a log entry has been written after model deployment
      assertNotNull(partitionLog.findLogEntryForMethod("afterModelDeployment"));

   }

   @Test
   public void testMonitorUserOperations()
   {

      PartitionMonitorSpiTestLog partitionLog = PartitionMonitorSpiTestLog.getInstance();

      serviceFactory.getUserService().createUser("user", "Test", "User", "Description",
            "userpass", "usermail", null, null);

      // Test if log entry has been written after creation of user
      assertNotNull(partitionLog.findLogEntryForMethod("userCreated"));
      
      serviceFactory.getUserService().invalidateUser("user");
      
      // Test if log entry has been written after creation of user
      assertNotNull(partitionLog.findLogEntryForMethod("userDisabled"));
      

      // Test if log entry has been written after creation of user
      // TODO: Implement after enableing functionality has been defined
      // assertNotNull(partitionLog.findLogEntryForMethod("userEnabled"));      


   }

   @Test
   public void testMonitorUserRealmOperations()
   {
      PartitionMonitorSpiTestLog partitionLog = PartitionMonitorSpiTestLog.getInstance();
      
      serviceFactory.getUserService().createUserRealm("TestRealm", "Test Realm", "Realm Description");
      
      // Test if log entry has been written after creation of user realm
      assertNotNull(partitionLog.findLogEntryForMethod("userRealmCreated"));      
            
      serviceFactory.getUserService().dropUserRealm("TestRealm");
      
      // Test if log entry has been written after dropping of user realm
      assertNotNull(partitionLog.findLogEntryForMethod("userRealmDropped"));         
      
   }

}
