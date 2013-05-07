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
    * Deputy description options with default values: no restriction on participants, no restrictions on dates
    */
   public static final DeputyOptions DEFAULT = new DeputyOptions();

   private Date fromDate;
   private Date toDate;
   private Set<ModelParticipantInfo> participints;

   public DeputyOptions()
   {
      this(new Date(), null);
   }

   public DeputyOptions(Date fromDate, Date toDate)
   {
      this(fromDate, toDate, Collections.<ModelParticipantInfo> emptySet());
   }

   public DeputyOptions(Date fromDate, Date toDate, Set<ModelParticipantInfo> participints)
   {
      super();

      if (fromDate == null)
      {
         new IllegalArgumentException();
      }

      if (participints == null)
      {
         new IllegalArgumentException();
      }

      this.fromDate = fromDate;
      this.toDate = toDate;
      this.participints = participints;
   }

   public Date getFromDate()
   {
      return fromDate;
   }

   public void setFromDate(Date fromDate)
   {
      this.fromDate = fromDate;
   }

   public Date getToDate()
   {
      return toDate;
   }

   public void setToDate(Date toDate)
   {
      this.toDate = toDate;
   }

   public Set<ModelParticipantInfo> getParticipints()
   {
      return participints;
   }

   public void setParticipints(Set<ModelParticipantInfo> participints)
   {
      if (participints == null)
      {
         new IllegalArgumentException();
      }

      this.participints = participints;
   }
}
