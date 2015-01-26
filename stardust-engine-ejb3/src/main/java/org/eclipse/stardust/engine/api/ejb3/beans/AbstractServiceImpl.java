/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.EJB;

import org.eclipse.stardust.engine.core.runtime.ejb.AbstractEjb3ServiceBean;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;

public class AbstractServiceImpl extends AbstractEjb3ServiceBean
{
   @EJB
   private ForkingService forkingService;

   public ExecutorService getForkingService()
   {
      return forkingService;
   }
}
