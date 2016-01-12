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

import javax.xml.namespace.QName;

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
   public static final String MODEL_ID = "StatisticsQueryModel";

   /**
    * the ID of the model used for cross model statistics query tests
    */
   public static final String CROSS_MODEL_ID = "StatisticsQueryCrossModel";

   /**
    * the ID of the process definition having one interactive activity
    */
   /* package-private */ static final String PROCESS_DEF_ID_DO_WORK = new QName(MODEL_ID, "DoWork").toString();

   /**
    * the ID of the process definition having two interactive activities and one application activity
    */
   /* package-private */ static final String PROCESS_DEF_ID_AI_PROCESSING_TIME = new QName(MODEL_ID, "AIProcessingTime").toString();

   /**
    * the ID of the process definition having two subprocesses
    */
   /* package-private */ static final String PROCESS_DEF_ID_PI_PROCESSING_TIME_A = new QName(MODEL_ID, "PIProcessingTime_A").toString();

   /**
    * the ID of the process definition having cross model subprocess
    */
   /* package-private */ static final String PROCESS_DEF_ID_PI_CROSS_PROCESSING_TIME = new QName(CROSS_MODEL_ID, "CrossProcessingTime").toString();

   /**
    * the ID of the last interactive activity of process definition {@link #PROCESS_DEF_ID_DO_WORK}
    */
   /* package-private */ static final String ACTIVITY_ID_WORK = "Work";

   /**
    * the ID of the last interactive activity of process definition {@link #PROCESS_DEF_ID_AI_PROCESSING_TIME}
    */
   /* package-private */ static final String ACTIVITY_ID_LAST_INTERACTIVE_ACTIVITY = "LastInteractiveActivity";
}
