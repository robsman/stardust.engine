
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	       A client view of a workflow organizational unit.
 *            An organization is a logical grouping of workflow participants.
 * 	       
 * 
 * <p>Java-Klasse für Organization complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Organization">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelParticipant">
 *       &lt;sequence>
 *         &lt;element name="teamLeadRole" type="{http://eclipse.org/stardust/ws/v2012a/api}Role" minOccurs="0"/>
 *         &lt;element name="allSubRoles" type="{http://eclipse.org/stardust/ws/v2012a/api}Roles" minOccurs="0"/>
 *         &lt;element name="allSubOrganizations" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Organization", propOrder = {
    "teamLeadRole",
    "allSubRoles",
    "allSubOrganizations"
})
public class OrganizationXto
    extends ModelParticipantXto
{

    protected RoleXto teamLeadRole;
    protected RolesXto allSubRoles;
    protected OrganizationsXto allSubOrganizations;

    /**
     * Ruft den Wert der teamLeadRole-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RoleXto }
     *     
     */
    public RoleXto getTeamLeadRole() {
        return teamLeadRole;
    }

    /**
     * Legt den Wert der teamLeadRole-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RoleXto }
     *     
     */
    public void setTeamLeadRole(RoleXto value) {
        this.teamLeadRole = value;
    }

    /**
     * Ruft den Wert der allSubRoles-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RolesXto }
     *     
     */
    public RolesXto getAllSubRoles() {
        return allSubRoles;
    }

    /**
     * Legt den Wert der allSubRoles-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RolesXto }
     *     
     */
    public void setAllSubRoles(RolesXto value) {
        this.allSubRoles = value;
    }

    /**
     * Ruft den Wert der allSubOrganizations-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getAllSubOrganizations() {
        return allSubOrganizations;
    }

    /**
     * Legt den Wert der allSubOrganizations-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setAllSubOrganizations(OrganizationsXto value) {
        this.allSubOrganizations = value;
    }

}
