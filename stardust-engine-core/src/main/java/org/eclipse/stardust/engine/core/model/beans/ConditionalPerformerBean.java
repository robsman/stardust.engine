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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IConditionalPerformer;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.ParticipantType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.model.utils.SingleRef;
import org.eclipse.stardust.engine.core.runtime.beans.IDataValue;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.ExtendedDataValidator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;


/**
 * @author mgille
 * @version $Revision$
 */
public class ConditionalPerformerBean extends ModelParticipantBean
      implements IConditionalPerformer
{
   private static final Logger trace = LogManager.getLogger(ConditionalPerformerBean.class);

   protected static final String NAME_SPACE = "ConditionalPerformer::";

   private boolean user;

   private SingleRef data = new SingleRef(this, "Associated Data");
   private String dereferencePath;

   ConditionalPerformerBean()
   {
   }

   public ConditionalPerformerBean(String id, String name, String description, IData data)
   {
      super(id, name, description);

      setData(data);
   }

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies
    */
   public void checkConsistency(List inconsistencies)
   {
      Assert.isNotNull(getModel(), "Reference to model is not null");

      try
      {
         // check inherit consistency rules
         super.checkConsistency(inconsistencies);

         // Rule: associated workflow data must exist
         if (null == getData())
         {
            BpmValidationError error = BpmValidationError.PART_NO_DATA_ASSOCIATED_TO_CONDITIONAL_PERFORMER.raise(getId());
            inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
         }
         else
         {
            ExtendedDataValidator validator = (ExtendedDataValidator) ValidatorUtils.getValidator(
                  getData().getType(), this, inconsistencies);

            if (null != validator)
            {
               BridgeObject rhs = validator.getBridgeObject(getData(),
                     getDereferencePath(), Direction.OUT, null);

               if ( !String.class.isAssignableFrom(rhs.getEndClass())
                     && !Long.class.isAssignableFrom(rhs.getEndClass())
                     && !long.class.isAssignableFrom(rhs.getEndClass()))
               {
                  BpmValidationError error = BpmValidationError.PART_DATA_EXPRESSION_OF_UNSUPPORTED_TYPE.raise(getId());
                  inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
               }
            }

            if (null != getRealmData())
            {
               validator = (ExtendedDataValidator) ValidatorUtils.getValidator(getRealmData()
                     .getType(), this, inconsistencies);

               if (null != validator)
               {
                  BridgeObject rhs = validator.getBridgeObject(getRealmData(),
                        getRealmDereferencePath(), Direction.OUT, null);

                  if ( !String.class.isAssignableFrom(rhs.getEndClass())
                        && !Long.class.isAssignableFrom(rhs.getEndClass())
                        && !long.class.isAssignableFrom(rhs.getEndClass()))
                  {
                     BpmValidationError error = BpmValidationError.PART_DATA_REALM_EXPRESSION_OF_UNSUPPORTED_TYPE.raise(getId());
                     inconsistencies.add(new Inconsistency(error, this, Inconsistency.ERROR));
                  }
               }
            }
         }

      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new PublicException("Exception during the consistency check of " +
               "conditional performer '" + getId() + "'\n" + e.getMessage());
      }
   }

   public Iterator getAllParticipants()
   {
      return Collections.EMPTY_LIST.iterator();
   }

   public int getCardinality()
   {
      return Unknown.INT;
   }

   public String toString()
   {
      return "Conditional Performer: " + getName();
   }

   public boolean isUser()
   {
      return user;
   }

   public void setUser(boolean user)
   {
      markModified();

      this.user = user;
   }

   public ParticipantType getPerformerKind()
   {
      final String performerKind = (String) getAttribute(PredefinedConstants.CONDITIONAL_PERFORMER_KIND);

      ParticipantType kind;
      if (!StringUtils.isEmpty(performerKind))
      {
         if (PredefinedConstants.CONDITIONAL_PERFORMER_KIND_USER.equals(performerKind))
         {
            kind = ParticipantType.User;
         }
         else if (PredefinedConstants.CONDITIONAL_PERFORMER_KIND_USER_GROUP
               .equals(performerKind))
         {
            kind = ParticipantType.UserGroup;
         }
         else if (PredefinedConstants.CONDITIONAL_PERFORMER_KIND_MODEL_PARTICIPANT
               .equals(performerKind))
         {
            kind = ParticipantType.ModelParticipant;
         }
         else if (PredefinedConstants.CONDITIONAL_PERFORMER_KIND_MODEL_PARTICIPANT_OR_USER_GROUP
               .equals(performerKind))
         {
            kind = ParticipantType.ModelParticipantOrUserGroup;
         }
         else
         {
            throw new PublicException("Unsupported conditional performer kind: "
                  + performerKind);
         }
      }
      else
      {
         if (true == isUser())
         {
            kind = ParticipantType.User;
         }
         else
         {
            kind = ParticipantType.ModelParticipant;
         }
      }

      return kind;
   }

   public IData getData()
   {
      return (IData) data.getElement();
   }

   public final void setData(IData data)
   {
      markModified();

      this.data.setElement(data);
      setDereferencePath(null);
   }

   public String getDereferencePath()
   {
      return dereferencePath;
   }

   public void setDereferencePath(String dereferencePath)
   {
      markModified();

      this.dereferencePath = dereferencePath;
   }

   public boolean isAuthorized(IModelParticipant participant)
   {
      return true;
   }

   public boolean isAuthorized(IUser user)
   {
      return true;
   }

   public boolean isAuthorized(IUserGroup userGroup)
   {
      return true;
   }

   // TODO (sb): refactor this method outside the model package
   public IParticipant retrievePerformer(IProcessInstance processInstance)
         throws PublicException
   {
      Object performerHandle = retrievePerformerHandle(processInstance);

      IParticipant performer = null;

      try
      {
         final ParticipantType performerKind = getPerformerKind();

         if (ParticipantType.User.equals(performerKind))
         {
            performer = retrieveUser(performerHandle);
         }
         else if (ParticipantType.ModelParticipant.equals(performerKind))
         {
            performer = retrieveModelParticipant(performerHandle, processInstance);
         }
         else if (ParticipantType.UserGroup.equals(performerKind))
         {
            performer = retrieveUserGroup(performerHandle);
         }
         else if (ParticipantType.ModelParticipantOrUserGroup.equals(performerKind))
         {
            performer = retrieveModelParticipant(performerHandle, processInstance);

            if (null == performer)
            {
               performer = retrieveUserGroup(performerHandle);
            }
         }
      }
      catch (InvalidHandleTypeException e)
      {
         throw new PublicException("Failed resolving conditional performer identity.", e);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Conditional performer was resolved as " + performer + ".");
      }

      if (null == performer)
      {
         throw new PublicException("Cannot retrieve conditional participant "
               + "performer for handle '" + performerHandle + "'.");
      }

      return performer;
   }

   private IData getRealmData()
   {
      String realmStringId = (String) getAttribute(PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA);
      if ( !StringUtils.isEmpty(realmStringId))
      {
         IModel model = (IModel) getModel();
         return model.findData(realmStringId);
      }
      else
      {
         return null;
      }
   }

   private String getRealmDereferencePath()
   {
      return (String) getAttribute(PredefinedConstants.CONDITIONAL_PERFORMER_REALM_DATA_PATH);
   }

   // @todo (france, ub): refactor this method outside the model package
   /**
    * Retrieves the user of a conditional performer if
    * <code>getPerfomerKind()</code> returns <code>PERFORMER_KIND_USER</code>
    */
   private IUser retrieveUser(Object performerHandle) throws InvalidHandleTypeException
   {
      IUser userPerformer;

      if (performerHandle instanceof Long)
      {
         userPerformer = retrieveUserByOid(((Long)performerHandle).longValue());
      }
      else if (performerHandle instanceof String)
      {
         String account = (String) performerHandle;
         userPerformer = retrieveUserByAccount(account, Collections.EMPTY_MAP);
      }
      else if (performerHandle instanceof Pair)
      {
         Pair realmQualifiedUser = (Pair) performerHandle;
         userPerformer = retrieveUserByRealmQualification(realmQualifiedUser);
      }
      else
      {
         throw new InvalidHandleTypeException(
               "Invalid conditional participant performer " + "identifier: '"
                     + performerHandle + "'.");
      }

      return userPerformer;
   }

   private IUser retrieveUserByRealmQualification(Pair realmQualifiedUser)
   {
      IUser userPerformer;
      final Object userHandle = realmQualifiedUser.getFirst();
      if (userHandle instanceof Long)
      {
         Long userOid = (Long) userHandle;
         userPerformer = retrieveUserByOid((userOid).longValue());
         if (null != userPerformer)
         {
            final Object realmHandle = realmQualifiedUser.getSecond();
            if (realmHandle instanceof Long)
            {
               Long realmOid = (Long) realmHandle;
               if (realmOid.longValue() != userPerformer.getRealm().getOID())
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug(MessageFormat.format(
                           "Invalid conditional user performer {0}: "
                                 + "Provided user realm oid ''{1}'' does not match",
                           new Object[] { userPerformer, realmOid }));
                  }
                  userPerformer = null;
               }
            }
            else if (realmHandle instanceof String)
            {
               String realmId = (String) realmHandle;
               if ( !realmId.equals(userPerformer.getRealm().getId()))
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug(MessageFormat.format(
                           "Invalid conditional user performer {0}: "
                                 + "Provided user realm id ''{1}'' does not match",
                           new Object[] { userPerformer, realmId }));
                  }
                  userPerformer = null;
               }
            }
            else
            {
               throw new InvalidHandleTypeException(MessageFormat.format(
                     "Invalid user performer realm identifier: ''{0}''.",
                     new Object[] { realmHandle }));
            }
         }
      }
      else if (userHandle instanceof String)
      {
         final String account = (String) userHandle;
         final Map properties = new HashMap();

         final Object realmHandle = realmQualifiedUser.getSecond();
         if (realmHandle instanceof Long)
         {
            Long realmOid = (Long) realmHandle;
            try
            {
               UserRealmBean realm = UserRealmBean.findByOID(realmOid.longValue());
               properties.put(SecurityProperties.REALM, realm.getId());
            }
            catch (ObjectNotFoundException e)
            {
               trace.debug("Invalid conditional user performer", e);
               userPerformer = null;
            }
         }
         else if (realmHandle instanceof String)
         {
            String realmId = (String) realmHandle;
            properties.put(SecurityProperties.REALM, realmId);
         }
         else
         {
            throw new InvalidHandleTypeException(MessageFormat.format(
                  "Invalid user performer realm identifier: ''{0}''.",
                  new Object[] { realmHandle }));
         }

         userPerformer = retrieveUserByAccount(account, properties);
      }
      else
      {
         throw new InvalidHandleTypeException(MessageFormat.format(
               "Invalid conditional participant performer identifier: ''{0}''.",
               new Object[] { userHandle }));
      }

      return userPerformer;
   }

   private IUser retrieveUserByOid(long userOid)
   {
      IUser userPerformer;
      try
      {
         userPerformer = UserBean.findByOID(userOid);
      }
      catch (ObjectNotFoundException e)
      {
         trace.debug("Invalid conditional user performer", e);
         userPerformer = null;
      }

      return userPerformer;
   }

   private IUser retrieveUserByAccount(String account, Map properties)
   {
      IUser userPerformer;
      try
      {
         if (null == properties)
         {
            properties = Collections.EMPTY_MAP;
         }

         userPerformer = SynchronizationService.synchronize(account,
               (IModel) getModel(), Parameters.instance().getBoolean(
                        SecurityProperties.AUTHORIZATION_SYNC_CONDITIONAL_PERFORMER_PROPERTY,
                        true), properties);
      }
      catch (ObjectNotFoundException e)
      {
         trace.debug("Invalid conditional user performer", e);
         userPerformer = null;
      }

      return userPerformer;
   }

   // TODO (sb): refactor this method outside the model package
   /**
    * Retrieves the user group of a conditional performer if
    * <code>getPerfomerKind()</code> returns <code>PERFORMER_KIND_USER_GROUP</code>
    */
   private IUserGroup retrieveUserGroup(Object performerHandle)
         throws InvalidHandleTypeException
   {
      IUserGroup userGroupPerformer;

      if (performerHandle instanceof Long)
      {
         try
         {
            userGroupPerformer = UserGroupBean.findByOid(((Long) performerHandle)
                  .longValue());
         }
         catch (ObjectNotFoundException e)
         {
            trace.debug("Invalid conditional user group performer", e);
            userGroupPerformer = null;
         }
      }
      else if (performerHandle instanceof String)
      {
         String id = (String) performerHandle;
         try
         {
            userGroupPerformer = SynchronizationService.synchronizeUserGroup(id);
         }
         catch (ObjectNotFoundException e)
         {
            trace.debug("Invalid conditional user group performer", e);
            userGroupPerformer = null;
         }
      }
      else
      {
         throw new InvalidHandleTypeException(
               "Invalid conditional participant performer " + "identifier: '"
                     + performerHandle + "'.");
      }

      return userGroupPerformer;
   }

   // @todo (france, ub): refactor this method outside the model package
   /**
    * Retrieves the participant of a conditional performer if
    * <code>getPerfomerKind()</code> returns <code>PERFORMER_KIND_MODEL_PARTICIPANT</code>
    */
   private IModelParticipant retrieveModelParticipant(Object performerHandle,
         IProcessInstance processInstance)
   {
      IModelParticipant performer;

      if (performerHandle instanceof Long)
      {
         long oid = ((Long) performerHandle).longValue();
         // TODO ensure backward compatibility with ald data format
         performer = ModelManagerFactory.getCurrent().findModelParticipant(
               getModel().getModelOID(), oid);
         if (null == performer)
         {
            performer = (IModelParticipant) ModelManagerFactory.getCurrent()
                  .lookupObjectByOID(oid);
         }
      }
      else if (performerHandle instanceof String)
      {
         String id = (String) performerHandle;

         QName idRef = QName.valueOf(id);

         if (idRef.getNamespaceURI().isEmpty())
         {
            performer = ((IModel) processInstance.getProcessDefinition().getModel()).findParticipant(id);
         }
         else
         {
            IModel model = ModelManagerFactory.getCurrent().findActiveModel(
                  idRef.getNamespaceURI());

            performer = model.findParticipant(idRef.getLocalPart());

         }

      }
      else
      {
         throw new InvalidHandleTypeException(
               "Invalid conditional participant performer " + "identifier: '"
                     + performerHandle + "'.");
      }

      return performer;
   }

   private Object retrievePerformerHandle(IProcessInstance processInstance)
   {
      // @todo Should be checked in consistency check

      final IData data = getData();

      if (null == data)
      {
         throw new InternalException("No data set for conditional performer '" + getId()
               + "'.");
      }

      Object performerHandle = evaluateData(processInstance, data, getDereferencePath());

      if (!(performerHandle instanceof Long))
      {
         final IData realmData = getRealmData();
         if (null != realmData)
         {
            Object realmHandle = evaluateData(processInstance, realmData,
                  getRealmDereferencePath());
            performerHandle = new Pair(performerHandle, realmHandle);
         }
      }

      return performerHandle;
   }

   private Object evaluateData(IProcessInstance processInstance, IData data, String dereferencePath)
   {
      IDataValue dataValue = processInstance.getDataValue(data);
      if (null == dataValue)
      {
         throw new InternalException("No data value for data '" + data.getId()
               + "' available for conditional performer retrieval.");
      }

      ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(data, dereferencePath);
      AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(processInstance, null);
      return evaluator.evaluate(data, dataValue.getValue(),
            dereferencePath, evaluationContext);
   }

   private class InvalidHandleTypeException extends RuntimeException
   {
      public InvalidHandleTypeException(String message)
      {
         super(message);
      }
   }

}