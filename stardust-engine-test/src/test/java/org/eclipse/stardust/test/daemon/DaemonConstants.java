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
package org.eclipse.stardust.test.daemon;

/**
 * <p>
 * This class contains constants
 * for tests dealing with the <i>Daemon</i> functionality.
 * </p>
 *
 * @author Barry.Grotjahn
 */
public class DaemonConstants
{
   /**
    * the name of the models used for <i>Daemon</i> tests
    */   
   public static final String MODEL_NAME = "BusinessObjectManagement";

   /**
    * dms related constants
    */      
   public static final String FOLDER = "/business-calendars";
   public static final String CALENDAR = "test.bpmcal";   
   public static final String BLOCK_CALENDAR = "block.bpmcalx";
   public static final String IMPORTED_CALENDAR = "common.bpmcalx";

   public static final String BO_FUND = "{BusinessObjectManagement}Fund";
   public static final String BO_GROUP = "{BusinessObjectManagement}FundGroup";
      
   /**
    * the 'Administrator' role
    */
   /* package-private */ static final String ROLE_ADMIN_ID = "Administrator";

   private DaemonConstants()
   {
      /* utility class; do not allow the creation of an instance */
   }
}