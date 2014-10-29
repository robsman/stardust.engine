/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.query.statistics;

/**
 * <p>
 * This class contains constants related to the model
 * used for statistics query tests.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class StatisticsQueryModelConstants
{
   /**
    * the ID of the model used for statistics query tests
    */
   /* package-private */ static final String MODEL_ID = "StatisticsQueryModel";

   /**
    * the process definition model ID prefix for model {@link #MODEL_ID}
    */
   /* package-private */ static final String MODEL_ID_PREFIX = "{" + MODEL_ID + "}";


   /**
    * the ID of the process definition having one interactive activity
    */
   /* package-private */ static final String PROCESS_DEF_ID_DO_WORK = MODEL_ID_PREFIX + "DoWork";

   /**
    * the ID of the process definition having two interactive activities and one application activity
    */
   /* package-private */ static final String PROCESS_DEF_ID_PROCESSING_TIME = MODEL_ID_PREFIX + "ProcessingTime";


   /**
    * the ID of the last interactive activity of process definition {@link #PROCESS_DEF_ID_DO_WORK}
    */
   /* package-private */ static final String ACTIVITY_ID_WORK = "Work";

   /**
    * the ID of the application activity of process definition {@link #PROCESS_DEF_ID_PROCESSING_TIME}
    */
   /* package-private */ static final String ACTIVITY_ID_APP_ACTIVITY = "ApplicationActivity";

   /**
    * the ID of the last interactive activity of process definition {@link #PROCESS_DEF_ID_PROCESSING_TIME}
    */
   /* package-private */ static final String ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY = "LastInteractiveActivity";
}
