/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.examples;

import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.test.api.ClientServiceFactory;
import org.eclipse.stardust.test.api.LocalJcrH2Test;
import org.eclipse.stardust.test.api.ModelDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * <p>
 * This is an example for a class that contains functional tests
 * for the <code>DocumentManagementService</code> running in a
 * local Spring environment, using a H2 DB and providing JCR support.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class MyDmsTest extends LocalJcrH2Test
{
   private static final String MODEL_NAME = "MyExampleModel";
   private static final String DOC_NAME = "MyDoc";
   private static final String TOP_LEVEL_FOLDER = "/";
   
   @Rule
   public ClientServiceFactory serviceFactory = new ClientServiceFactory(MOTU, MOTU);
   
   @Before
   public void setUp()
   {
      ModelDeployer.deploy(MODEL_NAME, serviceFactory);
   }
   
   @After
   public void tearDown()
   {
      serviceFactory.getAdministrationService().cleanupRuntimeAndModels();
   }
   
   @Test
   public void testCreateAndRetrieveDocument()
   {
      final DocumentManagementService dms = serviceFactory.getDocumentManagementService();
      final DocumentInfo docInfo = DmsUtils.createDocumentInfo(DOC_NAME);
      final Document createdDoc = dms.createDocument(TOP_LEVEL_FOLDER, docInfo);
      final Document retrievedDoc = dms.getDocument(createdDoc.getId());
      
      assertNotNull(retrievedDoc);
      assertThat(createdDoc.getId(), equalTo(retrievedDoc.getId()));
   }
}
