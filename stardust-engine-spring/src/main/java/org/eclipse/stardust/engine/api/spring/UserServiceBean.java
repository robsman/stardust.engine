/*
 * Generated from Revision: 66568 
 */
package org.eclipse.stardust.engine.api.spring;

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
 * @version 66568
 */
public class UserServiceBean extends org.eclipse.stardust.engine.api.spring.AbstractSpringServiceBean implements IUserService
{

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#startSession(java.lang.String clientId)
    */
   public java.lang.String startSession(java.lang.String clientId)
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).startSession(clientId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#closeSession(java.lang.String sessionId)
    */
   public void closeSession(java.lang.String sessionId)
   {
      ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).closeSession(sessionId);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthentified()
    */
   public boolean isInternalAuthentified()
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).isInternalAuthentified();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthentication()
    */
   public boolean isInternalAuthentication()
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).isInternalAuthentication();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#isInternalAuthorization()
    */
   public boolean isInternalAuthorization()
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).isInternalAuthorization();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser()
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser()
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUser();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyLoginUser(java.lang.String oldPassword, java.lang.String firstName, java.lang.String lastName, java.lang.String newPassword, java.lang.String eMail)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyLoginUser(
         java.lang.String oldPassword, java.lang.String firstName, java.lang.String
         lastName, java.lang.String newPassword, java.lang.String eMail)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).modifyLoginUser(
            oldPassword, firstName, lastName, newPassword, eMail);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUser(org.eclipse.stardust.engine.api.runtime.User user)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyUser(org.eclipse.stardust.engine.api.runtime.User user)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).modifyUser(user);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#resetPassword(java.lang.String account, java.util.Map properties)
    */
   public void resetPassword(java.lang.String account, java.util.Map properties)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).resetPassword(account, properties);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUser(org.eclipse.stardust.engine.api.runtime.User user, boolean generatePassword)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         modifyUser(
         org.eclipse.stardust.engine.api.runtime.User user, boolean generatePassword)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException,
         org.eclipse.stardust.common.error.AccessForbiddenException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).modifyUser(user, generatePassword);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUser(java.lang.String account, java.lang.String firstName, java.lang.String lastName, java.lang.String description, java.lang.String password, java.lang.String eMail, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         createUser(
         java.lang.String account, java.lang.String firstName, java.lang.String lastName,
         java.lang.String description, java.lang.String password, java.lang.String eMail,
         java.util.Date validFrom, java.util.Date validTo)
         throws org.eclipse.stardust.engine.api.runtime.UserExistsException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).createUser(
            account, firstName, lastName, description, password, eMail, validFrom,
            validTo);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUser(java.lang.String realm, java.lang.String account, java.lang.String firstName, java.lang.String lastName, java.lang.String description, java.lang.String password, java.lang.String eMail, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         createUser(
         java.lang.String realm, java.lang.String account, java.lang.String firstName,
         java.lang.String lastName, java.lang.String description, java.lang.String
         password, java.lang.String eMail, java.util.Date validFrom, java.util.Date
         validTo)
         throws org.eclipse.stardust.engine.api.runtime.UserExistsException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).createUser(
            realm, account, firstName, lastName, description, password, eMail, validFrom,
            validTo);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(
         java.lang.String account)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUser(account);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(java.lang.String realm, java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(
         java.lang.String realm, java.lang.String account)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUser(realm, account);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUser(long userOID)
    */
   public org.eclipse.stardust.engine.api.runtime.User getUser(long userOID)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUser(userOID);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidate(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidate(java.lang.String account)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).invalidate(account);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUser(java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidateUser(java.lang.String account)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).invalidateUser(account);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUser(java.lang.String realm, java.lang.String account)
    */
   public org.eclipse.stardust.engine.api.runtime.User
         invalidateUser(java.lang.String realm, java.lang.String account)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).invalidateUser(realm, account);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUserGroup(java.lang.String id, java.lang.String name, java.lang.String description, java.util.Date validFrom, java.util.Date validTo)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         createUserGroup(
         java.lang.String id, java.lang.String name, java.lang.String description,
         java.util.Date validFrom, java.util.Date validTo)
         throws org.eclipse.stardust.engine.api.runtime.UserGroupExistsException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException,
         org.eclipse.stardust.common.security.InvalidPasswordException,
         org.eclipse.stardust.common.error.InvalidArgumentException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).createUserGroup(id, name, description, validFrom, validTo);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserGroup(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         getUserGroup(java.lang.String id)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUserGroup(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserGroup(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup getUserGroup(
         long oid)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUserGroup(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyUserGroup(org.eclipse.stardust.engine.api.runtime.UserGroup userGroup)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         modifyUserGroup(org.eclipse.stardust.engine.api.runtime.UserGroup userGroup)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).modifyUserGroup(userGroup);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUserGroup(java.lang.String id)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         invalidateUserGroup(java.lang.String id)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).invalidateUserGroup(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#invalidateUserGroup(long oid)
    */
   public org.eclipse.stardust.engine.api.runtime.UserGroup
         invalidateUserGroup(long oid)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).invalidateUserGroup(oid);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#createUserRealm(java.lang.String id, java.lang.String name, java.lang.String description)
    */
   public org.eclipse.stardust.engine.api.runtime.UserRealm
         createUserRealm(
         java.lang.String id, java.lang.String name, java.lang.String description)
         throws org.eclipse.stardust.engine.api.runtime.UserRealmExistsException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).createUserRealm(id, name, description);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#dropUserRealm(java.lang.String id)
    */
   public void dropUserRealm(java.lang.String id)
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.common.error.ObjectNotFoundException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).dropUserRealm(id);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUserRealms()
    */
   public java.util.List getUserRealms()
         throws org.eclipse.stardust.common.error.ConcurrencyException,
         org.eclipse.stardust.engine.api.runtime.IllegalOperationException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUserRealms();
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#addDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser, org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.Deputy
         addDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).addDeputy(user, deputyUser, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#modifyDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser, org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
    */
   public org.eclipse.stardust.engine.api.runtime.Deputy
         modifyDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser,
         org.eclipse.stardust.engine.api.runtime.DeputyOptions options)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).modifyDeputy(user, deputyUser, options);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#removeDeputy(org.eclipse.stardust.engine.api.runtime.UserInfo user, org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
    */
   public void removeDeputy(
         org.eclipse.stardust.engine.api.runtime.UserInfo user,
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
         throws org.eclipse.stardust.common.error.ObjectNotFoundException
   {
      ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).removeDeputy(user, deputyUser);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getDeputies(org.eclipse.stardust.engine.api.runtime.UserInfo user)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Deputy>
         getDeputies(org.eclipse.stardust.engine.api.runtime.UserInfo user)
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getDeputies(user);
   }

   /**
    * @see org.eclipse.stardust.engine.api.runtime.UserService#getUsersBeingDeputyFor(org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
    */
   public java.util.List<org.eclipse.stardust.engine.api.runtime.Deputy>
         getUsersBeingDeputyFor(
         org.eclipse.stardust.engine.api.runtime.UserInfo deputyUser)
   {
      return ((org.eclipse.stardust.engine.api.runtime.UserService)
            serviceProxy).getUsersBeingDeputyFor(deputyUser);
   }

	public UserServiceBean()
	{
      super(org.eclipse.stardust.engine.api.runtime.UserService.class,
            org.eclipse.stardust.engine.core.runtime.beans.UserServiceImpl.class);
	}
}