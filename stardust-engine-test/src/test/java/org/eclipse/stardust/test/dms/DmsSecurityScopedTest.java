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
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
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
 * <i>JCR Security</i> related use cases having a department scoped participant hierarchy.
 * </p>
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DmsSecurityScopedTest
{
   private static final String FOLDER_NAME = "Folder";
   private static final String DOCUMENT_NAME = "Document";

   private static final String SCOPED_ORG1 = "ScopedOrg1";
   private static final String SCOPED_ORG2 = "ScopedOrg2";
   private static final String SCOPED_ROLE1 = "ScopedRole1";

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

   private DmsPrincipal scopedPrincipal_d2_1;

   private DmsPrincipal scopedPrincipal_d2_2;

   @Before
   public void setUp()
   {
      final FolderInfo folderInfo = DmsUtils.createFolderInfo(FOLDER_NAME);
      folder = sf.getDocumentManagementService().createFolder("/", folderInfo);
      final DocumentInfo docInfo = DmsUtils.createDocumentInfo(DOCUMENT_NAME);
      doc = sf.getDocumentManagementService().createDocument(folder.getId(), docInfo);

      final ModelParticipant scopedOrg1 = (ModelParticipant) sf.getQueryService().getParticipant(SCOPED_ORG1);
      final ModelParticipant scopedOrg2 = (ModelParticipant) sf.getQueryService().getParticipant(SCOPED_ORG2);
      final ModelParticipant scopedRole1 = (ModelParticipant) sf.getQueryService().getParticipant(SCOPED_ROLE1);

      Department d1 = sf.getAdministrationService().createDepartment("d1", "d1", null, null, (OrganizationInfo) scopedOrg1);
      Department d2_1 = sf.getAdministrationService().createDepartment("d2", "d2", null, d1, (OrganizationInfo) scopedOrg2);

      Department d3 = sf.getAdministrationService().createDepartment("d3", "d3", null, null, (OrganizationInfo) scopedOrg1);
      Department d2_2 = sf.getAdministrationService().createDepartment("d2", "d2", null, d3, (OrganizationInfo) scopedOrg2);

      UserHome.create(sf, "ud2_1",  d2_1.getScopedParticipant(scopedRole1));
      scopedPrincipal_d2_1 = new DmsPrincipal(scopedRole1, d2_1, DMS_MODEL_NAME);
      Assert.assertEquals("{ipp-participant}{DmsModel}ScopedRole1[d1/d2]", scopedPrincipal_d2_1.getName());

      UserHome.create(sf, "ud2_2",  d2_2.getScopedParticipant(scopedRole1));
      scopedPrincipal_d2_2 = new DmsPrincipal(scopedRole1, d2_2, DMS_MODEL_NAME);
      Assert.assertEquals("{ipp-participant}{DmsModel}ScopedRole1[d3/d2]", scopedPrincipal_d2_2.getName());
   }


   @Test
   public void testScopedParticipantWithSameDepartmentName()
   {
      DmsPrivilege allPrivilege = DmsPrivilege.ALL_PRIVILEGES;

      /* (1) deny read on document ... */
      final Set<AccessControlPolicy> policies1 = retrievePoliciesFor(folder.getId());
      final AccessControlPolicy policy1 = policies1.iterator().next();
      policy1.addAccessControlEntry(scopedPrincipal_d2_1, Collections.<Privilege>singleton(allPrivilege), AccessControlEntry.EntryType.ALLOW);
      sf.getDocumentManagementService().setPolicy(folder.getId(), policy1);
      /* ... and assert that it's set */
      final Set<AccessControlPolicy> setPolicies1 = retrievePoliciesFor(folder.getId());
      assertAccessControlEntryIsSet(new DmsAccessControlEntry(scopedPrincipal_d2_1, Collections.<Privilege>singleton(allPrivilege), AccessControlEntry.EntryType.ALLOW), setPolicies1);

      ServiceFactory sfud2_1 = ServiceFactoryLocator.get("ud2_1","ud2_1");
      try
      {
         DocumentManagementService dms = sfud2_1.getDocumentManagementService();

         // can read doc
         Assert.assertNotNull(dms.getDocument(doc.getId()));
         // privilege is there
         Assert.assertEquals(Collections.singleton(allPrivilege), dms.getPrivileges(doc.getId()));
      }
      finally
      {
         sfud2_1.close();
      }

      ServiceFactory sfud2_2 = ServiceFactoryLocator.get("ud2_2","ud2_2");
      try
      {
         DocumentManagementService dms = sfud2_2.getDocumentManagementService();

         // can read doc
         Assert.assertNotNull(dms.getDocument(doc.getId()));
         // privilege is only read
         Assert.assertEquals(Collections.singleton(DmsPrivilege.READ_PRIVILEGE), dms.getPrivileges(doc.getId()));
      }
      finally
      {
         sfud2_2.close();
      }


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
