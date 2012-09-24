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

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * 
 * 		    The HistoricalState represents a snapshot of the historic states of an
 *             activity instance.
 *             
 * 
 * <p>Java class for HistoricalState complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HistoricalState">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="activityDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processInstanceOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="processDefinitionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityState" type="{http://eclipse.org/stardust/ws/v2012a/api}ActivityInstanceState"/>
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="until" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="participant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *         &lt;element name="onBehalfOfParticipant" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
 *         &lt;element name="user" type="{http://eclipse.org/stardust/ws/v2012a/api}User"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HistoricalState", propOrder = {
    "activityOid",
    "activityDefinitionId",
    "processInstanceOid",
    "processDefinitionId",
    "activityState",
    "from",
    "until",
    "participant",
    "onBehalfOfParticipant",
    "user"
})
public class HistoricalStateXto {

    protected long activityOid;
    @XmlElement(required = true)
    protected String activityDefinitionId;
    protected long processInstanceOid;
    @XmlElement(required = true)
    protected String processDefinitionId;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter3 .class)
    protected ActivityInstanceState activityState;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date from;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date until;
    @XmlElement(required = true)
    protected ParticipantInfoXto participant;
    @XmlElement(required = true)
    protected ParticipantInfoXto onBehalfOfParticipant;
    @XmlElement(required = true)
    protected UserXto user;

    /**
     * Gets the value of the activityOid property.
     * 
     */
    public long getActivityOid() {
        return activityOid;
    }

    /**
     * Sets the value of the activityOid property.
     * 
     */
    public void setActivityOid(long value) {
        this.activityOid = value;
    }

    /**
     * Gets the value of the activityDefinitionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityDefinitionId() {
        return activityDefinitionId;
    }

    /**
     * Sets the value of the activityDefinitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityDefinitionId(String value) {
        this.activityDefinitionId = value;
    }

    /**
     * Gets the value of the processInstanceOid property.
     * 
     */
    public long getProcessInstanceOid() {
        return processInstanceOid;
    }

    /**
     * Sets the value of the processInstanceOid property.
     * 
     */
    public void setProcessInstanceOid(long value) {
        this.processInstanceOid = value;
    }

    /**
     * Gets the value of the processDefinitionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    /**
     * Sets the value of the processDefinitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessDefinitionId(String value) {
        this.processDefinitionId = value;
    }

    /**
     * Gets the value of the activityState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ActivityInstanceState getActivityState() {
        return activityState;
    }

    /**
     * Sets the value of the activityState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityState(ActivityInstanceState value) {
        this.activityState = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrom(Date value) {
        this.from = value;
    }

    /**
     * Gets the value of the until property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getUntil() {
        return until;
    }

    /**
     * Sets the value of the until property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUntil(Date value) {
        this.until = value;
    }

    /**
     * Gets the value of the participant property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getParticipant() {
        return participant;
    }

    /**
     * Sets the value of the participant property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setParticipant(ParticipantInfoXto value) {
        this.participant = value;
    }

    /**
     * Gets the value of the onBehalfOfParticipant property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getOnBehalfOfParticipant() {
        return onBehalfOfParticipant;
    }

    /**
     * Sets the value of the onBehalfOfParticipant property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setOnBehalfOfParticipant(ParticipantInfoXto value) {
        this.onBehalfOfParticipant = value;
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
