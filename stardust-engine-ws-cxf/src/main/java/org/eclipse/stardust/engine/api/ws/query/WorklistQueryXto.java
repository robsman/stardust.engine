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

package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 	    Additionally supports optional elements:
 * 	    A 'userContribution' which enables or disables the retrieval of the user's private worklist with an optional 'subsetPolicy'.
 * 	    A list of 'participantContributions' limiting the result to specified participants. One 'subsetPolicy' per 'participantContribution' can be set.
 * 	    
 * 
 * <p>Java class for WorklistQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WorklistQuery">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}Query">
 *       &lt;sequence>
 *         &lt;element name="userContribution" type="{http://eclipse.org/stardust/ws/v2012a/api/query}UserContribution" minOccurs="0"/>
 *         &lt;element name="participantContributions" type="{http://eclipse.org/stardust/ws/v2012a/api/query}ParticipantContributions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorklistQuery", propOrder = {
    "userContribution",
    "participantContributions"
})
public class WorklistQueryXto
    extends QueryXto
{

    protected UserContributionXto userContribution;
    protected ParticipantContributionsXto participantContributions;

    /**
     * Gets the value of the userContribution property.
     * 
     * @return
     *     possible object is
     *     {@link UserContributionXto }
     *     
     */
    public UserContributionXto getUserContribution() {
        return userContribution;
    }

    /**
     * Sets the value of the userContribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserContributionXto }
     *     
     */
    public void setUserContribution(UserContributionXto value) {
        this.userContribution = value;
    }

    /**
     * Gets the value of the participantContributions property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantContributionsXto }
     *     
     */
    public ParticipantContributionsXto getParticipantContributions() {
        return participantContributions;
    }

    /**
     * Sets the value of the participantContributions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantContributionsXto }
     *     
     */
    public void setParticipantContributions(ParticipantContributionsXto value) {
        this.participantContributions = value;
    }

}
