/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.query.statistics.api;

import java.util.Date;

public class AbsoluteDateRange implements DateRange
{

   private static final long serialVersionUID = -1405561697550858554L;

   private Date beginDate;

   private Date endDate;


   public AbsoluteDateRange(Date beginDate, Date endDate)
   {
      super();
      this.beginDate = beginDate;
      this.endDate = endDate;
   }

   @Override
   public Date getIntervalBegin()
   {
      return beginDate;
   }

   @Override
   public Date getIntervalEnd()
   {
      return endDate;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((beginDate == null) ? 0 : beginDate.hashCode());
      result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      AbsoluteDateRange other = (AbsoluteDateRange) obj;
      if (beginDate == null)
      {
         if (other.beginDate != null)
            return false;
      }
      else if ( !beginDate.equals(other.beginDate))
         return false;
      if (endDate == null)
      {
         if (other.endDate != null)
            return false;
      }
      else if ( !endDate.equals(other.endDate))
         return false;
      return true;
   }



}
