/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
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
import java.util.Collections;
import java.util.Set;

/**
 * Provides key indicators of audit trail health.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class AuditTrailHealthReport implements Serializable
{
   private static final long serialVersionUID = 2L;
   
   private final long nPendingPiCompletes;
   private final long nPendingPiAborts;
   private final long nPisHavingCrashedAis;
   private final long nPisHavingCrashedThreads;
   private final long nPisHavingCrashedEventBindings;
   private final long nPendingAiAborts;

   private Set<Long> nPendingPiCompletesSet = Collections.emptySet();
   private Set<Long> nPendingPiAbortsSet = Collections.emptySet();
   private Set<Long> nPendingAiAbortsSet = Collections.emptySet();
   private Set<Long> nPisHavingCrashedAisSet = Collections.emptySet();
   private Set<Long> nPisHavingCrashedThreadsSet = Collections.emptySet();
   private Set<Long> nPisHavingCrashedEventBindingsSet = Collections.emptySet();
   
   public AuditTrailHealthReport(long nPendingPiCompletes, long nPendingPiAborts,
         long nPendingAiAborts, long nPisHavingCrashedAis, long nPisHavingCrashedThreads,
         long nPisHavingCrashedEventBindings)
   {
      this.nPendingPiCompletes = nPendingPiCompletes;
      this.nPendingPiAborts = nPendingPiAborts;
      this.nPendingAiAborts = nPendingAiAborts;
      this.nPisHavingCrashedAis = nPisHavingCrashedAis;
      this.nPisHavingCrashedThreads = nPisHavingCrashedThreads;
      this.nPisHavingCrashedEventBindings = nPisHavingCrashedEventBindings;
   }
   
   public AuditTrailHealthReport(Set<Long> nPendingPiCompletesSet,
         Set<Long> nPendingPiAbortsSet, Set<Long> nPendingAiAbortsSet,
         Set<Long> nPisHavingCrashedAisSet, Set<Long> nPisHavingCrashedThreadsSet,
         Set<Long> nPisHavingCrashedEventBindingsSet)
   {
      this(nPendingPiCompletesSet.size(), nPendingPiAbortsSet.size(), nPendingAiAbortsSet
            .size(), nPisHavingCrashedAisSet.size(), nPisHavingCrashedThreadsSet.size(),
            nPisHavingCrashedEventBindingsSet.size());
      this.nPendingPiCompletesSet = nPendingPiCompletesSet;
      this.nPendingPiAbortsSet = nPendingPiAbortsSet;
      this.nPendingAiAbortsSet = nPendingAiAbortsSet;
      this.nPisHavingCrashedAisSet = nPisHavingCrashedAisSet;
      this.nPisHavingCrashedThreadsSet = nPisHavingCrashedThreadsSet;
      this.nPisHavingCrashedEventBindingsSet = nPisHavingCrashedEventBindingsSet;
   }


   /**
    * Gets the number of process instances not having further pending activities, but not
    * beeing marked as completed itself. Performing a process recovery on such processes
    * is recommended.
    * 
    * @return The number of process instances.
    */
   public long getNumberOfProcessInstancesLackingCompletion()
   {
      return nPendingPiCompletes;
   }

   /**
    * Gets the number of process instances which had been scheduled for abortion but did
    * not succeed. 
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The number of process instances.
    */
   public long getNumberOfProcessInstancesLackingAbortion()
   {
      return nPendingPiAborts;
   }
   
   /**
    * Gets the number of activity instances which had been scheduled for abortion but did
    * not succeed. 
    * Performing a process recovery on such activities is recommended.
    * 
    * @return The number of activity instances.
    */
   public long getNumberOfActivityInstancesLackingAbortion()
   {
      return nPendingAiAborts;
   }

   /**
    * Gets the number of process instances likely to have crashed event bindings.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The number of process instances.
    */
   public long getNumberOfProcessInstancesHavingCrashedEventBindings()
   {
      return nPisHavingCrashedEventBindings;
   }
   
   /**
    * Gets the number of process instances likely to have crashed activity instances.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The number of process instances.
    */
   public long getNumberOfProcessInstancesHavingCrashedActivities()
   {
      return nPisHavingCrashedAis;
   }

   /**
    * Gets the number of process instances likely to have crashed activity threads.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The number of process instances.
    */
   public long getNumberOfProcessInstancesHavingCrashedThreads()
   {
      return nPisHavingCrashedThreads;
   }

   /**
    * Gets the set of process instances oids not having further pending activities, but not
    * beeing marked as completed itself. Performing a process recovery on such processes
    * is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getProcessInstancesLackingCompletion()
   {
      return nPendingPiCompletesSet;
   }

   /**
    * Gets the set of process instances oids which had been scheduled for abortion but did
    * not succeed. 
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getProcessInstancesLackingAbortion()
   {
      return nPendingPiAbortsSet;
   }

   /**
    * Gets the set of process instances oids having activity instances which had been scheduled for abortion but did
    * not succeed. 
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getActivityInstancesLackingAbortion()
   {
      return nPendingAiAbortsSet;
   }

   /**
    * Gets the set of process instances oids likely to have crashed activity instances.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getProcessInstancesHavingCrashedActivities()
   {
      return nPisHavingCrashedAisSet;
   }

   /**
    * Gets the set of process instances oids likely to have crashed activity threads.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getProcessInstancesHavingCrashedThreads()
   {
      return nPisHavingCrashedThreadsSet;
   }

   /**
    * Gets the set of process instances oids likely to have crashed event bindings.
    * Performing a process recovery on such processes is recommended.
    * 
    * @return The set of process instances oids.
    */
   public Set<Long> getProcessInstancesHavingCrashedEventBindings()
   {
      return nPisHavingCrashedEventBindingsSet;
   }
}