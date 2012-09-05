/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;

/**
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SerialActivityThreadData implements Serializable
{
   private static final long serialVersionUID = -8152741308944997008L;

   private final long piOID;
   private final long activityOID;
   
   public SerialActivityThreadData(final long piOID, final long activityOID)
   {
      this.piOID = piOID;
      this.activityOID = activityOID;
   }
   
   public long piOID()
   {
      return piOID;
   }
   
   public long activityOID()
   {
      return activityOID;
   }
   
   public ActivityThreadCarrier toActivityThreadCarrier()
   {
      final ActivityThreadCarrier carrier = new ActivityThreadCarrier();
      carrier.setProcessInstanceOID(piOID());
      carrier.setActivityOID(activityOID());
      return carrier;
   }
   
   @Override
   public String toString()
   {
      return "Serial Activity Thread Data - pi: " + piOID + ", activity: " + activityOID;
   }
}
