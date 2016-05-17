/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;

public class ResumeJanitorCarrier extends HierarchyStateChangeJanitorCarrier
{
   private static final long serialVersionUID = 1L;

   /**
    * Default constructor, needed for creating instances via reflection.
    *
    */
   public ResumeJanitorCarrier()
   {
      super();
   }

   public ResumeJanitorCarrier(long processInstanceOid)
   {
      super(processInstanceOid, 0, Parameters.instance().getInteger(
            ProcessResumeJanitor.PRP_RETRY_COUNT, 10));
   }

   @Override
   public Action doCreateAction()
   {
      return new ProcessResumeJanitor(this);
   }

}
