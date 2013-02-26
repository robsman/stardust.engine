package org.eclipse.stardust.engine.api.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * The UserService provides functionality for operating on CARNOT users.
 *  	  This includes:
 * creating, modifying and invalidating users, and accessing user data.
 * 
 *
 * This class was generated by Apache CXF 2.6.1
 * 2013-02-26T13:09:03.416+01:00
 * Generated source version: 2.6.1
 * 
 */
@WebService(targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", name = "IUserService")
@XmlSeeAlso({ObjectFactory.class})
public interface IUserService {

    /**
     * Retrieves information on the current user.
     * 
     */
    @WebResult(name = "user", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getSessionUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSessionUser")
    @WebMethod(action = "getSessionUser")
    @ResponseWrapper(localName = "getSessionUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetSessionUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto getSessionUser() throws BpmFault;

    /**
     * Retrieves all existing user realms.
     * 
     */
    @WebResult(name = "userRealms", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getUserRealms", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUserRealms")
    @WebMethod(action = "getUserRealms")
    @ResponseWrapper(localName = "getUserRealmsResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUserRealmsResponse")
    public org.eclipse.stardust.engine.api.ws.GetUserRealmsResponse.UserRealmsXto getUserRealms() throws BpmFault;

    /**
     * Checks if internal authentication is used.
     * 
     */
    @WebResult(name = "internalAuthentication", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "isInternalAuthentication", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.IsInternalAuthentication")
    @WebMethod(action = "isInternalAuthentication")
    @ResponseWrapper(localName = "isInternalAuthenticationResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.IsInternalAuthenticationResponse")
    public boolean isInternalAuthentication() throws BpmFault;

    /**
     * Creates a new user with default realm ID.
     * 
     */
    @WebResult(name = "createdUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUser")
    @WebMethod(action = "createUser")
    @ResponseWrapper(localName = "createUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto createUser(
        @WebParam(name = "user", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.UserXto user
    ) throws BpmFault;

    /**
     * Retrieves the specified user group.
     * 
     */
    @WebResult(name = "userGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUserGroup")
    @WebMethod(action = "getUserGroup")
    @ResponseWrapper(localName = "getUserGroupResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUserGroupResponse")
    public org.eclipse.stardust.engine.api.ws.UserGroupXto getUserGroup(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid
    ) throws BpmFault;

    /**
     * Retrieves the specified user by userOid.
     * 
     */
    @WebResult(name = "modifiedUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "getUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUser")
    @WebMethod(action = "getUser")
    @ResponseWrapper(localName = "getUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.GetUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto getUser(
        @WebParam(name = "oid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        long oid
    ) throws BpmFault;

    /**
     * Creates a new user group.
     * 
     */
    @WebResult(name = "createdUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUserGroup")
    @WebMethod(action = "createUserGroup")
    @ResponseWrapper(localName = "createUserGroupResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUserGroupResponse")
    public org.eclipse.stardust.engine.api.ws.UserGroupXto createUserGroup(
        @WebParam(name = "userGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.UserGroupXto userGroup
    ) throws BpmFault;

    /**
     * Drops the user realm associated with the given ID.
     * 
     */
    @RequestWrapper(localName = "dropUserRealm", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DropUserRealm")
    @WebMethod(action = "dropUserRealm")
    @ResponseWrapper(localName = "dropUserRealmResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.DropUserRealmResponse")
    public void dropUserRealm(
        @WebParam(name = "id", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String id
    ) throws BpmFault;

    /**
     * Creates a new user realm.
     * 
     */
    @WebResult(name = "userRealm", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "createUserRealm", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUserRealm")
    @WebMethod(action = "createUserRealm")
    @ResponseWrapper(localName = "createUserRealmResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.CreateUserRealmResponse")
    public org.eclipse.stardust.engine.api.ws.UserRealmXto createUserRealm(
        @WebParam(name = "id", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String id,
        @WebParam(name = "name", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String name,
        @WebParam(name = "description", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String description
    ) throws BpmFault;

    /**
     * Invalidates the specified user group.
     * 
     */
    @WebResult(name = "userGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "invalidateUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.InvalidateUserGroup")
    @WebMethod(action = "invalidateUserGroup")
    @ResponseWrapper(localName = "invalidateUserGroupResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.InvalidateUserGroupResponse")
    public org.eclipse.stardust.engine.api.ws.UserGroupXto invalidateUserGroup(
        @WebParam(name = "userGroupOid", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Long userGroupOid,
        @WebParam(name = "userGroupId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String userGroupId
    ) throws BpmFault;

    /**
     * Modifies the specified user.
     * 
     */
    @WebResult(name = "modifiedUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "modifyUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyUser")
    @WebMethod(action = "modifyUser")
    @ResponseWrapper(localName = "modifyUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto modifyUser(
        @WebParam(name = "user", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.UserXto user,
        @WebParam(name = "generatePassword", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.Boolean generatePassword
    ) throws BpmFault;

    /**
     * Resets the password of specified user by generated password according to configured password rules.
     * 
     */
    @RequestWrapper(localName = "resetPassword", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ResetPassword")
    @WebMethod(action = "resetPassword")
    @ResponseWrapper(localName = "resetPasswordResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ResetPasswordResponse")
    public void resetPassword(
        @WebParam(name = "account", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String account,
        @WebParam(name = "properties", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.MapXto properties
    ) throws BpmFault;

    /**
     * Invalidates the user with the specified account.
     * 
     */
    @WebResult(name = "user", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "invalidateUser", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.InvalidateUser")
    @WebMethod(action = "invalidateUser")
    @ResponseWrapper(localName = "invalidateUserResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.InvalidateUserResponse")
    public org.eclipse.stardust.engine.api.ws.UserXto invalidateUser(
        @WebParam(name = "accountId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String accountId,
        @WebParam(name = "realmId", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        java.lang.String realmId
    ) throws BpmFault;

    /**
     * Checks if internal authorization is used.
     * 
     */
    @WebResult(name = "internalAuthorization", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "isInternalAuthorization", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.IsInternalAuthorization")
    @WebMethod(action = "isInternalAuthorization")
    @ResponseWrapper(localName = "isInternalAuthorizationResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.IsInternalAuthorizationResponse")
    public boolean isInternalAuthorization() throws BpmFault;

    /**
     * Modifies the specified user group.
     * 
     */
    @WebResult(name = "modifiedUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
    @RequestWrapper(localName = "modifyUserGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyUserGroup")
    @WebMethod(action = "modifyUserGroup")
    @ResponseWrapper(localName = "modifyUserGroupResponse", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", className = "org.eclipse.stardust.engine.api.ws.ModifyUserGroupResponse")
    public org.eclipse.stardust.engine.api.ws.UserGroupXto modifyUserGroup(
        @WebParam(name = "userGroup", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api")
        org.eclipse.stardust.engine.api.ws.UserGroupXto userGroup
    ) throws BpmFault;
}
