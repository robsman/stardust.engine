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
package org.eclipse.stardust.engine.core.query.statistics.utils;

import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AnyActivityVisitor extends AnyProcessVisitor
      implements IActivityVisitor
{

   public boolean visitProcess(IProcessDefinition process)
   {
      ModelElementList mel = process.getActivities();
      for (int i=0; i<mel.size(); i++)
      {
         IActivity activity = (IActivity) mel.get(i);

         if(!visitActivity(activity))
         {
            return false;
         }
      }
      return true;
   }

}
