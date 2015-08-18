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

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AnyProcessVisitor implements IModelVisitor, IProcessVisitor
{

   public boolean visitModel(IModel model)
   {
      ModelElementList mel = model.getProcessDefinitions();
      for (int i = 0; i<mel.size(); i++)
      {
         IProcessDefinition process = (IProcessDefinition) mel.get(i);

         if(!visitProcess(process))
         {
            return false;
         }
      }
      return true;
   }

}
