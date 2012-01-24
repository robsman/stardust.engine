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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.List;
import java.util.Vector;

import org.eclipse.stardust.engine.core.model.beans.XMLConstants;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TransitionConnection extends Connection
{
   private List points = new Vector();
   private String transition;

   public TransitionConnection(String transition, int sourceId, int targetId)
   {
      super(XMLConstants.TRANSITION_CONNECTION, sourceId, targetId);
      this.transition = transition;
   }

   public void setPoints(List points)
   {
      this.points = points;
   }

   public List getPoints()
   {
      return points;
   }

   public String getTransition()
   {
      return transition;
   }
}

