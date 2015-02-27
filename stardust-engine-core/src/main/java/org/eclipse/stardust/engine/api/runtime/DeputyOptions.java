/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Container class for the deputy description options.
 * 
 * @author stephan.born
 * @version $Revision: $
 */
public class DeputyOptions implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * Deputy options with default values: no restriction on participants, no restrictions on dates
    */
   public static final DeputyOptions DEFAULT = new DeputyOptions();

   private Date fromDate;
   private Date toDate;
   private Set<ModelParticipantInfo> participants;

   /**
    * Creates new options valid from now, unlimited and with an empty participant set.
    */
   public DeputyOptions()
   {
      this(TimestampProviderUtils.getTimeStamp(), null);
   }

   /**
    * Creates new options valid in the given interval and with an empty participant set.
    * 
    * @param fromDate the validity start date. Must not be null.
    * @param toDate the validity end date. If null, then it is unlimited.
    * @throws IllegalArgumentException if fromDate is null.
    */
   public DeputyOptions(Date fromDate, Date toDate)
   {
      this(fromDate, toDate, Collections.<ModelParticipantInfo> emptySet());
   }

   /**
    * Creates new options valid in the given interval and with the specified participant set.
    * 
    * @param fromDate the validity start date. Must not be null.
    * @param toDate the validity end date. If null, then it is unlimited.
    * @param participants the set of participants the deputy will have grants to. Can be empty, but not null. 
    * @throws IllegalArgumentException if fromDate or the participants set are null.
    */
   public DeputyOptions(Date fromDate, Date toDate, Set<ModelParticipantInfo> participants)
   {
      super();

      if (fromDate == null)
      {
         new IllegalArgumentException();
      }

      if (participants == null)
      {
         new IllegalArgumentException();
      }

      this.fromDate = fromDate;
      this.toDate = toDate;
      this.participants = participants;
   }

   /**
    * Gets the validity start date.
    * 
    * @return the validity start date.
    */
   public Date getFromDate()
   {
      return fromDate;
   }

   /**
    * Sets the validity start date.
    * 
    * @param fromDate the new validity start date. Must not be null.
    * @throws IllegalArgumentException if fromDate is null.
    */
   public void setFromDate(Date fromDate)
   {
      if (fromDate == null)
      {
         new IllegalArgumentException();
      }

      this.fromDate = fromDate;
   }

   /**
    * Gets the validity end date.
    * 
    * @return the validity end date or null if unlimited.
    */
   public Date getToDate()
   {
      return toDate;
   }

   /**
    * Sets the validity end date.
    * 
    * @param toDate the new validity end date or null if unlimited.
    */
   public void setToDate(Date toDate)
   {
      this.toDate = toDate;
   }

   /**
    * Gets the set of participant grants.
    * 
    * @return the set of participant grants.
    */
   public Set<ModelParticipantInfo> getParticipants()
   {
      return participants;
   }

   /**
    * Sets the participant grants.
    * 
    * @param participants the new set of participant grants. Should be empty if the
    * deputy user only inherit grants over work items in the personal worklist.
    * @throws IllegalArgumentException if the participants set is null.
    */
   public void setParticipants(Set<ModelParticipantInfo> participants)
   {
      if (participants == null)
      {
         new IllegalArgumentException();
      }

      this.participants = participants;
   }
}
