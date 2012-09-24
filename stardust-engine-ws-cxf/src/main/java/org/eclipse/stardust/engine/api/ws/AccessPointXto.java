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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.eclipse.stardust.common.Direction;


/**
 * 
 * 		    An AccessPoint is a modelling element where an Application provides access to it's
 *             data. It is used as the endpoint for a data mapping. An AccessPoint can expose a java
 *             type or a custom defined type to be the end point of a data mapping. Every application
 *             keeps a map of all access points identified by the ID attribute.
 * 		    
 * 
 * <p>Java class for AccessPoint complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccessPoint">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="direction" type="{http://eclipse.org/stardust/ws/v2012a/api}Direction" minOccurs="0"/>
 *         &lt;element name="accessPathEvaluatorClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attributes" type="{http://eclipse.org/stardust/ws/v2012a/api}Attributes" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccessPoint", propOrder = {
    "id",
    "name",
    "direction",
    "accessPathEvaluatorClass",
    "attributes"
})
public class AccessPointXto {

    protected String id;
    protected String name;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Direction direction;
    protected String accessPathEvaluatorClass;
    protected AttributesXto attributes;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirection(Direction value) {
        this.direction = value;
    }

    /**
     * Gets the value of the accessPathEvaluatorClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccessPathEvaluatorClass() {
        return accessPathEvaluatorClass;
    }

    /**
     * Sets the value of the accessPathEvaluatorClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccessPathEvaluatorClass(String value) {
        this.accessPathEvaluatorClass = value;
    }

    /**
     * Gets the value of the attributes property.
     * 
     * @return
     *     possible object is
     *     {@link AttributesXto }
     *     
     */
    public AttributesXto getAttributes() {
        return attributes;
    }

    /**
     * Sets the value of the attributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributesXto }
     *     
     */
    public void setAttributes(AttributesXto value) {
        this.attributes = value;
    }

}
