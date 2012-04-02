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

/**
 * TODO
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class SpawnOptions
{
   public static final SpawnOptions DEFAULT = new SpawnOptions("");

   /**
    * Specifies if the spawned process instance should be instantiated based on the latest version of the process definition or
    * on the version existing at the time when the source process instance was created.
    */
   //private boolean spawnLatestVersion;
   
   /**
    * The spawned process instance should be started at the specified activity.
    */
   private String startActivity;
   
   /**
    * Specifies if the source process instance should be aborted or completed.
    */
   private boolean abortProcessInstance;
   
   /**
    * Spawn comment.
    */
   private String comment;

   /**
    * Instructions on how the data will be copied to the spawned process.
    */
   private DataCopyOptions dataCopyOptions;

   /**
    * Creates a default spawn options which aborts the source process instance and starts the spawned
    * process with the default start activity and copies all data from the source process instance to
    * the spawned process instance.
    * 
    * @param comment the spawning comment.
    */
   public SpawnOptions(String comment)
   {
      this(null, true, comment, null);
   }
   
   /**
    * TODO
    * @param startActivity
    * @param abortProcessInstance
    * @param comment
    * @param dataCopyOptions
    */
   public SpawnOptions(String startActivity, boolean abortProcessInstance, String comment,
         DataCopyOptions dataCopyOptions)
   {
      this.startActivity = startActivity;
      this.abortProcessInstance = abortProcessInstance;
      this.comment = comment;
      this.dataCopyOptions = dataCopyOptions;
   }

   /**
    * Retrieves the starting activity id. May be null, in which case the spawned process should start
    * with the default starting activity.
    * 
    * @return the id of the activity from which the spawned process instance will start.
    */
   public String getStartActivity()
   {
      return startActivity;
   }

   public boolean isAbortProcessInstance()
   {
      return abortProcessInstance;
   }

   public String getComment()
   {
      return comment;
   }

   public DataCopyOptions getDataCopyOptions()
   {
      return dataCopyOptions;
   }
}
