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

import java.io.ObjectStreamException;

/**
 * Filter for finding processes started by a specific user.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see #CURRENT_USER
 */
public class StartingUserFilter implements FilterCriterion
{
   private static final int CUSTOM_USER_FILTER_TAG = 0;
   private static final int CURRENT_USER_FILTER_TAG = 1;

   /**
    * Special filter for finding process started by the currently logged in user.
    */
   public static final StartingUserFilter CURRENT_USER =
         new StartingUserFilter(CURRENT_USER_FILTER_TAG, -1);

   /**
    * Internal tag to detect singletons.
    */
   private final int filterKind;

   /**
    * The OID of the workflow user this filter represents.
    */
   private final long userOID;

   public StartingUserFilter(long userOID)
   {
      this(CUSTOM_USER_FILTER_TAG, userOID);
   }

   private StartingUserFilter(int filterKind, long userOID)
   {
      this.filterKind = filterKind;

      this.userOID = userOID;
   }

   public long getUserOID()
   {
      return userOID;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }

   protected Object readResolve() throws ObjectStreamException
   {
      Object replacement = this;
      switch (this.filterKind)
      {
         case CURRENT_USER_FILTER_TAG:
            replacement = CURRENT_USER;
            break;
         default:
            // don't replace
      }
      return replacement;
   }
}
