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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessSpawnInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessSpawnInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="copyData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="parameters" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessSpawnInfo", propOrder = {
    "processId",
    "copyData",
    "parameters"
})
public class ProcessSpawnInfoXto {

    @XmlElement(required = true)
    protected String processId;
    protected boolean copyData;
    @XmlElement(required = true)
    protected ParametersXto parameters;

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
     * Gets the value of the copyData property.
     * 
     */
    public boolean isCopyData() {
        return copyData;
    }

    /**
     * Sets the value of the copyData property.
     * 
     */
    public void setCopyData(boolean value) {
        this.copyData = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setParameters(ParametersXto value) {
        this.parameters = value;
    }

}
