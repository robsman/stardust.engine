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
   public static final TransitionOptions DEFAULT = new TransitionOptions(false, false, false);
   
   /**
    * Full options that allows relocation transitions inside sub processes and out of them.
    */
   public static final TransitionOptions FULL = new TransitionOptions(true, true, true);
   
   private boolean transitionOutOfSubprocessesAllowed;
   private boolean transitionIntoSubprocessesAllowed;
   private boolean loopsAllowed;

   private String processIdPattern;
   private String activityIdPattern;
   
   
   
   /**
    * Constructs a new instance of transition options.
    * 
    * @param allowTransitionOutOfSubprocesses specifies that transitions out of the sub process should be allowed.
    * @param allowTransitionIntoSubprocesses specifies that transitions into sub process should be allowed.
    * @param allowLoops specifies that loops should be allowed.
    */
   public TransitionOptions(
         boolean allowTransitionOutOfSubprocesses,
         boolean allowTransitionIntoSubprocesses,
         boolean allowLoops)
   {
      this(allowTransitionOutOfSubprocesses, allowTransitionIntoSubprocesses, allowLoops, null, null);
   }

   /**
    * Constructs a new instance of transition options.
    * 
    * @param allowTransitionOutOfSubprocesses specifies that transitions out of the sub process should be allowed.
    * @param allowTransitionIntoSubprocesses specifies that transitions into sub process should be allowed.
    * @param allowLoops specifies that loops should be allowed.
    * @param processIdPattern regex expression to match process definition ids.
    * @param activityIdPattern regex expression to match activity ids.
    */
   public TransitionOptions(
         boolean allowTransitionOutOfSubprocesses,
         boolean allowTransitionIntoSubprocesses,
         boolean allowLoops,
         String processIdPattern, String activityIdPattern)
   {
      this.transitionOutOfSubprocessesAllowed = allowTransitionOutOfSubprocesses;
      this.transitionIntoSubprocessesAllowed = allowTransitionIntoSubprocesses;
      this.loopsAllowed = allowLoops;
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

   /**
    * Checks if loops should be allowed while traversing AND joins/splits. If true, then
    * loops detected inside AND blocks are silently ignored, otherwise the traversal of
    * AND blocks is immediately stopped.
    */
   public boolean areLoopsAllowed()
   {
      return loopsAllowed;
   }

   /**
    * Retrieves the regular expression pattern for matching process definition ids.
    */
   public String getProcessIdPattern()
   {
      return processIdPattern;
   }

   /**
    * Retrieves the regular expression pattern for matching activity ids.
    */
   public String getActivityIdPattern()
   {
      return activityIdPattern;
   }
}
