package org.eclipse.stardust.test.security;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.DEFAULT_PARTITION_ID;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_INTERNAL;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_PRINCIPAL;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHENTICATION_MODE_PROPERTY;
import static org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties.AUTHORIZATION_SYNC_CLASS_PROPERTY;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.runtime.beans.PartitionAwareExtensionsManager.FlushPartitionPredicate;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.security.SpringPrincipalAuthenticationTest.DummySyncProvider;

public class PublicApiTest
{

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, "BasicWorkflowModel" );

   @Rule
   public final TestRule chain = RuleChain.outerRule(sf).around(testMethodSetup);

   @Before
   public void setup()
   {
      sf.getUserService().createUser("test", "Test", "User", null, "test", "", null, null);
   }
   
   @Test
   public void testPublicApiAccessAllowed()
   {    
      UserService us = ServiceFactoryLocator.get(CredentialProvider.PUBLIC_LOGIN)
            .getUserService();
      
      us.generatePasswordResetToken("carnot", "test");
                       
   }
   

   
   @Test(expected = AccessForbiddenException.class)
   public void testPublicApiAccessDenied()
   {
      AdministrationService as = ServiceFactoryLocator.get(
            CredentialProvider.PUBLIC_LOGIN).getAdministrationService();
      
      as.getAuditTrailHealthReport();
   
   }

   @Test
   public void testPublicApiEvaluatorAllowed()
   {
      AdministrationService as = ServiceFactoryLocator.get(
            CredentialProvider.PUBLIC_LOGIN).getAdministrationService();
      
      as.getPreferences(PreferenceScope.PARTITION, "public", "configuration");
   }
   
   @Test(expected = AccessForbiddenException.class)
   public void testPublicApiEvaluatorDenied()
   {
      AdministrationService as = ServiceFactoryLocator.get(
            CredentialProvider.PUBLIC_LOGIN).getAdministrationService();
      
      as.getPreferences(PreferenceScope.PARTITION, "ipp-portal-preferences", "configuration");
   }
   
   @Test
   public void testPublicApiAccessWithPrincipalMode()
   {
      setAuthModeToPrincipal();
      
      UserService us = ServiceFactoryLocator.get(CredentialProvider.PUBLIC_LOGIN).getUserService();
            
      us.generatePasswordResetToken("carnot", "test");
      
      setAuthModeToInternal();
   }
   
   @Test
   public void testUserApiAccess()
   {
      AdministrationService as = ServiceFactoryLocator.get(
            "motu","motu").getAdministrationService();
      
      as.getAuditTrailHealthReport();
      
   }
   
   private void setAuthModeToPrincipal()
   {
      /* we need to flush the partition since the extension provider for DynamicParticipantSynchronizationProvider is cached */
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);
   
      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_PRINCIPAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, DummySyncProvider.class.getName());
   }

   private void setAuthModeToInternal()
   {
      final FlushPartitionPredicate flushPartition = new FlushPartitionPredicate(DEFAULT_PARTITION_ID);
      ExtensionProviderUtils.forEachExtensionsManager(flushPartition);
   
      final GlobalParameters params = GlobalParameters.globals();
      params.set(AUTHENTICATION_MODE_PROPERTY, AUTHENTICATION_MODE_INTERNAL);
      params.set(AUTHORIZATION_SYNC_CLASS_PROPERTY, "None");
   }
   
}
