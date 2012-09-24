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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BpmInteractionFault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BpmInteractionFault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="faultCode" type="{http://eclipse.org/stardust/ws/v2012a/interactions}BpmInteractionFaultCode"/>
 *         &lt;element name="faultDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BpmInteractionFault", propOrder = {
    "faultCode",
    "faultDescription"
})
public class BpmInteractionFaultXto {

    @XmlElement(required = true)
    protected BpmInteractionFaultCodeXto faultCode;
    protected String faultDescription;

    /**
     * Gets the value of the faultCode property.
     * 
     * @return
     *     possible object is
     *     {@link BpmInteractionFaultCodeXto }
     *     
     */
    public BpmInteractionFaultCodeXto getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the value of the faultCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BpmInteractionFaultCodeXto }
     *     
     */
    public void setFaultCode(BpmInteractionFaultCodeXto value) {
        this.faultCode = value;
    }

    /**
     * Gets the value of the faultDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFaultDescription() {
        return faultDescription;
    }

    /**
     * Sets the value of the faultDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFaultDescription(String value) {
        this.faultDescription = value;
    }

}
