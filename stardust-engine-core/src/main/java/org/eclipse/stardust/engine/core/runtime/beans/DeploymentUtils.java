/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.api.dto.DeploymentInfoDetails;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.IFormalParameter;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IModeler;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.utils.RootElement;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeploymentUtils
{
   private static final String IN_USE = "Deleted {0} is used by {1} [oid: {2}].";
   private static final String PRIMARY_IMPLEMENTATION = "Deleted {0} is primary implementation for {1} [model oid: {2}].";
   private static final String DANGLING_AUDIT_TRAIL_RECORDS = "Deleting the model element {0} will result in {1} dangling audit trail record(s).";
   private static final String INCOMPATIBLE_MODEL_ELEMENTS = "Incompatible model elements '{'{0}}{1}[oid:{2}] <--> '{'{3}}{4}[oid:{5}].";

   public static List checkModelVersion(IModel iModel)
   {
      List inconsistencies = new ArrayList();
      Version version = iModel.getCarnotVersion();
      if (version == null)
      {
         inconsistencies.add(new Inconsistency(
               "The model was created with an unknown CARNOT version.", iModel, Inconsistency.WARNING));
      }
      else if (version.compareTo(Version.createFixedVersion(3, 0, 0)) < 0 )
      {
         inconsistencies.add(new Inconsistency(
               "The model was created with CARNOT version " + version, iModel, Inconsistency.WARNING));
      }
      return inconsistencies;
   }

   public static void attachDeploymentAttributes(DeploymentInfoDetails info, IModel model)
   {
      info.setModelOID(model.getModelOID());
      info.setDeploymentTime((Date) model.getAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT));
      info.setSuccess(true);
      info.setRevision(model.getIntegerAttribute(PredefinedConstants.REVISION_ATT));
   }

   public static void replicateRoles(IModel newModel, IModel oldModel)
   {
      ModelElementList participants = oldModel.getParticipants();
      for (int i = 0, len = participants.size(); i < len; i++)
      {
         IModelParticipant participant = (IModelParticipant) participants.get(i);
         if (!(participant instanceof IModeler))
         {
            String id = participant.getId();
            IModelParticipant newParticipant = newModel.findParticipant(id);
            if (newParticipant != null)
            {
               ModelManagerBean.trace.debug("Replicating participant " + id
                     + " with OID " + participant.getOID());
               Iterator<UserParticipantLink> links = UserParticipantLink.findAllFor(participant);
               while (links.hasNext())
               {
                  UserParticipantLink userParticipantLink = links.next();
                  IUser user = userParticipantLink.getUser();
                  if (user != null)
                  {
                     user.addToParticipants(newParticipant, userParticipantLink.getDepartment());
                  }
               }
            }
         }
      }
   }

   public static boolean validateDanglingRuntimeItems(List<Inconsistency> problems, ModelElement deletedElement)
   {
      boolean valid = true;
      if (deletedElement instanceof IProcessDefinition)
      {
         valid &= checkUsed(problems, (IProcessDefinition) deletedElement);
         valid &= validateDanglingRuntimeItems(problems, deletedElement, ProcessInstanceBean.class,
               ProcessInstanceBean.FR__PROCESS_DEFINITION, ProcessInstanceBean.FR__MODEL);
      }
      else if (deletedElement instanceof IActivity)
      {
         valid &= validateDanglingRuntimeItems(problems, deletedElement, ActivityInstanceBean.class,
               ActivityInstanceBean.FR__ACTIVITY, ActivityInstanceBean.FR__MODEL);
      }
      else if (deletedElement instanceof IModelParticipant)
      {
         valid &= validateDanglingRuntimeItems(problems, deletedElement, ActivityInstanceLogBean.class,
               ActivityInstanceLogBean.FR__PARTICIPANT, ActivityInstanceLogBean.FR__MODEL);
         /*valid &= validateDanglingRuntimeItems(problems, deletedElement, UserParticipantLink.class,
               UserParticipantLink.FR__PARTICIPANT, UserParticipantLink.FR__MODEL);*/
      }
      else if (deletedElement instanceof IData)
      {
         valid &= validateDanglingRuntimeItems(problems, deletedElement, DataValueBean.class,
               DataValueBean.FR__DATA, DataValueBean.FR__MODEL);
      }
      else if (deletedElement instanceof ITransition)
      {
         valid &= validateDanglingRuntimeItems(problems, deletedElement, TransitionInstanceBean.class,
               TransitionInstanceBean.FR__TRANSITION, TransitionInstanceBean.FR__MODEL);
         valid &= validateDanglingRuntimeItems(problems, deletedElement, TransitionTokenBean.class,
               TransitionTokenBean.FR__TRANSITION, TransitionTokenBean.FR__MODEL);
      }
      return valid;
   }

   private static boolean checkUsed(List<Inconsistency> problems, IProcessDefinition deletedElement)
   {
      if (deletedElement.getDeclaresInterface())
      {
         List<IdentifiableElement> using = ModelUtils.findUsing(deletedElement);
         if (!using.isEmpty())
         {
            RootElement model = using.get(0).getModel();
            problems.add(new Inconsistency(Inconsistency.ERROR, deletedElement, IN_USE,
                  deletedElement, model, model.getModelOID()));
            return false;
         }
      }
      else
      {
         IReference ref = deletedElement.getExternalReference();
         if (ref != null)
         {
            IModel model = ref.getExternalPackage().getReferencedModel();
            if (deletedElement == ModelRefBean.getPrimaryImplementation(model.findProcessDefinition(ref.getId()), null, null))
            {
               problems.add(new Inconsistency(Inconsistency.ERROR, deletedElement, PRIMARY_IMPLEMENTATION,
                     deletedElement, "{" + model.getId() + "}" + ref.getId(), model.getModelOID()));
               return false;
            }
         }
      }
      return true;
   }

   /**
    * 
    * @param problems
    * @param deletedElement
    * @param runtimeItemClass
    * @param rtOidField
    * @param modelOidField
    * @return true if there are no dangling runtime items.
    */
   public static boolean validateDanglingRuntimeItems(List<Inconsistency> problems, ModelElement deletedElement,
         Class runtimeItemClass, FieldRef rtOidField, FieldRef modelOidField)
   {
      // TODO review callers
      final long nItems = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getCount(
            runtimeItemClass,
            QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(rtOidField, ModelManagerFactory.getCurrent()
                        .getRuntimeOid((IdentifiableElement) deletedElement)),
                  Predicates.isEqual(modelOidField,
                        deletedElement.getModel().getModelOID()))),
                  true);

      if (nItems > 0)
      {
         problems.add(new Inconsistency(Inconsistency.WARNING, deletedElement,
               DANGLING_AUDIT_TRAIL_RECORDS, deletedElement, nItems));
         return false;
      }
      return true;
   }

   /**
    * 
    * @param inconsistencies
    * @param oldElement
    * @param newElement
    * @return true is old and new elements belong to the same class.
    */
   public static boolean checkSameClass(List inconsistencies, ModelElement oldElement,
         ModelElement newElement)
   {
      if (oldElement.getClass() != newElement.getClass())
      {
         inconsistencies.add(new Inconsistency(Inconsistency.WARNING, newElement,
               INCOMPATIBLE_MODEL_ELEMENTS, oldElement, newElement));
         return false;
      }
      return true;
   }

   /**
    * 
    * @param inconsistencies
    * @param oldElement
    * @param newElement
    * @return true if oldElement do not declare an interface or both declare compatible interfaces.
    */
   public static boolean checkCompatibleInterface(List inconsistencies,
         IProcessDefinition oldElement, IProcessDefinition newElement, boolean isImplementation)
   {
      if (oldElement.getDeclaresInterface())
      {
         if (!isImplementation && !newElement.getDeclaresInterface())
         {
            return setIncompatible(inconsistencies, oldElement, newElement);
         }

         if (oldElement.getFormalParameters().size() != newElement.getFormalParameters().size())
         {
            return setIncompatible(inconsistencies, oldElement, newElement);
         }

         Map<String, Direction> usedParams = getUsedFormalParameterIds(oldElement);
         for (IFormalParameter oldParam : oldElement.getFormalParameters())
         {
            String paramId = oldParam.getId();
            // ignore changes on unused parameters
            if (usedParams.containsKey(paramId))
            {
               IFormalParameter newParam = newElement.findFormalParameter(paramId);
               // check if deleted
               if (newParam == null)
               {
                  return setIncompatible(inconsistencies, oldElement, newElement);
               }
               // check if incompatible direction change
               Direction usedDirection = usedParams.get(paramId);
               if (!newParam.getDirection().isCompatibleWith(usedDirection))
               {
                  return setIncompatible(inconsistencies, oldElement, newElement);
               }
               // check if type change
               IData oldData = oldParam.getData();
               if (oldData != null)
               {
                  IData newData = newParam.getData();
                  if (newData == null || !CompareHelper.areEqual(oldData.getType().getId(), newData.getType().getId()))
                  {
                     return setIncompatible(inconsistencies, oldElement, newElement);
                  }
               }
            }
         }
      }
      return true;
   }

   private static Map<String, Direction> getUsedFormalParameterIds(IProcessDefinition oldElement)
   {
      Map<String, Direction> usedParams = CollectionUtils.newMap();
      for (IdentifiableElement identifiable : ModelUtils.findUsing(oldElement))
      {
         if (identifiable instanceof IActivity)
         {
            for (IDataMapping mapping : ((IActivity) identifiable).getDataMappings())
            {
               if (PredefinedConstants.PROCESSINTERFACE_CONTEXT.equals(mapping.getId()))
               {
                  String id = mapping.getActivityAccessPointId();
                  Direction direction = mapping.getDirection();
                  Direction existing = usedParams.get(id);
                  if (Direction.IN_OUT != existing && direction != existing)
                  {
                     usedParams.put(id, existing == null ? direction : Direction.IN_OUT);
                  }
               }
            }
         }
         if (identifiable instanceof IProcessDefinition)
         {
            for (IFormalParameter param : oldElement.getFormalParameters())
            {
               usedParams.put(param.getId(), param.getDirection());
            }
         }
      }
      return usedParams;
   }

   private static boolean setIncompatible(List inconsistencies,
         IProcessDefinition oldElement, IProcessDefinition newElement)
   {
      IModel oldModel = (IModel) oldElement.getModel();
      IModel newModel = (IModel) newElement.getModel();
      inconsistencies.add(new Inconsistency(Inconsistency.ERROR, newElement,
            INCOMPATIBLE_MODEL_ELEMENTS, oldModel.getId(), oldElement.getId(), oldModel.getModelOID(),
            newModel.getId(), newElement.getId(), newModel.getModelOID()));
      return false;
   }
}
