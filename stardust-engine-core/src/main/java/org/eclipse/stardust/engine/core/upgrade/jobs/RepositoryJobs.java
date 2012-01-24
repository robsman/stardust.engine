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
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.stardust.engine.core.upgrade.jobs.m30.X3_0_0from2_5_0Converter;


/**
 * @author fherinean
 * @version $Revision$
 */
public class RepositoryJobs
{
   public static List getRepositoryJobs()
   {
      ArrayList jobs = new ArrayList(1);
      jobs.add(new X3_0_0from2_5_0Converter());
      return jobs;
   }
}
