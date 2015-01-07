
package org.eclipse.stardust.engine.api.ws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 *             The User represents a snapshot of the user state.
 *             It contains general user information, as well as information regarding the
 *             permissions the user currently has.
 *  			The User can be modified and used to update the user's information including grants and userGroups.
 * 	        
 * 
 * <p>Java class for User complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="User">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="accountId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="firstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousLoginTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="eMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="validTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="detailsLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="userRealm" type="{http://eclipse.org/stardust/ws/v2012a/api}UserRealm" minOccurs="0"/>
 *         &lt;element name="userGroups" type="{http://eclipse.org/stardust/ws/v2012a/api}UserGroups" minOccurs="0"/>
 *         &lt;element name="grants" type="{http://eclipse.org/stardust/ws/v2012a/api}Grants" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *         &lt;element name="passwordExpired" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "accountId",
    "firstName",
    "lastName",
    "password",
    "previousLoginTime",
    "eMail",
    "validFrom",
    "validTo",
    "description",
    "detailsLevel",
    "userRealm",
    "userGroups",
    "grants",
    "attributes",
    "passwordExpired"
})
public class UserXto {

    protected Long oid;
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
    protected Integer detailsLevel;
    protected UserRealmXto userRealm;
    protected UserGroupsXto userGroups;
    protected GrantsXto grants;
    protected AttributesXto attributes;
    protected boolean passwordExpired;

    /**
     * Gets the value of the oid property.
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
     * Sets the value of the oid property.
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
     * Gets the value of the accountId property.
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
     * Sets the value of the accountId property.
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
     * Gets the value of the firstName property.
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
     * Sets the value of the firstName property.
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
     * Gets the value of the lastName property.
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
     * Sets the value of the lastName property.
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
     * Gets the value of the password property.
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
     * Sets the value of the password property.
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
     * Gets the value of the previousLoginTime property.
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
     * Sets the value of the previousLoginTime property.
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
     * Gets the value of the eMail property.
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
     * Sets the value of the eMail property.
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
     * Gets the value of the validFrom property.
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
     * Sets the value of the validFrom property.
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
     * Gets the value of the validTo property.
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
     * Sets the value of the validTo property.
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
     * Gets the value of the description property.
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
     * Sets the value of the description property.
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
     * Gets the value of the detailsLevel property.
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
     * Sets the value of the detailsLevel property.
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
     * Gets the value of the userRealm property.
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
     * Sets the value of the userRealm property.
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
     * Gets the value of the userGroups property.
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
     * Sets the value of the userGroups property.
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
     * Gets the value of the grants property.
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
     * Sets the value of the grants property.
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
     * Gets the value of the attributes property.
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
     * Sets the value of the attributes property.
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
     * Gets the value of the passwordExpired property.
     * 
     */
    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    /**
     * Sets the value of the passwordExpired property.
     * 
     */
    public void setPasswordExpired(boolean value) {
        this.passwordExpired = value;
    }

}
