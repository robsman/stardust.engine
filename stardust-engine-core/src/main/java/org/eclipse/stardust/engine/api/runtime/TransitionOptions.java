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

/**
 * Specifies options for searching possible targets for relocation transitions. 
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class TransitionOptions implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   /**
    * Default options that limits the search to the process definition of the specified activity instance.
    */
   public static final TransitionOptions DEFAULT = new TransitionOptions(false, false/*, false*/);
   
   /**
    * Full options that allows relocation transitions inside sub processes and out of them.
    */
   public static final TransitionOptions FULL = new TransitionOptions(true, true/*, false*/);
   
   private boolean transitionOutOfSubprocessesAllowed = false;
   private boolean transitionIntoSubprocessesAllowed = false;
   //private boolean activityHistoryUsed = false;

   private String processIdPattern;
   private String activityIdPattern;
   
   
   
   /**
    * Constructs a new instance of transition options.
    * 
    * @param allowTransitionOutOfSubprocesses specifies that transitions out of the sub process should be allowed.
    * @param allowTransitionIntoSubprocesses specifies that transitions into sub process should be allowed.
    */
   public TransitionOptions(
         boolean allowTransitionOutOfSubprocesses,
         boolean allowTransitionIntoSubprocesses)
   {
      this.transitionOutOfSubprocessesAllowed = allowTransitionOutOfSubprocesses;
      this.transitionIntoSubprocessesAllowed = allowTransitionIntoSubprocesses;
   }

   /*public TransitionOptions(
         boolean allowTransitionOutOfSubprocesses,
         boolean allowTransitionIntoSubprocesses,
         boolean useActivityHistory)
   {
      this.transitionOutOfSubprocessesAllowed = allowTransitionOutOfSubprocesses;
      this.transitionIntoSubprocessesAllowed = allowTransitionIntoSubprocesses;
      this.activityHistoryUsed = useActivityHistory;
   }*/

   /**
    * Constructs a new instance of transition options.
    * 
    * @param allowTransitionOutOfSubprocesses specifies that transitions out of the sub process should be allowed.
    * @param allowTransitionIntoSubprocesses specifies that transitions into sub process should be allowed.
    * @param processIdPattern regex expression to match process definition ids.
    * @param activityIdPattern regex expression to match activity ids.
    */
   public TransitionOptions(
         boolean allowTransitionOutOfSubprocesses,
         boolean allowTransitionIntoSubprocesses,
         String processIdPattern, String activityIdPattern)
   {
      this.transitionOutOfSubprocessesAllowed = allowTransitionOutOfSubprocesses;
      this.transitionIntoSubprocessesAllowed = allowTransitionIntoSubprocesses;
      this.processIdPattern = processIdPattern;
      this.activityIdPattern = activityIdPattern;
   }

   /**
    * Checks if transitions out of the sub process should be allowed. 
    */
   public boolean isTransitionOutOfSubprocessesAllowed()
   {
      return transitionOutOfSubprocessesAllowed;
   }

   /**
    * Checks if transitions into sub processes should be allowed. 
    */
   public boolean isTransitionIntoSubprocessesAllowed()
   {
      return transitionIntoSubprocessesAllowed;
   }

   /*public boolean isActivityHistoryUsed()
   {
      return activityHistoryUsed;
   }*/

   public String getProcessIdPattern()
   {
      return processIdPattern;
   }

   public String getActivityIdPattern()
   {
      return activityIdPattern;
   }
}
