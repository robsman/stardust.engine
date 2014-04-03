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

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;



/**
 * Client side class for decorating the {@link AdministrationService},
 * specific to the quality control feature
 *
 * @author holger.prause
 * @version $Revision: $
 */
public class QualityAssuranceAdminServiceFacade
{
   final AdministrationService as;
   final QueryService qs;

   public QualityAssuranceAdminServiceFacade(ServiceFactory sf)
   {
      this.as = sf.getAdministrationService();
      this.qs = sf.getQueryService();
   }

   /**
    * Set the probability set for all user
    * This value will be taken when new user are created.
    * @param the (default) value for all user - if null , this value
    * will not be saved
    */
   public void setQualityAssuranceUserDefaultProbability(Integer probability)
   {
      setQualityAssuranceProbability(probability, QualityAssuranceUtils.QUALITY_ASSURANCE_USER_DEFAULT_PROBABILITY);
   }

   /**
    * Gets the probability set for all user
    *
    * @return the probability set for all user- if null , this value
    * will not be saved
    */
   public Integer getQualityAssuranceUserDefaultProbability()
   {
      return getQualityAssuranceProbability(QualityAssuranceUtils.QUALITY_ASSURANCE_USER_DEFAULT_PROBABILITY);
   }

   /**
    * Set the probability for a specific participant / department combination.
    * The participant is specified by the activity instance.
    * See {@link Activity#getDefaultPerformer()}
    *
    *
    * @param a - the activity determining the participant, may be null
    * @param department - the department for this participant
    * @param probability - the probability to use - if null , this value
    * will not be saved
    */
   public void setQualityAssuranceParticipantProbability(Activity a, DepartmentInfo department, Integer probability)
   {
      String probabilityKey = getProbabiltyKey(a, department);
      setQualityAssuranceProbability(probability, probabilityKey);
   }

   /**
    * Gets the probability for a specific participant / department combination.
    * The participant is specified by the activity instance.
    * See {@link Activity#getDefaultPerformer()}
    *
    * @param a - the activity determining the participant
    * @param department - the department for this participant, may be null
    * @return the probability set or null otherwise
    */
   public Integer getQualityAssuranceParticipantProbability(Activity a, DepartmentInfo department)
   {
      String probabilityKey = getProbabiltyKey(a, department);
      return getQualityAssuranceProbability(probabilityKey);
   }

   private Integer getQualityAssuranceProbability(String probabilityKey)
   {
      Integer probabilityValue = null;

      Preferences preferences = as.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_QUALITY_CONTROL);
      Map<String, Serializable> preferencesValues = null;
      if(preferences != null)
      {
         preferencesValues = preferences.getPreferences();
         Serializable value = preferencesValues.get(probabilityKey);
         if(value != null)
         {
            if(value instanceof String)
            {
               if(StringUtils.isEmpty((String) value))
               {
                  return probabilityValue;
               }
               else
               {
                  try
                  {
                     return Integer.parseInt((String) value);
                  }
                  catch (NumberFormatException e)
                  {
                     return probabilityValue;
                  }
               }
            }

            probabilityValue = (Integer) value;
         }
      }
      return probabilityValue;
   }

   private void setQualityAssuranceProbability(Integer probability, String probabilityKey)
   {
      Preferences preferences = as.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_QUALITY_CONTROL);
      Map<String, Serializable> preferencesValues = null;
      if(preferences != null)
      {
         preferencesValues = preferences.getPreferences();
      }
      else
      {
         preferencesValues = CollectionUtils.newMap();
      }

      if(probability != null)
      {
         preferencesValues.put(probabilityKey, probability);
      }

      if(preferences == null)
      {
         preferences = new Preferences(PreferenceScope.PARTITION,
               PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
               PreferencesConstants.PREFERENCE_ID_QUALITY_CONTROL,
               preferencesValues);
      }

      as.savePreferences(preferences);
   }

   private String getProbabiltyKey(Activity a, DepartmentInfo department)
   {
      DeployedModel model =  qs.getModel(a.getModelOID(), false);
      return QualityAssuranceUtils.getParticipantProbabiltyKey(model.getId(), a, department);
   }
}