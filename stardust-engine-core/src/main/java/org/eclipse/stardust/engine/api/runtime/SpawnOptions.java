/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Container class for options that controls how the spawning operation has to be performed.
 *
 * @author Florin.Herinean
 */
public class SpawnOptions implements Serializable
{
   private static final long serialVersionUID = 2L;

   /**
    * The default spawn options used when no options are specified.
    */
   public static final SpawnOptions DEFAULT = new SpawnOptions("");

   /**
    * The processing behavior of the originating process instance. Must be <b><code>true</code></b>.
    */
   private SpawnMode spawnMode;

   /**
    * The operation comment.
    */
   private String comment;

   /**
    * Instructions on how the data will be copied to the spawned process.
    */
   private DataCopyOptions dataCopyOptions;

   /**
    * Contains information about the started activities.
    */
   private ProcessStateSpec processStateSpec;

   /**
    * Creates a default spawn options which aborts the source process instance and starts the spawned
    * process with the default start activity and copies all data from the source process instance to
    * the spawned process instance.
    *
    * @param comment the spawning comment.
    */
   public SpawnOptions(String comment)
   {
      this(new ProcessStateSpec(), SpawnMode.ABORT, comment, null);
   }


   /**
    * Creates a new SpawnOptions that allows to specify the starting activity and detailed data copy options.
    *
    * @param startActivity the activity from which the spawned process instance should start.
    *        If null, the spawned process instance will start from the default start activity.
    * @param abortProcessInstance use true to abort the originating process instance.
    * @param comment a comment describing the operation. May be null.
    * @param dataCopyOptions instructions on how the data should be transferred from the
    *        originating process instance to the spawned process instance. If null, then
    *        {@link DataCopyOptions#DEFAULT} is used.
    * @deprecated use {@link #SpawnOptions(String, SpawnMode, String, DataCopyOptions)}
    */
   @Deprecated
   public SpawnOptions(String startActivity, boolean abortProcessInstance, String comment,
         DataCopyOptions dataCopyOptions)
   {
      this(ProcessStateSpec.simpleSpec(startActivity), abortProcessInstance ? SpawnMode.ABORT : SpawnMode.KEEP, comment, dataCopyOptions);
   }

   /**
    * Creates a new SpawnOptions that allows to specify the starting activity and detailed data copy options.
    *
    * @param startActivity the activity from which the spawned process instance should start.
    *        If null, the spawned process instance will start from the default start activity.
    * @param spawnMode specifies what action to take with the originating process instance. {@link SpawnMode#KEEP}, {@link SpawnMode#ABORT} or {@link SpawnMode#HALT}.
    * @param comment a comment describing the operation. May be null.
    * @param dataCopyOptions instructions on how the data should be transferred from the
    *        originating process instance to the spawned process instance. If null, then
    *        {@link DataCopyOptions#DEFAULT} is used.
    */
   public SpawnOptions(String startActivity, SpawnMode spawnMode, String comment,
         DataCopyOptions dataCopyOptions)
   {
      this(ProcessStateSpec.simpleSpec(startActivity), spawnMode, comment, dataCopyOptions);
   }

   /**
    * Creates a new SpawnOptions that allows to specify the starting activity and detailed data copy options.
    *
    * @param ProcessStateSpec information about the started activities.
    * @param spawnMode specifies what action to take with the originating process instance. {@link SpawnMode#Keep}, {@link SpawnMode#ABORT} or {@link SpawnMode#HALT}.
    * @param comment a comment describing the operation. May be null.
    * @param dataCopyOptions instructions on how the data should be transferred from the
    *        originating process instance to the spawned process instance. If null, then
    *        {@link DataCopyOptions.DEFAULT} is used.
    */
   private SpawnOptions(ProcessStateSpec processStateSpec, SpawnMode spawnMode, String comment,
         DataCopyOptions dataCopyOptions)
   {
      this.processStateSpec = processStateSpec;
      this.spawnMode = spawnMode;
      this.comment = comment;
      this.dataCopyOptions = dataCopyOptions;
   }

   /**
    * Retrieves the starting activity id. May be null, in which case the spawned process should start
    * with the default starting activity.
    *
    * @return the id of the activity from which the spawned process instance will start.
    * @deprecated since Stardust 3.0 / I.P.P. 9.0
    */
   public String getStartActivity()
   {
      if (processStateSpec == null)
      {
         return null;
      }
      Iterator<List<String>> jumpTargets = processStateSpec.iterator();
      return jumpTargets.hasNext() ? jumpTargets.next().get(0) : null;
   }

   /**
    * Retrieves the specification of the started activities.
    *
    * @return the specification of the started activities.
    */
   public ProcessStateSpec getProcessStateSpec()
   {
      return processStateSpec ;
   }

   /**
    * Retrieves the processing behavior of the originating process instance.
    *
    * @return true, if the originating process instance should be aborted.
    */
   public boolean isAbortProcessInstance()
   {
      return SpawnMode.ABORT.equals(spawnMode);
   }

   /**
    * Retrieves the processing behavior of the originating process instance.
    *
    * @return true, if the originating process instance should be halted.
    */
   public boolean isHaltProcessInstance()
   {
      return SpawnMode.HALT.equals(spawnMode);
   }

   /**
    * Retrieves the comment associated with the spawning operation.
    *
    * @return the comment or null.
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * Retrieves the options that controls how the data is copied between the originating and
    * the spawned process instance.
    *
    * @return a DataCopyOptions or null if the default options are to be used.
    */
   public DataCopyOptions getDataCopyOptions()
   {
      return dataCopyOptions;
   }

   /**
    * Specifies what action to take with the originating process instance.
    * 
    */
   public enum SpawnMode
   {
      KEEP,
      ABORT,
      HALT
   }
}
