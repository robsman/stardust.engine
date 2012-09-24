/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

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
 * <p>Java class for Role complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the teams property.
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
     * Sets the value of the teams property.
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
     * Gets the value of the clientOrganizations property.
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
     * Sets the value of the clientOrganizations property.
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
