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
package org.eclipse.stardust.test.archive;

/**
 * <p>
 * This class contains constants related to the model used for tests dealing with the
 * <i>Archive</i> functionality.
 * </p>
 *
 * @author jsaayman
 * @version $Revision$
 */
public class ArchiveModelConstants
{
   /**
    * the ID of the model comprising the transient process definitions
    */
   public static final String MODEL_ID = "ArchiveModel";

   static final String MODEL_ID_OTHER = "ArchiveModelOther";
   
   /**
    * the process definition model ID prefix for model {@link #MODEL_ID}
    */
   static final String MODEL_ID_PREFIX = "{" + MODEL_ID + "}";

   static final String PROCESS_DEF_SIMPLEMANUAL = MODEL_ID_PREFIX + "SimpleManual";

   static final String PROCESS_DEF_SIMPLE = MODEL_ID_PREFIX + "Simple";

   static final String PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL = MODEL_ID_PREFIX
         + "CallSubProcessesInModel";
   
   static final String PROCESS_DEF_CALL_SCRIPTPROCESS = MODEL_ID_PREFIX
         + "ScriptProcess";

   static final String DATA_ID_TEXTDATA = "TextData";

   static final String DATA_ID_TEXTDATA1 = "TextData1";

   static final String DATA_ID_NUMBERVALUE = "NumberValue";
   
   static final String DATA_ID_STRUCTUREDDATA = "StructuredData";

   static final String DATA_ID_STRUCTUREDDATA_MYFIELDB = "MyFieldB";

}
