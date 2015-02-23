
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Participants complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Participants">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="roles" type="{http://eclipse.org/stardust/ws/v2012a/api}Roles" minOccurs="0"/>
 *         &lt;element name="organizations" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Participants", propOrder = {
    "roles",
    "organizations"
})
public class ParticipantsXto {

    protected RolesXto roles;
    protected OrganizationsXto organizations;

    /**
     * Ruft den Wert der roles-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RolesXto }
     *     
     */
    public RolesXto getRoles() {
        return roles;
    }

    /**
     * Legt den Wert der roles-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RolesXto }
     *     
     */
    public void setRoles(RolesXto value) {
        this.roles = value;
    }

    /**
     * Ruft den Wert der organizations-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getOrganizations() {
        return organizations;
    }

    /**
     * Legt den Wert der organizations-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setOrganizations(OrganizationsXto value) {
        this.organizations = value;
    }

}
