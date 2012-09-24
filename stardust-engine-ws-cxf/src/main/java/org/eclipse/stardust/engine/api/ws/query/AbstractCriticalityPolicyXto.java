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
 * <p>Java class for AbstractCriticalityPolicy complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractCriticalityPolicy">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api/query}EvaluationPolicy">
 *       &lt;sequence>
 *         &lt;element name="lowPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="normalPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="highPriorityCriticalPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractCriticalityPolicy", propOrder = {
    "lowPriorityCriticalPct",
    "normalPriorityCriticalPct",
    "highPriorityCriticalPct"
})
@XmlSeeAlso({
    AbstractCriticalDurationPolicyXto.class
})
public abstract class AbstractCriticalityPolicyXto
    extends EvaluationPolicyXto
{

    protected float lowPriorityCriticalPct;
    protected float normalPriorityCriticalPct;
    protected float highPriorityCriticalPct;

    /**
     * Gets the value of the lowPriorityCriticalPct property.
     * 
     */
    public float getLowPriorityCriticalPct() {
        return lowPriorityCriticalPct;
    }

    /**
     * Sets the value of the lowPriorityCriticalPct property.
     * 
     */
    public void setLowPriorityCriticalPct(float value) {
        this.lowPriorityCriticalPct = value;
    }

    /**
     * Gets the value of the normalPriorityCriticalPct property.
     * 
     */
    public float getNormalPriorityCriticalPct() {
        return normalPriorityCriticalPct;
    }

    /**
     * Sets the value of the normalPriorityCriticalPct property.
     * 
     */
    public void setNormalPriorityCriticalPct(float value) {
        this.normalPriorityCriticalPct = value;
    }

    /**
     * Gets the value of the highPriorityCriticalPct property.
     * 
     */
    public float getHighPriorityCriticalPct() {
        return highPriorityCriticalPct;
    }

    /**
     * Sets the value of the highPriorityCriticalPct property.
     * 
     */
    public void setHighPriorityCriticalPct(float value) {
        this.highPriorityCriticalPct = value;
    }

}
