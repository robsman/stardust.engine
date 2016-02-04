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

package org.eclipse.stardust.engine.core.model.utils;

import java.util.List;

/**
 * @author Barry.Grotjahn
 * @version $Revision: 48953 $
 */
public class IdFactory
{
   private String baseId;
   private String id;
   
   public IdFactory(String baseId)
   {      
      this.baseId = baseId;
   }

   public String getId()
   {
      return id == null ? baseId : id;
   }

   public void computeNames(List<Identifiable> list)
   {
      computeNames(list, true);
   }
   
   public void computeNames(List<Identifiable> list, boolean forceSuffix)
   {
      boolean foundBaseId = false;
      String searchId = baseId + "_";
      int counter = 1;
                  
      for (Identifiable o : list)
      {
         String existingId = (String) o.getId();
         foundBaseId |= ((null != existingId) && existingId.equals(baseId));
         if (existingId != null && existingId.startsWith(searchId))
         {
            try
            {
               String sn = existingId.substring(searchId.length()).trim();
               int number = Integer.parseInt(sn);
               if (number >= counter)
               {
                  counter = number + 1;
               }
            }
            catch (NumberFormatException nfe)
            {
               // ignore
            }
         }            
      }

      if (!foundBaseId && !forceSuffix)
      {
         id = baseId;
      }
      else
      {
         id = searchId + counter;
      }
   }
}