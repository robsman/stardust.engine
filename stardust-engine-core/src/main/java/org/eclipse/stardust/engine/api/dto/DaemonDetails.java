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
package org.eclipse.stardust.engine.api.dto;

import java.util.Date;
import java.util.TreeSet;

import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.engine.core.runtime.beans.DaemonExecutionLog;


/**
 * A read only client side view to represent the state of a CARNOT runtime daemon.
 * 
 * @author mgille
 * @version $Revision$
 */
public class DaemonDetails
      implements Daemon
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private String type;
   private long startTime;
   private long lastExecutionTime;
   private boolean running;
   private AcknowledgementState ack;
   private DaemonExecutionState des;
   private DaemonExecutionLog log;

   public DaemonDetails(String type, long startTime, long lastExecutionTime,
         boolean running, AcknowledgementState ack,
         DaemonExecutionState des)
   {
      this(type, startTime, lastExecutionTime, running, ack, des, null);
   }
   
   public DaemonDetails(String type, long startTime, long lastExecutionTime,
         boolean running, AcknowledgementState ack,
         DaemonExecutionState des, DaemonExecutionLog log)
   {
      this.type = type;
      this.startTime = startTime;
      this.lastExecutionTime = lastExecutionTime;
      this.running = running;
      this.ack = ack;
      this.des = des;
      this.log = log;
   }

   public String getType()
   {
      return type;
   }

   public Date getStartTime()
   {
      return startTime <= 0 ? null : new Date(startTime);
   }

   public Date getLastExecutionTime()
   {
      return lastExecutionTime <= 0 ? null: new Date(lastExecutionTime);
   }

   public boolean isRunning()
   {
      return running;
   }

   public AcknowledgementState getAcknowledgementState()
   {
      return ack;
   }

   public DaemonExecutionState getDaemonExecutionState()
   {
      return des;
   }
   public DaemonExecutionLog getExecutionLog()
   {
      return log;

   }
}
