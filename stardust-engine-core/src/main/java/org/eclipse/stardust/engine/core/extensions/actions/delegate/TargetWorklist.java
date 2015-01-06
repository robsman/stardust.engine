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
package org.eclipse.stardust.engine.core.extensions.actions.delegate;

import org.eclipse.stardust.common.StringKey;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TargetWorklist extends StringKey
{
   public static final TargetWorklist DefaultPerformer =
         new TargetWorklist("defaultPerformer", "Default Performer");
   public static final TargetWorklist CurrentUser =
         new TargetWorklist("currentUser", "Current User");
   public static final TargetWorklist RandomUser =
         new TargetWorklist("randomUser", "RandomUser");
   public static final TargetWorklist Participant =
         new TargetWorklist("participant", "Participant");

   public TargetWorklist(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
