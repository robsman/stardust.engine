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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.engine.api.ws.xsd.Adapter1;


/**
 * <p>Java class for EventBindingBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventBindingBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="handlerId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bound" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="timeout" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="bindingAttributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventBindingBase", propOrder = {
    "handlerId",
    "bound",
    "timeout",
    "bindingAttributes"
})
@XmlSeeAlso({
    ProcessEventBindingXto.class,
    ActivityEventBindingXto.class
})
public class EventBindingBaseXto {

    @XmlElement(required = true)
    protected String handlerId;
    protected boolean bound;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Date timeout;
    protected AttributesXto bindingAttributes;

    /**
     * Gets the value of the handlerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandlerId() {
        return handlerId;
    }

    /**
     * Sets the value of the handlerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandlerId(String value) {
        this.handlerId = value;
    }

    /**
     * Gets the value of the bound property.
     * 
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Sets the value of the bound property.
     * 
     */
    public void setBound(boolean value) {
        this.bound = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeout(Date value) {
        this.timeout = value;
    }

    /**
     * Gets the value of the bindingAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link AttributesXto }
     *     
     */
    public AttributesXto getBindingAttributes() {
        return bindingAttributes;
    }

    /**
     * Sets the value of the bindingAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributesXto }
     *     
     */
    public void setBindingAttributes(AttributesXto value) {
        this.bindingAttributes = value;
    }

}
