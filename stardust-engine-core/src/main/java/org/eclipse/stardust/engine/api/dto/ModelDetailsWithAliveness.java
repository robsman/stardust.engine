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

import java.util.*;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.QueryUtils;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ModelDetailsWithAliveness extends ModelDetails
{
   private static final long serialVersionUID = 1L;

   public ModelDetailsWithAliveness(ModelDetails template, boolean alive)
   {
      super(template, alive);
   }

   public ModelDetailsWithAliveness(List<ModelDetails> details, boolean alive)
   {
      super(details.get(0), alive);
      List contentList = null;
      Map contentMap = null;
      
      for (int i = 1, len = details.size(); i < len; i++)
      {
         ModelDetails detail = details.get(i);
         
         contentList = new ArrayList();
         contentList.addAll(detail.roles);
         filter(contentList);
         roles.addAll(contentList);
         contentList = new ArrayList();
         contentList.addAll(detail.topLevelRoles);
         filter(contentList);
         topLevelRoles.addAll(contentList);
         contentList = new ArrayList();
         contentList.addAll(detail.data);
         filter(contentList);
         data.addAll(contentList);
                  
         processes.addAll(detail.processes);
         organizations.addAll(detail.organizations);
         topLevelOrganizations.addAll(detail.topLevelOrganizations);                  
         typeDeclarations.addAll(detail.typeDeclarations);

         contentMap = new HashMap();
         contentMap.putAll(detail.indexedRoles);
         filter(contentMap);
         indexedRoles.putAll(contentMap);
         contentMap = new HashMap();
         contentMap.putAll(detail.indexedData);
         filter(contentMap);
         indexedData.putAll(contentMap);
         
         indexedPDs.putAll(detail.indexedPDs);
         indexedOrgs.putAll(detail.indexedOrgs);
         indexedTypeDecls.putAll(detail.indexedTypeDecls);
      }
   }
   
   private void filter(List content)
   {
      for(Iterator it = content.iterator(); it.hasNext();)
      {
         ModelElement element = (ModelElement) it.next();
         if((PredefinedConstants.META_DATA_IDS.contains(element.getId()) && element instanceof Data)
               || (QueryUtils.isPredefinedParticipant(element.getId())) && element instanceof Role)
         {
            it.remove();            
         }
      }
      
   }   
   
   private void filter(Map content)
   {
      Iterator it = content.entrySet().iterator(); 
      while (it.hasNext()) 
      {
         Map.Entry entry = (Map.Entry) it.next();
         ModelElement element = (ModelElement) entry.getValue();
         if((PredefinedConstants.META_DATA_IDS.contains(element.getId()) && element instanceof Data)
               || (QueryUtils.isPredefinedParticipant(element.getId())) && element instanceof Role)
         {
            it.remove();            
         }
      }
   }   
}