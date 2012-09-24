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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Restricts the resulting items to the ones related to a specific activity.
 *  		The search can be further restricted to certain models by passing in a collection of model oids
 *  		and to a process definition scope by using the 'processId' element.
 *         
 * 
 * <p>Java class for ActivityDefinitionFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivityDefinitionFilter">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="activityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="includingSubprocesses" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="modelOids">
 *         &lt;simpleType>
 *           &lt;list itemType="{http://www.w3.org/2001/XMLSchema}long" />
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityDefinitionFilter", propOrder = {
    "activityId",
    "processId"
})
public class ActivityDefinitionFilterXto
    extends PredicateBaseXto
{

    @XmlElement(required = true)
    protected String activityId;
    protected String processId;
    @XmlAttribute
    protected Boolean includingSubprocesses;
    @XmlAttribute
    protected List<Long> modelOids;

    /**
     * Gets the value of the activityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * Sets the value of the activityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityId(String value) {
        this.activityId = value;
    }

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessId(String value) {
        this.processId = value;
    }

    /**
     * Gets the value of the includingSubprocesses property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIncludingSubprocesses() {
        if (includingSubprocesses == null) {
            return false;
        } else {
            return includingSubprocesses;
        }
    }

    /**
     * Sets the value of the includingSubprocesses property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludingSubprocesses(Boolean value) {
        this.includingSubprocesses = value;
    }

    /**
     * Gets the value of the modelOids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modelOids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModelOids().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getModelOids() {
        if (modelOids == null) {
            modelOids = new ArrayList<Long>();
        }
        return this.modelOids;
    }

}
