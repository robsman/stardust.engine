
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	       The Participant contains a role, organization or user.
 * 	       
 * 
 * <p>Java-Klasse für Participant complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der role-Eigenschaft ab.
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
     * Legt den Wert der role-Eigenschaft fest.
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
     * Ruft den Wert der organization-Eigenschaft ab.
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
     * Legt den Wert der organization-Eigenschaft fest.
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
     * Ruft den Wert der user-Eigenschaft ab.
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
     * Legt den Wert der user-Eigenschaft fest.
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
