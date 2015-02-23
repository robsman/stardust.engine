
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *          The User represents a snapshot of the user state.
 *          It contains general user information, as well as information regarding the
 *          permissions the user currently has.
 *    		The User can be modified and used to update the user's information including grants and userGroups.
 *          
 * 
 * <p>Java-Klasse für User complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="User">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="qualifiedId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="accountId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="firstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousLoginTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="eMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="administrator" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="permissionStates" type="{http://eclipse.org/stardust/ws/v2012a/api}PermissionStates" minOccurs="0"/>
 *         &lt;element name="detailsLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userRealm" type="{http://eclipse.org/stardust/ws/v2012a/api}UserRealm" minOccurs="0"/>
 *         &lt;element name="userGroups" type="{http://eclipse.org/stardust/ws/v2012a/api}UserGroups" minOccurs="0"/>
 *         &lt;element name="grants" type="{http://eclipse.org/stardust/ws/v2012a/api}Grants" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *         &lt;element name="passwordExpired" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="qualityAssuranceProbability" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", propOrder = {
    "oid",
    "qualifiedId",
    "accountId",
    "firstName",
    "lastName",
    "password",
    "previousLoginTime",
    "eMail",
    "validFrom",
    "validTo",
    "description",
    "administrator",
    "permissionStates",
    "detailsLevel",
    "userRealm",
    "userGroups",
    "grants",
    "attributes",
    "passwordExpired",
    "qualityAssuranceProbability"
})
public class UserXto {

    protected Long oid;
    protected String qualifiedId;
    @XmlElement(required = true)
    protected String accountId;
    protected String firstName;
    protected String lastName;
    protected String password;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date previousLoginTime;
    protected String eMail;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validFrom;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date validTo;
    protected String description;
    protected boolean administrator;
    protected PermissionStatesXto permissionStates;
    protected Integer detailsLevel;
    protected UserRealmXto userRealm;
    protected UserGroupsXto userGroups;
    protected GrantsXto grants;
    protected AttributesXto attributes;
    protected boolean passwordExpired;
    protected Integer qualityAssuranceProbability;

    /**
     * Ruft den Wert der oid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Legt den Wert der oid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOid(Long value) {
        this.oid = value;
    }

    /**
     * Ruft den Wert der qualifiedId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQualifiedId() {
        return qualifiedId;
    }

    /**
     * Legt den Wert der qualifiedId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQualifiedId(String value) {
        this.qualifiedId = value;
    }

    /**
     * Ruft den Wert der accountId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Legt den Wert der accountId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccountId(String value) {
        this.accountId = value;
    }

    /**
     * Ruft den Wert der firstName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Legt den Wert der firstName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstName(String value) {
        this.firstName = value;
    }

    /**
     * Ruft den Wert der lastName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Legt den Wert der lastName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastName(String value) {
        this.lastName = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der previousLoginTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getPreviousLoginTime() {
        return previousLoginTime;
    }

    /**
     * Legt den Wert der previousLoginTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousLoginTime(Date value) {
        this.previousLoginTime = value;
    }

    /**
     * Ruft den Wert der eMail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEMail() {
        return eMail;
    }

    /**
     * Legt den Wert der eMail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEMail(String value) {
        this.eMail = value;
    }

    /**
     * Ruft den Wert der validFrom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Legt den Wert der validFrom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidFrom(Date value) {
        this.validFrom = value;
    }

    /**
     * Ruft den Wert der validTo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getValidTo() {
        return validTo;
    }

    /**
     * Legt den Wert der validTo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidTo(Date value) {
        this.validTo = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der administrator-Eigenschaft ab.
     * 
     */
    public boolean isAdministrator() {
        return administrator;
    }

    /**
     * Legt den Wert der administrator-Eigenschaft fest.
     * 
     */
    public void setAdministrator(boolean value) {
        this.administrator = value;
    }

    /**
     * Ruft den Wert der permissionStates-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PermissionStatesXto }
     *     
     */
    public PermissionStatesXto getPermissionStates() {
        return permissionStates;
    }

    /**
     * Legt den Wert der permissionStates-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PermissionStatesXto }
     *     
     */
    public void setPermissionStates(PermissionStatesXto value) {
        this.permissionStates = value;
    }

    /**
     * Ruft den Wert der detailsLevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDetailsLevel() {
        return detailsLevel;
    }

    /**
     * Legt den Wert der detailsLevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDetailsLevel(Integer value) {
        this.detailsLevel = value;
    }

    /**
     * Ruft den Wert der userRealm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserRealmXto }
     *     
     */
    public UserRealmXto getUserRealm() {
        return userRealm;
    }

    /**
     * Legt den Wert der userRealm-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserRealmXto }
     *     
     */
    public void setUserRealm(UserRealmXto value) {
        this.userRealm = value;
    }

    /**
     * Ruft den Wert der userGroups-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UserGroupsXto }
     *     
     */
    public UserGroupsXto getUserGroups() {
        return userGroups;
    }

    /**
     * Legt den Wert der userGroups-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UserGroupsXto }
     *     
     */
    public void setUserGroups(UserGroupsXto value) {
        this.userGroups = value;
    }

    /**
     * Ruft den Wert der grants-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GrantsXto }
     *     
     */
    public GrantsXto getGrants() {
        return grants;
    }

    /**
     * Legt den Wert der grants-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GrantsXto }
     *     
     */
    public void setGrants(GrantsXto value) {
        this.grants = value;
    }

    /**
     * Ruft den Wert der attributes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AttributesXto }
     *     
     */
    public AttributesXto getAttributes() {
        return attributes;
    }

    /**
     * Legt den Wert der attributes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributesXto }
     *     
     */
    public void setAttributes(AttributesXto value) {
        this.attributes = value;
    }

    /**
     * Ruft den Wert der passwordExpired-Eigenschaft ab.
     * 
     */
    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    /**
     * Legt den Wert der passwordExpired-Eigenschaft fest.
     * 
     */
    public void setPasswordExpired(boolean value) {
        this.passwordExpired = value;
    }

    /**
     * Ruft den Wert der qualityAssuranceProbability-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getQualityAssuranceProbability() {
        return qualityAssuranceProbability;
    }

    /**
     * Legt den Wert der qualityAssuranceProbability-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setQualityAssuranceProbability(Integer value) {
        this.qualityAssuranceProbability = value;
    }

}
