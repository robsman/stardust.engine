
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
 * <p>Java class for Organization complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the teamLeadRole property.
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
     * Sets the value of the teamLeadRole property.
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
     * Gets the value of the allSubRoles property.
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
     * Sets the value of the allSubRoles property.
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
     * Gets the value of the allSubOrganizations property.
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
     * Sets the value of the allSubOrganizations property.
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
