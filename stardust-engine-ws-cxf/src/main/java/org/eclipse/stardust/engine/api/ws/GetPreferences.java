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
 *         &lt;element name="scope" type="{http://eclipse.org/stardust/ws/v2012a/api}PreferenceScope"/>
 *         &lt;element name="moduleId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="preferencesId" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "scope",
    "moduleId",
    "preferencesId"
})
@XmlRootElement(name = "getPreferences")
public class GetPreferences {

    @XmlElement(required = true, nillable = true)
    protected PreferenceScopeXto scope;
    @XmlElement(required = true)
    protected String moduleId;
    @XmlElement(required = true)
    protected String preferencesId;

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link PreferenceScopeXto }
     *     
     */
    public PreferenceScopeXto getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link PreferenceScopeXto }
     *     
     */
    public void setScope(PreferenceScopeXto value) {
        this.scope = value;
    }

    /**
     * Gets the value of the moduleId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * Sets the value of the moduleId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModuleId(String value) {
        this.moduleId = value;
    }

    /**
     * Gets the value of the preferencesId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreferencesId() {
        return preferencesId;
    }

    /**
     * Sets the value of the preferencesId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreferencesId(String value) {
        this.preferencesId = value;
    }

}
