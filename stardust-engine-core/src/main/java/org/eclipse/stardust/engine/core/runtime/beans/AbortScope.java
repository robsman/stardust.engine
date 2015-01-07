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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.StringKey;

/**
 * Represents the abortion scope.
 *
 * @author sborn
 * @version $Revision: $
 */
public class AbortScope extends StringKey
{
   private static final long serialVersionUID = 1L;

   public static final String ROOT_HIERARCHY = "RootHierarchy";
   public static final String SUB_HIERARCHY = "SubHierarchy";

   /**
    * The process abort is performed starting from the root process instance. All activities
    * in the hierarchy of the process instance which are not yet being terminated will be aborted. 
    * The process instance state after the process abort is <code>Aborted</code>.
    */
   public static final AbortScope RootHierarchy = new AbortScope(ROOT_HIERARCHY,
         "RootHierarchy");
   
   /**
    * The process abort is performed starting from the sub process. If the activity instance is not a
    * sub process then the process continues in a way as if the activity is completed. This 
    * means for example that the process has the state <code>Completed</code> if the last
    * activity of the process will be aborted. In the case that the activity is a sub process
    * then the sub process hierarchy and the sub process themselves will be aborted but the  
    * process continues as usual.
    * <p>The activity instance state which was given for the abort process is set to 
    * <code>Aborted</code> in all cases.</p>
    */
   public static final AbortScope SubHierarchy = new AbortScope(SUB_HIERARCHY,
         "SubHierarchy");
   
   protected Object readResolve()
   {
      return super.readResolve();
   }

   private AbortScope(String id, String name)
   {
      super(id, name);
   }
}
