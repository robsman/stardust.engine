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
 * @author Nicolas.Werlein
 */
public class BusinessObjectModelConstants
{
   /**
    * the name of the model used for <i>BO</i> tests
    */
   public static final String MODEL_NAME = "BOModel";
   
   public static String qualifiedBusinessObjectId1 = "{BOModel}StructuredData1";
   /**
    * the 'Administrator' role
    */
   /* package-private */ static final String ROLE_ADMIN_ID = "Administrator";

   private BusinessObjectModelConstants()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
