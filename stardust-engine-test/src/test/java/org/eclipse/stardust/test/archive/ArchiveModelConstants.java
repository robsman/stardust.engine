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
/* package-private */class ArchiveModelConstants
{
   /**
    * the ID of the model comprising the transient process definitions
    */
   static final String MODEL_ID = "ArchiveModel";

   /**
    * the process definition model ID prefix for model {@link #MODEL_ID}
    */
   static final String MODEL_ID_PREFIX = "{" + MODEL_ID + "}";

   static final String PROCESS_DEF_SIMPLEMANUAL = MODEL_ID_PREFIX + "SimpleManual";

   static final String PROCESS_DEF_SIMPLE = MODEL_ID_PREFIX + "Simple";

   static final String PROCESS_DEF_CALL_SUBPROCESSES_IN_MODEL = MODEL_ID_PREFIX
         + "CallSubProcessesInModel";

   public static final String DATA_ID_TEXTDATA = "TextData";

   public static final String DATA_ID_TEXTDATA1 = "TextData1";

}
