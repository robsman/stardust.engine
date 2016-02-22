/*
 * Generated from Revision
 */
package org.eclipse.stardust.engine.api.ejb3.beans;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * The UserService provides functionality for operating on CARNOT users.
 * <p>
 * This includes:
 * <ul>
 * <li>creating, modifying and invalidating users, and</li>
 * <li>accessing user data.</li>
 * </ul>
 *
 * @author ubirkemeyer
 * @version $Revision
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class UserServiceImpl extends org.eclipse.stardust.engine.api.ejb3.beans.AbstractServiceImpl implements UserService, RemoteUserService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#startSession(java.lang.String clientId)
    */
   public java.lang.String startSession(
         java.lang.String clientId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).startSession(clientId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#closeSession(java.lang.String sessionId)
    */
   public void closeSession(
         java.lang.String sessionId,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).closeSession(sessionId);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthentified()
    */
   public boolean
         isInternalAuthentified(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).isInternalAuthentified();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthentication()
    */
   public boolean
         isInternalAuthentication(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).isInternalAuthentication();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthorization()
    */
   public boolean
         isInternalAuthorization(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).isInternalAuthorization();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User
         getUser(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUser();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyLoginUser(java.lang.String oldPassword, java.lang.String firstName, java.lang.String lastName, java.lang.String newPassword, java.lang.String eMail)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyLoginUser(
         java.lang.String oldPassword, java.lang.String firstName, java.lang.String
         lastName, java.lang.String newPassword, java.lang.String eMail,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).modifyLoginUser(oldPassword, firstName, lastName, newPassword, eMail);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUser(org.eclipse.stardust.engine.api.runtime.User user)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyUser(
         org.eclipse.stardust.engine.api.runtime.User user,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).modifyUser(user);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#generatePasswordResetToken(java.lang.String realm, java.lang.String account)
    */
   public void generatePasswordResetToken(
         java.lang.String realm, java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).generatePasswordResetToken(realm, account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#resetPassword(java.lang.String account, java.util.Map properties, java.lang.String token)
    */
   public void resetPassword(
         java.lang.String account, java.util.Map properties, java.lang.String token,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).resetPassword(account, properties, token);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUser(org.eclipse.stardust.engine.api.runtime.User user, boolean generatePassword)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyUser(
         org.eclipse.stardust.engine.api.runtime.User user, boolean generatePassword,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).modifyUser(user, generatePassword);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUser(java.lang.String account, java.lang.String firstName, java.lang.String lastName, java.lang.String description, java.lang.String password, java.lang.String eMail, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         createUser(
         java.lang.String account, java.lang.String firstName, java.lang.String lastName,
         java.lang.String description, java.lang.String password, java.lang.String eMail,
         java.util.Date validFrom, java.util.Date validTo,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).createUser(
            account, firstName, lastName, description, password, eMail, validFrom, validTo);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUser(java.lang.String realm, java.lang.String account, java.lang.String firstName, java.lang.String lastName, java.lang.String description, java.lang.String password, java.lang.String eMail, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         createUser(
         java.lang.String realm, java.lang.String account, java.lang.String firstName,
         java.lang.String lastName, java.lang.String description, java.lang.String
         password, java.lang.String eMail, java.util.Date validFrom, java.util.Date
         validTo, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).createUser(
            realm, account, firstName, lastName, description, password, eMail, validFrom,
            validTo);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(
         java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUser(account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(java.lang.String realm, java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(
         java.lang.String realm, java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUser(realm, account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(
         long userOID, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUser(userOID);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidate(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidate(
         java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).invalidate(account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUser(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidateUser(
         java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).invalidateUser(account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUser(java.lang.String realm, java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidateUser(
         java.lang.String realm, java.lang.String account,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).invalidateUser(realm, account);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUserGroup(java.lang.String id, java.lang.String name, java.lang.String description, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         createUserGroup(
         java.lang.String id, java.lang.String name, java.lang.String description,
         java.util.Date validFrom, java.util.Date validTo,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).createUserGroup(id, name, description, validFrom, validTo);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserGroup(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         getUserGroup(
         java.lang.String id, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUserGroup(id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserGroup(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup getUserGroup(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUserGroup(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUserGroup(org.eclipse.stardust.engine.api.runtime.UserGroup userGroup)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         modifyUserGroup(
         org.eclipse.stardust.engine.api.runtime.UserGroup userGroup,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).modifyUserGroup(userGroup);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUserGroup(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         invalidateUserGroup(
         java.lang.String id, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).invalidateUserGroup(id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUserGroup(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         invalidateUserGroup(
         long oid, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).invalidateUserGroup(oid);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUserRealm(java.lang.String id, java.lang.String name, java.lang.String description)
    */
   public org.eclipse.stardust.engine.api.runtime.UserRealm
         createUserRealm(
         java.lang.String id, java.lang.String name, java.lang.String description,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).createUserRealm(id, name, description);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#dropUserRealm(java.lang.String id)
    */
   public void dropUserRealm(
         java.lang.String id, org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).dropUserRealm(id);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserRealms()
    */
   public java.util.List
         getUserRealms(
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUserRealms();
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#addDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser, org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.Deputy
         addDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.api.runtime.DeputyOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).addDeputy(user, deputyUser, options);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser, org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.Deputy
         modifyDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.api.runtime.DeputyOptions options,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).modifyDeputy(user, deputyUser, options);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#removeDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
    */
   public void removeDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).removeDeputy(user, deputyUser);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getDeputies(org.eclipse.stardust.engine.api.runtime.UserInfo user)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Deputy>
         getDeputies(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getDeputies(user);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUsersBeingDeputyFor(org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Deputy>
         getUsersBeingDeputyFor(
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.core.runtime.ejb.TunneledContext
         __tunneledContext)throws org.eclipse.stardust.common.error.WorkflowException
    {
      java.util.Map<?, ?> __invocationContextBackup = null;
      try
      {
         __invocationContextBackup = initInvocationContext(__tunneledContext);
         return ((org.eclipse.stardust.engine.api.runtime.UserService)
            service).getUsersBeingDeputyFor(deputyUser);
      }
      catch(org.eclipse.stardust.common.error.PublicException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      catch(org.eclipse.stardust.common.error.ResourceException e)
      {
         throw new org.eclipse.stardust.common.error.WorkflowException(e);
      }
      finally
      {
         clearInvocationContext(__tunneledContext, __invocationContextBackup);
      }
    }

	public UserServiceImpl()
	{
      this.serviceType=org.eclipse.stardust.engine.api.runtime.UserService.class;
      this.serviceTypeImpl=org.eclipse.stardust.engine.core.runtime.beans.UserServiceImpl.class;
	}
}