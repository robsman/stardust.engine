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
package org.eclipse.stardust.test.deputy;

/**
 * <p>
 * This class contains constants related to the model
 * used for tests dealing with the <i>Deputy</i> functionality.
 * </p>
 *
 * @author Barry.Grotjahn
 * @version $Revision: 66675 $
 */
/* package-private */ class DeputyModelConstants
{
   /**
    * the name of the model used for <i>Deputy</i> tests
    */
   /* package-private */ static final String MODEL_NAME = "DeputyModel";


   /******************************************************************
    *                                                                *
    *                            Role IDs                            *
    *                                                                *
    ******************************************************************/

   /**
    * the 'Administrator' role
    */
   /* package-private */ static final String ROLE_ADMIN_ID = "Administrator";

   /**
    * has manage deputies grant
    */
   /* package-private */ static final String ROLE1_ID = "Role1";

   /**
    * has manage deputies grant
    */
   /* package-private */ static final String ROLE2_ID = "Role2";

   /**
    * without manage deputies grant
    */
   /* package-private */ static final String ROLE3_ID = "Role3";

   /**
    * without manage deputies grant
    */
   /* package-private */ static final String ROLE4_ID = "Role4";


   /******************************************************************
    *                                                                *
    *                            User IDs                            *
    *                                                                *
    ******************************************************************/

   /**
    * has manage deputies grant
    */
   /* package-private */ static final String USER1_ID = "test1";

   /**
    * has manage deputies grant
    */
   /* package-private */ static final String USER2_ID = "test2";

   /**
    * without manage deputies grant
    */
   /* package-private */ static final String USER3_ID = "test3";

   /**
    * without manage deputies grant
    */
   /* package-private */ static final String USER4_ID = "test4";


   private DeputyModelConstants()
   {
   }
}