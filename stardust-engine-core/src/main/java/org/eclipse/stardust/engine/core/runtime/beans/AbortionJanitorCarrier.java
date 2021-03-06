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

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;

/**
 *
 * @author stephan.born, roland.stamm
 */
public class AbortionJanitorCarrier extends HierarchyStateChangeJanitorCarrier
{
   private static final long serialVersionUID = 2L;

   /**
    * Default constructor, needed for creating instances via reflection.
    *
    */
   public AbortionJanitorCarrier()
   {
      super();
   }

   public AbortionJanitorCarrier(long processInstanceOid, long abortingUserOid)
   {
      super(processInstanceOid,  abortingUserOid, Parameters.instance().getInteger(
            ProcessAbortionJanitor.PRP_RETRY_COUNT, 10));
   }

   public Action doCreateAction()
   {
      return new ProcessAbortionJanitor(this);
   }

   public String toString()
   {
      return "Process instance abortion janitor carrier: pi = " + processInstanceOid
            + ".";
   }
}
