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
package org.eclipse.stardust.test.businessobject;

/**
 * <p>
 * This class contains constants related to the model
 * used for tests dealing with the <i>BO</i> functionality.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class BusinessObjectModelConstants
{
   public static final int TIME_LAPSE = 1000 * 60 * 60 * 24 * 2;
      
   /**
    * the name of the models used for <i>BO</i> tests
    */
   public static final String MODEL_NAME1 = "BOModel";
   public static String qualifiedBusinessObjectId1 = "{BOModel}StructuredData1";
   
   public static final String MODEL_NAME2 = "BusinessObjectManagement";
   public static final String MODEL_NAME3 = "EmployeeModel";
      
   /**
    * the 'Administrator' role
    */
   /* package-private */ static final String ROLE_ADMIN_ID = "Administrator";

   private BusinessObjectModelConstants()
   {
      /* utility class; do not allow the creation of an instance */
   }
}