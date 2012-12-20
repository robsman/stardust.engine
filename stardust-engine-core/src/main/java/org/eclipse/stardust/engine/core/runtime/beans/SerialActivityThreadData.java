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
 * <p>
 * Encapsulates the data needed for running a serial activity thread
 * (see {@link SerialActivityThreadWorkerCarrier.SerialActivityThreadRunner}).
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class SerialActivityThreadData implements Serializable
{
   private static final long serialVersionUID = -8152741308944997008L;

   private final long piOID;
   private final long activityOID;
   
   /**
    * <i>
    * Initializes the object with the given data.
    * </i>
    * 
    * @param piOID the process instance OID to initialize the object with
    * @param activityOID the activity OID to initialize the object with
    */
   public SerialActivityThreadData(final long piOID, final long activityOID)
   {
      this.piOID = piOID;
      this.activityOID = activityOID;
   }
   
   /**
    * @return the process instance OID
    */
   public long piOID()
   {
      return piOID;
   }
   
   /**
    * @return the activity OID
    */
   public long activityOID()
   {
      return activityOID;
   }
   
   /**
    * @return an {@link ActivityThreadCarrier} initialized with the data this object encapsulates
    */
   public ActivityThreadCarrier toActivityThreadCarrier()
   {
      final ActivityThreadCarrier carrier = new ActivityThreadCarrier();
      carrier.setProcessInstanceOID(piOID());
      carrier.setActivityOID(activityOID());
      return carrier;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Serial Activity Thread Data - pi: " + piOID + ", activity: " + activityOID;
   }
}
