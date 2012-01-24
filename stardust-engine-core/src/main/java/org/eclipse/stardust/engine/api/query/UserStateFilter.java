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
package org.eclipse.stardust.engine.api.query;


/**
 * Filter that reduces the query result to users with the given status.
 * 
 * @author sborn
 * @version $Revision$
 */
public class UserStateFilter implements FilterCriterion
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private boolean loggedInOnly;
   
   /**
    * Creates a filter matching currently logged in users.
    * 
    * @return The readily configured user state filter.
    */
   public static UserStateFilter forLoggedInUsers()
   {
      return new UserStateFilter(true);
   }

   /**
    * @return true if the filter filters currently logged in users. 
    */
   public boolean isLoggedInOnly()
   {
      return loggedInOnly;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
   
   private UserStateFilter(boolean loggedInOnly)
   {
      super();
      this.loggedInOnly = loggedInOnly;
   }
}
