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
package org.eclipse.stardust.test.transientpi;

/**
 * <p>
 * This class contains constants related to the model used for tests
 * dealing with the <i>Transient Process Instance</i> functionality.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$ 
 */
/* package-private */ class TransientProcessInstanceModelConstants
{
   /**
    * the ID of the model
    */
   /* package-private */ static final String MODEL_ID = "TransientProcessInstanceModel";
   
   
   /**
    * the ID of the non-forked process definition
    */
   /* package-private */ static final String PROCESS_DEF_ID_NON_FORKED = "Non_forkedProcess";
   
   /**
    * the ID of the forked process definition
    */
   /* package-private */ static final String PROCESS_DEF_ID_FORKED = "ForkedProcess";
   
   /**
    * the ID of the non-forked process definition that fails
    */
   /* package-private */ static final String PROCESS_DEF_ID_NON_FORKED_FAIL = "Non_forkedProcessFail";   

   /**
    * the ID of the forked process definition that fails
    */
   /* package-private */ static final String PROCESS_DEF_ID_FORKED_FAIL = "ForkedProcessFail";
   
   /**
    * the ID of the process definition that contains an AND split and is executed transiently
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_TRANSIENT = "SplitProcessTransient";
   
   /**
    * the ID of the process definition that contains two AND splits
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_SPLIT = "SplitSplitProcess";
   
   /**
    * the ID of the process definition that enforces a rollback
    */
   /* package-private */ static final String PROCESS_DEF_ID_ROLLBACK = "RollbackProcess";
   
   /**
    * the ID of the process definition that has a transient and a non-transient route
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE = "TransientAndNon_transientRoute";
   
   /**
    * the ID of the process definition that switches from transient to non-transient execution
    */
   /* package-private */ static final String PROCESS_DEF_ID_FROM_TRANSIENT_TO_NON_TRANSIENT = "FromTransientToNon_transient";
   
   /**
    * the ID of the process definition that can be started via JMS
    */
   /* package-private */ static final String PROCESS_DEF_ID_TRANSIENT_VIA_JMS = "TransientProcessViaJMSTrigger";
   
   /**
    * the ID of the process definition that contains an AND split and uses deferred persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_DEFERRED = "SplitProcessDeferredPersist";
   
   /**
    * the ID of the process definition that contains an AND split and uses default persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_DEFAULT = "SplitProcessDefaultPersist";
   
   /**
    * the ID of the process definition that contains an AND split and uses immediate persist
    */
   /* package-private */ static final String PROCESS_DEF_ID_SPLIT_IMMEDIATE = "SplitProcessImmediatePersist";

   /**
    * the ID of the process definition that contains two subprocesses
    */
   /* package-private */ static final String PROCESS_DEF_ID_SUB_SUB_PROCESS = "Sub_Sub_Process";
   
   /**
    * the ID of the process definition that contains a while loop
    */
   /* package-private */ static final String PROCESS_DEF_ID_WHILE_LOOP = "WhileLoop";

   /**
    * the ID of the process definition that contains a repeat loop
    */
   /* package-private */ static final String PROCESS_DEF_ID_REPEAT_LOOP = "RepeatLoop";
   
   
   /**
    * the ID of the data in model 'TransientAndNon_transientRoute' determining whether the transient or
    * the non-transient route should be taken
    */
   /* package-private */ static final String DATA_ID_TRANSIENT_ROUTE = "TransientRoute";
}
