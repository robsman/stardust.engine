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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="context" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="outDataValues" type="{http://eclipse.org/stardust/ws/v2012a/api}Parameters"/>
 *         &lt;element name="activate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "activityOid",
    "context",
    "outDataValues",
    "activate"
})
@XmlRootElement(name = "completeActivity")
public class CompleteActivity {

    protected long activityOid;
    @XmlElement(required = true, nillable = true)
    protected String context;
    @XmlElement(required = true, nillable = true)
    protected ParametersXto outDataValues;
    @XmlElementRef(name = "activate", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Boolean> activate;

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
     * Gets the value of the context property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContext(String value) {
        this.context = value;
    }

    /**
     * Gets the value of the outDataValues property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersXto }
     *     
     */
    public ParametersXto getOutDataValues() {
        return outDataValues;
    }

    /**
     * Sets the value of the outDataValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersXto }
     *     
     */
    public void setOutDataValues(ParametersXto value) {
        this.outDataValues = value;
    }

    /**
     * Gets the value of the activate property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getActivate() {
        return activate;
    }

    /**
     * Sets the value of the activate property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setActivate(JAXBElement<Boolean> value) {
        this.activate = ((JAXBElement<Boolean> ) value);
    }

}
