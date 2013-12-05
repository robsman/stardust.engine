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

import static org.eclipse.stardust.test.dms.DmsModelConstants.DMS_MODEL_NAME;
import static org.eclipse.stardust.test.dms.DmsModelConstants.ROLE_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.extensions.dms.data.DmsAccessControlEntry;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.test.api.setup.DmsAwareTestMethodSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class contains tests focusing on
 * <i>JCR Security</i> related use cases.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class DmsSecurityTest
{
   private static final String FOLDER_NAME = "Folder";
   private static final String DOCUMENT_NAME = "Document";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final DmsAwareTestMethodSetup testMethodSetup = new DmsAwareTestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, DMS_MODEL_NAME);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);
   
   private Document doc;
   
   private DmsPrincipal principal;
   
   @Before
   public void setUp()
   {
      final FolderInfo folderInfo = DmsUtils.createFolderInfo(FOLDER_NAME);
      final Folder folder = sf.getDocumentManagementService().createFolder("/", folderInfo);
      final DocumentInfo docInfo = DmsUtils.createDocumentInfo(DOCUMENT_NAME);
      doc = sf.getDocumentManagementService().createDocument(folder.getId(), docInfo);
      
      final ModelParticipant role1 = (ModelParticipant) sf.getQueryService().getParticipant(ROLE_ID);
      principal = new DmsPrincipal(role1, DMS_MODEL_NAME);      
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
