
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	       A client view of a workflow role.
 * 		   A role represents the context in which the user participates in the execution of a process or activity.
 * 	       
 * 
 * <p>Java-Klasse für Role complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Role">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelParticipant">
 *       &lt;sequence>
 *         &lt;element name="teams" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *         &lt;element name="clientOrganizations" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Role", propOrder = {
    "teams",
    "clientOrganizations"
})
public class RoleXto
    extends ModelParticipantXto
{

    protected OrganizationsXto teams;
    protected OrganizationsXto clientOrganizations;

    /**
     * Ruft den Wert der teams-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getTeams() {
        return teams;
    }

    /**
     * Legt den Wert der teams-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setTeams(OrganizationsXto value) {
        this.teams = value;
    }

    /**
     * Ruft den Wert der clientOrganizations-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getClientOrganizations() {
        return clientOrganizations;
    }

    /**
     * Legt den Wert der clientOrganizations-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setClientOrganizations(OrganizationsXto value) {
        this.clientOrganizations = value;
    }

}
