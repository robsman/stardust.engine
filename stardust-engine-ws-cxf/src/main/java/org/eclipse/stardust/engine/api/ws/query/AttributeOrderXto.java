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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		Criterion for ordering elements resulting from a query according to a given attribute,
 * 		either with ascending or descending values.
 *         
 * 
 * <p>Java class for AttributeOrder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeOrder">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}OrderCriterion">
 *       &lt;sequence>
 *         &lt;element name="attribute" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AttributeReference"/>
 *         &lt;element name="ascending" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeOrder", propOrder = {
    "attribute",
    "ascending"
})
public class AttributeOrderXto
    extends OrderCriterionXto
{

    @XmlElement(required = true)
    protected AttributeReferenceXto attribute;
    @XmlElement(defaultValue = "true")
    protected boolean ascending;

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public AttributeReferenceXto getAttribute() {
        return attribute;
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public void setAttribute(AttributeReferenceXto value) {
        this.attribute = value;
    }

    /**
     * Gets the value of the ascending property.
     * 
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets the value of the ascending property.
     * 
     */
    public void setAscending(boolean value) {
        this.ascending = value;
    }

}
