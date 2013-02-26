
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	       The Participant contains a role, organization or user.
 * 	       
 * 
 * <p>Java class for Participant complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Participant">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="role" type="{http://eclipse.org/stardust/ws/v2012a/api}Role" minOccurs="0"/>
 *         &lt;element name="organization" type="{http://eclipse.org/stardust/ws/v2012a/api}Organization" minOccurs="0"/>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}User" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Participant", propOrder = {
    "role",
    "organization",
    "user"
})
public class ParticipantXto {

    protected RoleXto role;
    protected OrganizationXto organization;
    protected UserXto user;

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link RoleXto }
     *     
     */
    public RoleXto getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleXto }
     *     
     */
    public void setRole(RoleXto value) {
        this.role = value;
    }

    /**
     * Gets the value of the organization property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationXto }
     *     
     */
    public OrganizationXto getOrganization() {
        return organization;
    }

    /**
     * Sets the value of the organization property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationXto }
     *     
     */
    public void setOrganization(OrganizationXto value) {
        this.organization = value;
    }

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link UserXto }
     *     
     */
    public UserXto getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserXto }
     *     
     */
    public void setUser(UserXto value) {
        this.user = value;
    }

}
