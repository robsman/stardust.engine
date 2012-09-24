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

package org.eclipse.stardust.engine.api.ws.interactions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.stardust.engine.api.ws.ParametersXto;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="inputParameters" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "inputParameters"
})
@XmlRootElement(name = "getInputParametersResponse")
public class GetInputParametersResponse {

    @XmlElement(required = true, nillable = true)
    protected ParametersXto inputParameters;

    /**
     * Gets the value of the inputParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getInputParameters() {
        return inputParameters;
    }

    /**
     * Sets the value of the inputParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setInputParameters(ParametersXto value) {
        this.inputParameters = value;
    }

}
