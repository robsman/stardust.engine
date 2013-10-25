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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataType;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IRole;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElementBean;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailParticipantBean;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;


/**
 * @author mgille
 * @version $Revision$
 */
public abstract class ModelParticipantBean extends IdentifiableElementBean
      implements IModelParticipant
{
   /** Organizations this participant is part of. */
   private List organizations = null;

   private List<IRole> allCurrentRoles = null;
   private List<IOrganization> allCurrentOrganizations = null;
   private List<IRole> allPreviousRoles = null;
   private List<IOrganization> allPreviousOrganizations = null;

   private List<String> allNewOrganizations = null;
   private List<String> allRemovedOrganizations = null;

   private String qualifiedId = null;

   public String getQualifiedId()
   {
      if(qualifiedId == null)
      {
         qualifiedId = ModelUtils.getQualifiedId(this);
      }
      return qualifiedId;
   }

   ModelParticipantBean()
   {
   }

   private boolean containsMember(IModelParticipant participant, boolean current)
   {
      if(current)
      {
         if(participant instanceof IOrganization && allCurrentOrganizations != null)
         {
            for(IOrganization organization : allCurrentOrganizations)
            {
               if(organization.getId().equals(participant.getId()))
               {
                  return true;
               }
            }
         }
         if(participant instanceof IRole && allCurrentRoles != null)
         {
            for(IRole role : allCurrentRoles)
            {
               if(role.getId().equals(participant.getId()))
               {
                  return true;
               }
            }
         }
      }
      else
      {
         if(participant instanceof IOrganization && allPreviousOrganizations != null)
         {
            for(IOrganization organization : allPreviousOrganizations)
            {
               if(organization.getId().equals(participant.getId()))
               {
                  return true;
               }
            }
         }
         if(participant instanceof IRole && allPreviousRoles != null)
         {
            for(IRole role : allPreviousRoles)
            {
               if(role.getId().equals(participant.getId()))
               {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean isTreeAdded(IOrganization organization)
   {
      if(containsMember(organization, false))
      {
         return false;
      }

      Iterator allParticipants = organization.getAllParticipants();
      while (allParticipants.hasNext())
      {
         IModelParticipant deployedParticipant = (IModelParticipant) allParticipants.next();
         if(containsMember(deployedParticipant, false))
         {
            return false;
         }
      }

      IRole teamLead = organization.getTeamLead();
      if(teamLead != null && containsMember(teamLead, false))
      {
         return false;
      }

      Iterator allOrganizations = organization.getSubOrganizations();
      while (allOrganizations.hasNext())
      {
         IOrganization deployedOrganization = (IOrganization) allOrganizations.next();
         if(!isTreeAdded(deployedOrganization))
         {
            return false;
         }
      }

      return true;
   }

   private boolean isTreeRemoved(IOrganization organization)
   {
      if(containsMember(organization, true))
      {
         return false;
      }

      Iterator allParticipants = organization.getAllParticipants();
      while (allParticipants.hasNext())
      {

         IModelParticipant deployedParticipant = (IModelParticipant) allParticipants.next();
         if(containsMember(deployedParticipant, true))
         {
            return false;
         }
      }

      IRole teamLead = organization.getTeamLead();
      if(teamLead != null && containsMember(teamLead, true))
      {
         return false;
      }

      Iterator allOrganizations = organization.getSubOrganizations();
      while (allOrganizations.hasNext())
      {
         IOrganization deployedOrganization = (IOrganization) allOrganizations.next();
         if(!isTreeAdded(deployedOrganization))
         {
            return false;
         }
      }

      return true;
   }

   public ModelParticipantBean(String id, String name, String description)
   {
      super(id, name);
      setDescription(description);
   }

   public String toString()
   {
      return "Participant: " + getName();
   }

   public IOrganization findOrganization(String id)
   {
      return (IOrganization) ModelUtils.findById(organizations, id);
   }

   /** */
   public Iterator getAllOrganizations()
   {
      if (organizations == null)
      {
         return Collections.emptyList().iterator();
      }
      return organizations.iterator();
   }

   /**
    * Retrieves an iterator over all top-level organizations, the participant is
    * directly or indirectly part of.
    * A top-level organization is an organization without a superorganization.
    */
   public java.util.Iterator getAllTopLevelOrganizations()
   {
      Iterator iterator = getAllOrganizations();
      List resultSet = CollectionUtils.newList();

      while (iterator.hasNext())
      {
         collectTopLevelOrganizations((IOrganization) iterator.next(), resultSet);
      }
      return resultSet.iterator();
   }

   /**
    *
    */
   private void collectTopLevelOrganizations(IOrganization organization, List resultSet)
   {
      Iterator iterator = organization.getAllOrganizations();

      if (!iterator.hasNext())
      {
         if (!resultSet.contains(organization))
         {
            resultSet.add(organization);

            return;
         }
      }

      // Traverse super organizations

      while (iterator.hasNext())
      {
         collectTopLevelOrganizations((IOrganization) iterator.next(), resultSet);
      }
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies of the participant.
    */
   public void checkConsistency(List inconsistencies)
   {
      try
      {
         super.checkConsistency(inconsistencies);
         checkId(inconsistencies);

         IModel currentModel = (IModel) getModel();
         allCurrentRoles = new ArrayList();
         allCurrentOrganizations = new ArrayList();

         Iterator allRoles = currentModel.getAllRoles();
         while (allRoles.hasNext())
         {
            IRole role = (IRole) allRoles.next();
            allCurrentRoles.add(role);
         }
         Iterator allOrganizations = currentModel.getAllOrganizations();
         while (allOrganizations.hasNext())
         {
            IOrganization organization = (IOrganization) allOrganizations.next();
            allCurrentOrganizations.add(organization);
         }

         // check for unique Id
         IModelParticipant p = ((IModel) getModel()).findParticipant(getId());
         if (p != null && p != this)
         {
            BpmValidationError error = BpmValidationError.PART_DUPLICATE_ID.raise(getName());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }

         if (null != getId())
         {
            // check id to fit in maximum length
            if (getId().length() > AuditTrailParticipantBean.getMaxIdLength())
            {
               BpmValidationError error = BpmValidationError.PART_ID_EXCEEDS_MAXIMUM_LENGTH.raise(
                     getName(), AuditTrailParticipantBean.getMaxIdLength());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }
         }

         // Rule: All associated Organizations must be part of the model
         for (Iterator i = ((IModel) getModel()).getAllOrganizations(); i.hasNext();)
         {
            IOrganization organization = (IOrganization) i.next();

            if (((IModel) getModel()).findParticipant(organization.getId()) == null)
            {
               BpmValidationError error = BpmValidationError.PART_ASSOCIATED_ORGANIZATION_SET_FOR_PARTICIPANT_DOES_NOT_EXIST.raise(
                     organization.getId(), getId());
               inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
            }

            boolean scopedOrg = organization
                  .getBooleanAttribute(PredefinedConstants.BINDING_ATT);
            String dataIdOrg = organization
                  .getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
            String dataPathOrg = organization
            .getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
            if (scopedOrg)
            {
               IData data = ((IModel) getModel()).findData(dataIdOrg);
               if(data == null)
               {
                  BpmValidationError error = BpmValidationError.PART_DATA_FOR_SCOPED_ORGANIZATION_MUST_EXIST.raise(organization.getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               }
               else
               {
                  IDataType dataType = (IDataType) data.getType();
                  boolean isPrimitiveData = PredefinedConstants.PRIMITIVE_DATA.equals(dataType
                        .getId());
                  boolean isStructData = PredefinedConstants.STRUCTURED_DATA.equals(dataType
                        .getId());
                  if ((!isPrimitiveData) && (!isStructData))
                  {
                     BpmValidationError error = BpmValidationError.PART_DATA_OF_SCOPED_ORGANIZATION_CAN_ONLY_BE_PRIM_OR_STRUCT.raise(organization.getId());
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.ERROR));
                  }
                  if (dataIdOrg == null)
                  {
                     BpmValidationError error = BpmValidationError.PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL.raise(organization.getId());
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.ERROR));
                  }
                  if (isStructData && dataPathOrg == null)
                  {
                     BpmValidationError error = BpmValidationError.PART_DATA_OF_SCOPED_ORGANIZATION_MUST_NOT_BE_NULL_WHEN_SDT_IS_USED.raise(organization.getId());
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.ERROR));
                  }
                  else
                  {
                     BridgeObject bridgeData = BridgeObject.getBridge(data, dataPathOrg,
                           Direction.OUT, null);
                     if (!String.class.equals(bridgeData.getEndClass()))
                     {
                        BpmValidationError error = BpmValidationError.PART_TYPE_OF_DATA_OF_SCOPED_ORGANIZATION_IS_NOT.raise(
                              dataIdOrg, organization.getId(), String.class);
                        inconsistencies.add(new Inconsistency(error, this,
                              Inconsistency.ERROR));
                     }
                  }
               }
            }

            if (ModelManagerFactory.isAvailable())
            {
               ModelManager modelManager = ModelManagerFactory.getCurrent();
               // is the order the same order like history?
               for (IModel model : modelManager.getModelsForId(getModel().getId()))
               {
                  allNewOrganizations = new ArrayList();
                  allRemovedOrganizations = new ArrayList();
                  allPreviousRoles = new ArrayList();
                  allPreviousOrganizations = new ArrayList();

                  for(IOrganization currentOrganization : allCurrentOrganizations)
                  {
                     allNewOrganizations.add(currentOrganization.getId());
                  }

                  allRoles = model.getAllRoles();
                  while (allRoles.hasNext())
                  {
                     IRole deployedRole = (IRole) allRoles.next();
                     allPreviousRoles.add(deployedRole);
                  }
                  allOrganizations = model.getAllOrganizations();
                  while (allOrganizations.hasNext())
                  {
                     IOrganization deployedOrganization = (IOrganization) allOrganizations.next();
                     allPreviousOrganizations.add(deployedOrganization);
                     // if not below this ones, it is new, remains in list
                     allNewOrganizations.remove(deployedOrganization.getId());
                     allRemovedOrganizations.add(deployedOrganization.getId());
                  }

                  // if not in list, it is removed
                  for(IOrganization currentOrganization : allCurrentOrganizations)
                  {
                     allRemovedOrganizations.remove(currentOrganization.getId());
                  }

                  allOrganizations = model.getAllOrganizations();
                  while (allOrganizations.hasNext())
                  {
                     IOrganization deployedOrganization = (IOrganization) allOrganizations
                           .next();

                     if(allNewOrganizations.contains(organization.getId()))
                     {
                        // is new
                        if (scopedOrg)
                        {
                           if (!compareParticipantTree(null,
                                 organization, inconsistencies))
                           {
                              break;
                           }
                        }
                     }
                     else if(allRemovedOrganizations.contains(deployedOrganization.getId()))
                     {
                        // is removed
                        boolean scopedDeployedOrg = deployedOrganization
                                       .getBooleanAttribute(PredefinedConstants.BINDING_ATT);
                        if (scopedDeployedOrg)
                        {
                           if (!compareParticipantTree(deployedOrganization,
                                 null, inconsistencies))
                           {
                              break;
                           }
                        }
                     }
                     else if (organization.getId().equals(deployedOrganization.getId()))
                     {
                        boolean scopedDeployedOrg = deployedOrganization
                              .getBooleanAttribute(PredefinedConstants.BINDING_ATT);
                        // Rule: It's not allowed to change from a scoped to an unscoped
                        // organization in model version deployment
                        if (scopedOrg && !scopedDeployedOrg)
                        {
                           BpmValidationError error = BpmValidationError.PART_ORGANIZATION_IS_SCOPED_BUT_IN_AUDITTRAIL_UNSCOPED.raise(organization.getId());
                           inconsistencies.add(new Inconsistency(error, this,
                                 Inconsistency.ERROR));
                        }
                        // Rule: It's not allowed to change from an unscoped to a scoped
                        // organization in model version deployment
                        else if (!scopedOrg && scopedDeployedOrg)
                        {
                           BpmValidationError error = BpmValidationError.PART_ORGANIZATION_IS_UNSCOPED_BUT_IN_AUDITTRAIL_SCOPED.raise(organization.getId());
                           inconsistencies.add(new Inconsistency(error, this,
                                 Inconsistency.ERROR));
                        }
                        else if (scopedOrg && scopedDeployedOrg)
                        {
                           String dataPathDeployedOrg = deployedOrganization
                                 .getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
                           String dataIdDeployedOrg = deployedOrganization
                                 .getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
                           if (!dataIdOrg.equals(dataIdDeployedOrg))
                           {
                              BpmValidationError error = BpmValidationError.PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_ID_IN_AUDIT_TRAIL.raise(
                                    dataIdOrg, organization.getId(), dataIdDeployedOrg);
                              inconsistencies.add(new Inconsistency(error, this,
                                    Inconsistency.ERROR));
                           }
                           else if ((dataPathOrg != null && dataPathDeployedOrg == null)
                                 || (dataPathOrg == null && dataPathDeployedOrg != null)
                                 || ((dataPathOrg != null && dataPathDeployedOrg != null) && (!dataPathOrg
                                       .equals(dataPathDeployedOrg))))
                           {
                              BpmValidationError error = BpmValidationError.PART_TYPE_OF_DATA_ID_OF_SCOPED_ORGANIZATION_IS_DIFFERENT_FROM_DATA_PATH_IN_AUDIT_TRAIL.raise(
                                    dataPathOrg, organization.getId(),
                                    dataPathDeployedOrg);
                              inconsistencies.add(new Inconsistency(error, this,
                                    Inconsistency.ERROR));
                           }

                           if (scopedOrg)
                           {
                              // Rule: The subtree of a scoped organization must not be
                              // changed
                              if (!compareParticipantTree(deployedOrganization,
                                    organization, inconsistencies))
                              {
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
               allOrganizations = this.getAllOrganizations();
               if (allOrganizations.hasNext())
               {
                  allOrganizations.next();
                  if (allOrganizations.hasNext())
                  {
                     BpmValidationError error = BpmValidationError.PART_MULTIPLE_SOPER_ORGANIZATIONS_ARE_NOT_ALLOWED.raise();
                     inconsistencies.add(new Inconsistency(error, this,
                           Inconsistency.ERROR));
                     break;
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, true);
      }
   }

   private boolean compareParticipantTree(IOrganization deployedOrg, IOrganization organization,
         List inconsistencies)
   {
      boolean isValid = true;
      String organizationID = organization != null ? organization.getId() : deployedOrg.getId();

      if(deployedOrg != null && organization != null)
      {
         // deployed one is the previous one
         IRole deployedTeamLead = deployedOrg.getTeamLead();
         IRole teamLead = organization.getTeamLead();
         if ((deployedTeamLead != null && teamLead == null && containsMember(deployedTeamLead, true))
               || (deployedTeamLead == null && teamLead != null && containsMember(teamLead, false))
               || (deployedTeamLead != null && teamLead != null
                     && !CompareHelper.areEqual(deployedTeamLead.getId(), teamLead.getId())))
         {
            BpmValidationError error = BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_MANAGER_OF_ASSOCIATION_THAN_DEPLOYED_MODEL.raise(organization.getId());
            inconsistencies.add(new Inconsistency(error, this,
                  Inconsistency.ERROR));
            isValid = false;
         }
      }

      Iterator deployedParticipantIter = deployedOrg != null ? deployedOrg.getAllParticipants() : null;
      Iterator participantIter = organization != null ? organization.getAllParticipants() : null;
      List<IModelParticipant> participantList = new ArrayList<IModelParticipant>();
      List<IModelParticipant> deployedParticipantList = new ArrayList<IModelParticipant>();
      while (participantIter != null && participantIter.hasNext())
      {
         participantList
               .add(((IModelParticipant) participantIter.next()));
      }
      while (deployedParticipantIter != null && deployedParticipantIter.hasNext())
      {
         deployedParticipantList.add(((IModelParticipant) deployedParticipantIter.next()));
      }

      List checkedParticipants = new ArrayList();
      for (IModelParticipant deployedModelParticipant : deployedParticipantList)
      {
         checkedParticipants.add(deployedModelParticipant);
         boolean containsParticipant = false;
         for (IModelParticipant modelParticipant : participantList)
         {
            if (deployedModelParticipant.getId().equals(modelParticipant.getId())
                  && ((deployedModelParticipant instanceof OrganizationBean && modelParticipant instanceof OrganizationBean)
                        || (deployedModelParticipant instanceof RoleBean && modelParticipant instanceof RoleBean)
                        || (deployedModelParticipant instanceof ConditionalPerformerBean && modelParticipant instanceof ConditionalPerformerBean)))
            {
               containsParticipant = true;
               checkedParticipants.add(modelParticipant);
               if (deployedModelParticipant instanceof IOrganization)
               {
                  compareParticipantTree((IOrganization) deployedOrg
                        .findParticipant(modelParticipant.getId()),
                        (IOrganization) modelParticipant, inconsistencies);
               }
            }
         }
         if(!containsParticipant)
         {
            // here check for removed
            if(deployedModelParticipant instanceof IOrganization)
            {
               if(!isTreeRemoved((IOrganization) deployedModelParticipant))
               {
                  BpmValidationError error = BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL.raise(organization.getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  isValid = false;
               }
            }
            else
            {
               if(containsMember(deployedModelParticipant, true))
               {
                  BpmValidationError error = BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL.raise(organization.getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  isValid = false;
               }
            }
         }
      }

      for (IModelParticipant modelParticipant : participantList)
      {
         if(checkedParticipants.contains(modelParticipant))
         {
            continue;
         }

         boolean containsParticipant = false;
         for (IModelParticipant deployedModelParticipant : deployedParticipantList)
         {
            if (deployedModelParticipant.getId().equals(modelParticipant.getId())
                  && ((deployedModelParticipant instanceof OrganizationBean && modelParticipant instanceof OrganizationBean)
                        || (deployedModelParticipant instanceof RoleBean && modelParticipant instanceof RoleBean)
                        || (deployedModelParticipant instanceof ConditionalPerformerBean && modelParticipant instanceof ConditionalPerformerBean)))
            {
               containsParticipant = true;
               if(checkedParticipants.contains(deployedModelParticipant))
               {
                  continue;
               }
               if (modelParticipant instanceof IOrganization)
               {
                  compareParticipantTree((IOrganization) deployedOrg
                        .findParticipant(modelParticipant.getId()),
                        (IOrganization) modelParticipant, inconsistencies);
               }
            }
         }
         if(!containsParticipant)
         {
            // here check for added
            if(modelParticipant instanceof IOrganization)
            {
               if(!isTreeAdded((IOrganization) modelParticipant))
               {
                  BpmValidationError error = BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL.raise(organization.getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  isValid = false;
               }
            }
            else
            {
               if(containsMember(modelParticipant, false))
               {
                  BpmValidationError error = BpmValidationError.PART_MODEL_CONTAINS_DIFFERENT_ORGANIZATION_TREE_THAN_DEPLOYED_MODEL.raise(organization.getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  isValid = false;
               }
            }
         }
      }

      return isValid;
   }

   public void addToOrganizations(IOrganization org)
   {
      if (organizations == null)
      {
         organizations = CollectionUtils.newList();
      }
      organizations.add(org);
   }
}