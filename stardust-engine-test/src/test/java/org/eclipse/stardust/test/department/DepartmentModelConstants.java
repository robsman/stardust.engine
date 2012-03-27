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
package org.eclipse.stardust.test.department;

/**
 * <p>
 * This class contains constants for tests dealing with the
 * 'Department' functionality.
 * </p>
 *
 * TODO remove unused constants
 * 
 * @author Nicolas.Werlein
 * @version $Revision$ 
 */
/* package-private */ class DepartmentModelConstants
{
   /* package-private */ static final String MODEL_NAME = "DepartmentModel";
   
   /**
    * a scoped organization
    */
   public static final String ORG_ID_1 = "O1";
   
   /**
    * a scoped organization
    */
   public static final String ORG_ID_2 = "O2";
   
   /**
    * a scoped sub organization of {@link DepartmentModelConstants#ORG_ID_2} 
    */
   public static final String SUB_ORG_ID_2 = "SubO2";
   
   /**
    * a scoped sub organization of {@link DepartmentModelConstants#SUB_ORG_ID_2}
    */
   public static final String SUB_SUB_ORG_ID_2 = "SubSubO2";
   
   /**
    * an unscoped organization
    */
   public static final String ORG_ID_3 = "O3";
   
   /**
    * an unscoped organization, user for authorization tests on model level {@link TestModifyDepartment} 
    */
   public static final String ORG_ID = "Org";
   
   /**
    * a scoped organization
    */
   public static final String ORG1_ID = "Org1";
   
   /**
    * an unscoped organization
    */
   public static final String ORG2_ID = "Org2";
   
   /**
    * a scoped organization
    */
   public static final String ORG3_ID = "Org3";
   
   /**
    * a scoped organization
    */
   public static final String ORG4_ID = "Org4";
   
   /**
    * a scoped organization
    */
   public static final String ORG5_ID = "Org5";
   
   /**
    * a scoped organization 
    */
   public static final String READER_ORG_ID = "Reader";
   
   /**
    * an unscoped role
    */
   public static final String ROLE1_ID = "Role1";
   public static final String ROLE_ADMIN_ID = "Administrator";
   public static final String ROLE_DEFAU_ID = "DefaultPerformer";
   
   /**
    * an unscoped role
    */
   public static final String ROLE_SMALL_TEAM_ID = "SmallTeam";
   
   public static final String COUNTRY_CODE_DATA_NAME = "CountryCode";
   public static final String DEP_ID_DE = "DE";
   public static final String DEP_ID_EN = "EN";

   public static final String REGION_CODE_DATA_NAME = "RegionCode";
   public static final String SUB_DEP_ID_NORTH = "North";
   public static final String SUB_DEP_ID_SOUTH = "South";
   
   public static final String CITY_CODE_DATA_NAME = "CityCode";
   public static final String SUB_SUB_DEP_ID_HH = "HH";
   
   public static final String X_SCOPE = "X";
   public static final String Y_SCOPE = "Y";
   public static final String Z_SCOPE = "Z";
   public static final String A_SCOPE = "A";
   
   public static final String IN_DATA_PATH_X = "DataPathX";
   public static final String IN_DATA_PATH_Y = "DataPathY";
   public static final String IN_DATA_PATH_Z = "DataPathZ";
   public static final String IN_DATA_PATH_A = "DataPathA";
   
   
   public static final String DEP_ID_U = "u";
   public static final String DEP_ID_V = "v";
   
   public static final String SUB_DEP_ID_I = "i";
   public static final String SUB_DEP_ID_J = "j";
   public static final String SUB_DEP_ID_K = "k";
   
   public static final String SUB_SUB_DEP_ID_M = "m";
   public static final String SUB_SUB_DEP_ID_N = "n";
   
   public static final String DEP_ID_A = "a";
   public static final String DEP_ID_B = "b";
   
   public static final String O1_USERNAME = "o1";
   public static final String O1_PWD = "pwd";
   
   public static final String ORG1_USERNAME = "org1";
   public static final String ORG1_PWD = "pwd";
   
   public static final String ORG2_USERNAME = "org2";
   public static final String ORG2_PWD = "pwd";
   
   public static final String ORG3_USERNAME = "org3";
   public static final String ORG3_PWD = "pwd";
   
   public static final String ROLE1_USERNAME = "role1";
   public static final String ROLE1_PWD = "pwd";
   
   public static final String ABORT_ORG_USERNAME = "abort";
   public static final String ABORT_ORG_PWD = "pwd";
   
   /** 
    * identifies a process definition that comprises 
    * a manual activity connected to department org
    */
   public static final String PROCESS_ID_SUSPEND = "P_SUSPEND";
   
   /** 
    * identifies a process definition that comprises 
    * a activity connected to a department and a preceding
    * route activity with fork and traversal
    */
   public static final String PROCESS_ID_1 = "P1";
   
   /** 
    * identifies a process definition that comprises 
    * a single activity connected to a department
    */
   public static final String PROCESS_ID_2 = "P2";
   
   /**
    * identifies a process definition that comprises
    * a single activity connected to an organization
    * hierarchy (scoped and unscoped) 
    */
   public static final String PROCESS_ID_3 = "P3";
   
   public static final String PROCESS_ID_4 = "P4";
   
   public static final String PROCESS_ID_5 = "P5";
   
   public static final String PROCESS_ID_6 = "P6";
   
   public static final String PROCESS_ID_7 = "P7";
   
   public static final String PROCESS_ID_8 = "P8";
   

   /**
    * identifies a process definition that has scoped particpants set on process level    
    * */
   public static final String PROCESS_ID_ORG3 = "POrg3";
   /**
    * identifies a process definition that has scoped particpants set on process level    
    * */  
   public static final String PROCESS_ID_ORG2 = "POrg2";
   /**
    * identifies a process definition that has scoped particpants set on process level    
    * */   
   public static final String PROCESS_ID_ST = "PSt";
   
   /**
    * identifies a data definition that has scoped particpants set on data level    
    * */ 
   public static final String  DATA_STRING_ORG3 = "DataStringOrg3";
   public static final String  DATA_STRING_ORG2 = "DataStringOrg2";
   public static final String  DATA_STRING_ST = "DataStringST";
   /**
    * identifies an in data path or Process P_x     
    * */ 
   public static final String IN_DATA_PATH_ORG3 = "DataPathOrg3";
   public static final String IN_DATA_PATH_ORG2 = "DataPathOrg2";
   public static final String IN_DATA_PATH_ST = "DataPathST";

   
   /**
    * identifies a data definition that has scoped particpants set on data level    
    * */ 
   public static final String  DATA_STRING_STL = "DataStringOrgSMT";

   public static final String ON_EXCEPTION_HANDLER_ID = "OnExceptionHandler";
   
   private DepartmentModelConstants()
   {
      /* avoid creation of an instance */
   }
}
