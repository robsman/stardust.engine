/**********************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.dms;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_MODEL_NAME;
import static org.eclipse.stardust.test.dms.DmsModelConstants.ROLE_ID;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.extensions.dms.data.DmsAccessControlEntry;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

/**
 * <p>
 * This class contains tests focusing on
 * <i>JCR Security</i> related use cases.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DmsSecurityTest
{
   private static final String FOLDER_NAME = "Folder";
   private static final String DOCUMENT_NAME = "Document";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DMS_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);
   
   private Document doc;
   
   private Folder folder;

   private DmsPrincipal principal;
   
   @SuppressWarnings("deprecation")
   private DmsPrincipal everyonePrincipal = new DmsPrincipal("everyone");

   @Before
   public void setUp()
   {
      final FolderInfo folderInfo = DmsUtils.createFolderInfo(FOLDER_NAME);
      folder = sf.getDocumentManagementService().createFolder("/", folderInfo);
      final DocumentInfo docInfo = DmsUtils.createDocumentInfo(DOCUMENT_NAME);
      doc = sf.getDocumentManagementService().createDocument(folder.getId(), docInfo);
      
      final ModelParticipant role1 = (ModelParticipant) sf.getQueryService().getParticipant(ROLE_ID);

      // motu also gets role1 so we can check if he gets denied because of his granted
      // role besides Administrator.
      User user = sf.getUserService().getUser();
      user.addGrant(role1);
      sf.getUserService().modifyUser(user);

      principal = new DmsPrincipal(role1, DMS_MODEL_NAME);      

      UserHome.create(sf, "u1", role1);
      UserHome.create(sf, "u2");
   }
   
   
   /**
    * <p>
    * This test makes sure that removing existing, i.e. already set, privileges
    * works correctly.
    * </p>
    */
   @Test
   public void testRemoveExistingPrivilege()
   {
      /* (1) deny read on document ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY), setPolicies1);
      
      /* (2) remove policy by removing the access control entries ... */
      final Set<AccessControlPolicy> policies2 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy2 = policies2.iterator().next();
      policy2.removeAllAccessControlEntries();
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy2);
      /* ... and assert that its entries are removed */
      final Set<AccessControlPolicy> setPolicies2 = retrievePoliciesFor(doc.getId());
      Assert.assertTrue(setPolicies2.iterator().next().getAccessControlEntries().isEmpty());
   }   
   
   
   /**
    * <p>
    * This test makes sure that modifying existing, i.e. already set, privileges
    * works correctly.
    * </p>
    */
   @Test
   public void testModifyExistingPrivilege()
   {
      /* (1) deny read on document ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY), setPolicies1);
      
      /* (2) allow read on document ... */
      final Set<AccessControlPolicy> policies2 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy2 = policies2.iterator().next();
      policy2.removeAllAccessControlEntries();
      policy2.addAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy2);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies2 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.ALLOW), setPolicies2);
   }
   
   /**
    * <p>
    * This test makes sure that extending existing, i.e. already set, privileges
    * works correctly.
    * </p>
    */
   @Test
   public void testExtendExistingPrivilege()
   {
      /* (1) deny read on document ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_PRIVILEGE), AccessControlEntry.EntryType.DENY), setPolicies1);
      
      /* (2) allow read ACL on document ... */
      final Set<AccessControlPolicy> policies2 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy2 = policies2.iterator().next();
      policy2.addAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_ACL_PRIVILEGE), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy2);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies2 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(DmsPrivilege.READ_ACL_PRIVILEGE), AccessControlEntry.EntryType.ALLOW), setPolicies2);
   }
   
   @Test
   public void testFileACLAllowDocAfterDeniedFolder_All()
   {
      doTestFileAclAllowDocAfterDeniedFolder(DmsPrivilege.ALL_PRIVILEGES);
   }


   @Test
   public void testFileACLAllowDocAfterDeniedFolder_Read()
   {
      doTestFileAclAllowDocAfterDeniedFolder(DmsPrivilege.READ_PRIVILEGE);
   }


   private void doTestFileAclAllowDocAfterDeniedFolder(DmsPrivilege testPrivilege)
   {
      /* (1) deny read on root folder ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(folder.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.DENY);
      sf.getDocumentManagementService().setPolicy(folder.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(folder.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.DENY), setPolicies1);

      /* (2) allow read on document ... */
      final Set<AccessControlPolicy> policies2 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy2 = policies2.iterator().next();
      policy2.addAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy2);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies2 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW), setPolicies2);

      ServiceFactory sfu1 = ServiceFactoryLocator.get("u1","u1");
      try
      {
         DocumentManagementService dms = sfu1.getDocumentManagementService();

         // can read doc
         Assert.assertNotNull(dms.getDocument(doc.getId()));
         // privilege is there
         Assert.assertEquals(Collections.singleton(testPrivilege), dms.getPrivileges(doc.getId()));

         // folder is not found
         Assert.assertNull(dms.getFolder(folder.getId()));

         boolean found = true;
         try
         {
            // folder privilege reading fails because requested node is not found
            Assert.assertEquals(null, dms.getPrivileges(folder.getId()));
         }
         catch (DocumentManagementServiceException e)
         {
            found = false;
         }
         if (found)
         {
            Assert.fail();
         }
      }
      finally
      {
         sfu1.close();
      }

      DocumentManagementService dms = sf.getDocumentManagementService();
      Set<AccessControlPolicy> pol = dms.getEffectivePolicies(doc.getId());
      // one policy effective on document
      Assert.assertEquals(1, pol.size());

      Set<AccessControlPolicy> pol2 = dms.getEffectivePolicies(folder.getId());
      // to policies effective on folder
      Assert.assertEquals(2, pol2.size());
   }


   @Test
   public void testFileACLRestrictDocToRoleAfterFolderHasEveryoneAll_All()
   {
      doTestFileACLRestrictDocToRole(DmsPrivilege.ALL_PRIVILEGES);
   }


   @Test
   public void testFileACLRestrictDocToRoleAfterFolderHasEveryoneAll_Read()
   {
      doTestFileACLRestrictDocToRole(DmsPrivilege.READ_PRIVILEGE);
   }


   private void doTestFileACLRestrictDocToRole(
         DmsPrivilege testPrivilege)
   {
      /* (1) deny read on root folder ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(folder.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(everyonePrincipal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(folder.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(folder.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(everyonePrincipal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW), setPolicies1);

      /* (2) allow read on document ... */
      final Set<AccessControlPolicy> policies2 = retrievePoliciesFor(doc.getId());
      final AccessControlPolicy policy2 = policies2.iterator().next();
      policy2.addAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(doc.getId(), policy2);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies2 = retrievePoliciesFor(doc.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(principal, Collections.<Privilege>singleton(testPrivilege), AccessControlEntry.EntryType.ALLOW), setPolicies2);

      // user 1 should have access
      ServiceFactory sfu1 = ServiceFactoryLocator.get("u1", "u1");
      try
      {
         DocumentManagementService dms = sfu1.getDocumentManagementService();

         // can read doc
         Assert.assertNotNull(dms.getDocument(doc.getId()));
         // privilege is there
         Assert.assertEquals(Collections.singleton(testPrivilege),
               dms.getPrivileges(doc.getId()));

         // folder is found
         Assert.assertNotNull(dms.getFolder(folder.getId()));
         // folder has privilege
         Assert.assertEquals(Collections.singleton(testPrivilege),
               dms.getPrivileges(folder.getId()));
      }
      finally
      {
         sfu1.close();
      }

      // user 2 should have no access
      ServiceFactory sfu2 = ServiceFactoryLocator.get("u2", "u2");
      try
      {
         DocumentManagementService dms = sfu2.getDocumentManagementService();

         // can not read doc
         Assert.assertNull(dms.getDocument(doc.getId()));

         boolean found = true;
         try
         {
            // document privilege reading fails because requested node is not found
            Assert.assertEquals(null, dms.getPrivileges(doc.getId()));
         }
         catch (DocumentManagementServiceException e)
         {
            found = false;
         }
         if (found)
         {
            Assert.fail();
         }

         // folder is found
         Assert.assertNotNull(dms.getFolder(folder.getId()));
         // folder has privilege
         Assert.assertEquals(Collections.singleton(testPrivilege),
               dms.getPrivileges(folder.getId()));
      }
      finally
      {
         sfu2.close();
      }

      DocumentManagementService dms = sf.getDocumentManagementService();
      Set<AccessControlPolicy> pol = dms.getEffectivePolicies(doc.getId());
      // one policy effective on document
      Assert.assertEquals(1, pol.size());

      Set<AccessControlPolicy> pol2 = dms.getEffectivePolicies(folder.getId());
      // to policies effective on folder
      Assert.assertEquals(2, pol2.size());
   }


   private void assertAccessControlEntryIsSet(final AccessControlEntry ace, final Set<AccessControlPolicy> policies)
   {
      for (final AccessControlPolicy p : policies)
      {
         for (final AccessControlEntry a : p.getAccessControlEntries())
         {
            if (a.equals(ace))
            {
               return;
            }
         }
      }
      
      fail("Access Control Entry not found.");
   }
   
   private Set<AccessControlPolicy> retrievePoliciesFor(final String id)
   {
      final Set<AccessControlPolicy> policies = sf.getDocumentManagementService().getApplicablePolicies(id);
      if ( !policies.isEmpty())
      {
         return policies;
      }
      
      return sf.getDocumentManagementService().getPolicies(id);
   }
}
