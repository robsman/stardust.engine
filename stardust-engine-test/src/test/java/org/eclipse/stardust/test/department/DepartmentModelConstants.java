/**********************************************************************************
 * Copyright (c) 2012, 2015 SunGard CSA LLC and others.
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
 * This class contains constants related to the model
 * used for tests dealing with the <i>Department</i> functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class DepartmentModelConstants
{
   /**
    * the name of the model used for <i>Department</i> tests
    */
   public static final String MODEL_NAME = "DepartmentModel";


   /******************************************************************
    *                                                                *
    *                      Organization IDs                          *
    *                                                                *
    ******************************************************************/

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG_ID_1 = "O1";

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG_ID_2 = "O2";

   /**
    * a scoped sub organization of {@link DepartmentModelConstants#ORG_ID_2}
    */
   /* package-private */ static final String SUB_ORG_ID_2 = "SubO2";

   /**
    * a scoped sub organization of {@link DepartmentModelConstants#SUB_ORG_ID_2}
    */
   /* package-private */ static final String SUB_SUB_ORG_ID_2 = "SubSubO2";

   /**
    * an unscoped organization
    */
   /* package-private */ static final String ORG_ID_3 = "O3";

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG1_ID = "Org1";

   /**
    * an unscoped organization
    */
   /* package-private */ static final String ORG2_ID = "Org2";

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG3_ID = "Org3";

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG4_ID = "Org4";

   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG5_ID = "Org5";

   /**
    * a scoped organization
    */
   /* package-private */ static final String READER_ORG_ID = "Reader";


   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG_ENUM_SD_ID = "OrgEnumSD";


   /**
    * a scoped organization
    */
   /* package-private */ static final String ORG_ENUM_JAVA_ID = "OrgEnumJava";


   /******************************************************************
    *                                                                *
    *                            Role IDs                            *
    *                                                                *
    ******************************************************************/

   /**
    * an unscoped role
    */
   /* package-private */ static final String ROLE1_ID = "Role1";

   /**
    * the 'Administrator' role
    */
   /* package-private */ static final String ROLE_ADMIN_ID = "Administrator";

   /**
    * a role with no permission to delegate
    */
   /* package-private */ static final String NONE_ROLE_ID = "None";

   /**
    * a role with the permission 'delegate to others'
    */
   /* package-private */ static final String DTO_ROLE_ID = "Dto";

   /**
    * a role with the permission 'delegate to department'
    */
   /* package-private */ static final String DTD_ROLE_ID = "Dtd";


   /******************************************************************
    *                                                                *
    *                   'Department Data' IDs                        *
    *                                                                *
    ******************************************************************/

   /**
    * the ID of 'department data' 'Country Code'
    */
   /* package-private */ static final String COUNTRY_CODE_DATA_NAME = "CountryCode";

   /**
    * the ID of 'department data' 'A'
    */
   /* package-private */ static final String A_SCOPE = "A";

   /**
    * the ID of 'department data' 'X'
    */
   /* package-private */ static final String X_SCOPE = "X";

   /**
    * the ID of 'department data' 'Y'
    */
   /* package-private */ static final String Y_SCOPE = "Y";

   /**
    * the ID of 'department data' 'Z'
    */
   /* package-private */ static final String Z_SCOPE = "Z";

   /**
    * the ID of 'department data' 'ScopeDataEnumSD'
    */
   /* package-private */ static final String ENUM_SD_SCOPE = "ScopeDataEnumSD";

   /**
    * the ID of 'department data' 'ScopeDataEnumJava'
    */
   /* package-private */ static final String ENUM_JAVA_SCOPE = "ScopeDataEnumJava";


   /******************************************************************
    *                                                                *
    *                        Department IDs                          *
    *                                                                *
    ******************************************************************/

   /**
    * the ID of department 'DE'
    */
   /* package-private */ static final String DEPT_ID_DE = "DE";

   /**
    * the ID of department 'EN'
    */
   /* package-private */ static final String DEPT_ID_EN = "EN";

   /**
    * the ID of (sub-) department 'North'
    */
   /* package-private */ static final String SUB_DEPT_ID_NORTH = "North";

   /**
    * the ID of (sub-) department 'South'
    */
   /* package-private */ static final String SUB_DEPT_ID_SOUTH = "South";

   /**
    * the ID of (sub-) department 'HH'
    */
   /* package-private */ static final String SUB_SUB_DEP_ID_HH = "HH";

   /**
    * the ID of department 'a'
    */
   /* package-private */ static final String DEP_ID_A = "a";

   /**
    * the ID of department 'b'
    */
   /* package-private */ static final String DEP_ID_B = "b";

   /**
    * the ID of department 'u'
    */
   /* package-private */ static final String DEPT_ID_U = "u";

   /**
    * the ID of department 'v'
    */
   /* package-private */ static final String DEPT_ID_V = "v";

   /**
    * the ID of (sub-) department 'i'
    */
   /* package-private */ static final String SUB_DEPT_ID_I = "i";

   /**
    * the ID of (sub-) department 'j'
    */
   /* package-private */ static final String SUB_DEPT_ID_J = "j";

   /**
    * the ID of (sub-) department 'k'
    */
   /* package-private */ static final String SUB_DEPT_ID_K = "k";

   /**
    * the ID of (sub-) department 'm'
    */
   /* package-private */ static final String SUB_SUB_DEP_ID_M = "m";

   /**
    * the ID of (sub-) department 'n'
    */
   /* package-private */ static final String SUB_SUB_DEP_ID_N = "n";

   /**
    * the ID of department 'ABC'
    */
   /* package-private */ static final String DEPT_ID_ABC = "ABC";

   /**
    * the ID of department 'KLM'
    */
   /* package-private */ static final String DEPT_ID_KLM = "KLM";

   /**
    * the ID of department 'XYZ'
    */
   /* package-private */ static final String DEPT_ID_XYZ = "XYZ";

   /******************************************************************
    *                                                                *
    *                   Process Definition IDs                       *
    *                                                                *
    ******************************************************************/

   /**
    * identifies process definition #1
    */
   /* package-private */ static final String PROCESS_ID_1 = "P1";

   /**
    * identifies process definition #2
    */
   /* package-private */ static final String PROCESS_ID_2 = "P2";

   /**
    * identifies process definition #3
    */
   /* package-private */ static final String PROCESS_ID_3 = "P3";

   /**
    * identifies process definition #4
    */
   /* package-private */ static final String PROCESS_ID_4 = "P4";

   /**
    * identifies process definition #5
    */
   /* package-private */ static final String PROCESS_ID_5 = "P5";

   /**
    * identifies process definition #6
    */
   /* package-private */ static final String PROCESS_ID_6 = "P6";

   /**
    * identifies process definition #7
    */
   /* package-private */ static final String PROCESS_ID_7 = "P7";

   /**
    * identifies process definition #8
    */
   /* package-private */ static final String PROCESS_ID_8 = "P8";


   /**
    * identifies process definition #9
    */
   /* package-private */ static final String PROCESS_ID_9 = "P9";


   /**
    * identifies process definition #10
    */
   /* package-private */ static final String PROCESS_ID_10 = "P10";


   /******************************************************************
    *                                                                *
    *                        Activity IDs                            *
    *                                                                *
    ******************************************************************/
   /**
    * identifies the final activity in {@link #PROCESS_ID_1}
    */
   /* package-private */ static final String FINAL_ACTIVITY_IN_PD_1_ID = "Activity_1";


   /******************************************************************
    *                                                                *
    *                    Exception Handler IDs                       *
    *                                                                *
    ******************************************************************/

   /**
    * the ID of an exception handler
    */
   /* package-private */ static final String ON_EXCEPTION_HANDLER_ID = "OnExceptionHandler";


   private DepartmentModelConstants()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
