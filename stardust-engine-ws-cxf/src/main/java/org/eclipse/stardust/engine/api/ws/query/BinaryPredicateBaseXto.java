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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BinaryPredicateBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinaryPredicateBase">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}PredicateBase">
 *       &lt;sequence>
 *         &lt;element name="lhsVariable" type="{http://eclipse.org/stardust/ws/v2012a/api/query}VariableReference" minOccurs="0"/>
 *         &lt;element name="lhsAttribute" type="{http://eclipse.org/stardust/ws/v2012a/api/query}AttributeReference" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryPredicateBase", propOrder = {
    "lhsVariable",
    "lhsAttribute"
})
@XmlSeeAlso({
    BinaryPredicateXto.class,
    BinaryListValuedPredicateXto.class
})
public abstract class BinaryPredicateBaseXto
    extends PredicateBaseXto
{

    protected VariableReferenceXto lhsVariable;
    protected AttributeReferenceXto lhsAttribute;

    /**
     * Gets the value of the lhsVariable property.
     * 
     * @return
     *     possible object is
     *     {@link VariableReferenceXto }
     *     
     */
    public VariableReferenceXto getLhsVariable() {
        return lhsVariable;
    }

    /**
     * Sets the value of the lhsVariable property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableReferenceXto }
     *     
     */
    public void setLhsVariable(VariableReferenceXto value) {
        this.lhsVariable = value;
    }

    /**
     * Gets the value of the lhsAttribute property.
     * 
     * @return
     *     possible object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public AttributeReferenceXto getLhsAttribute() {
        return lhsAttribute;
    }

    /**
     * Sets the value of the lhsAttribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributeReferenceXto }
     *     
     */
    public void setLhsAttribute(AttributeReferenceXto value) {
        this.lhsAttribute = value;
    }

}
