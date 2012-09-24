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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Holds a List of ConfigurationVariables belonging to a specific modelId
 * 			
 * 
 * <p>Java class for ConfigurationVariables complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConfigurationVariables">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="configurationVariable" type="{http://eclipse.org/stardust/ws/v2012a/api}ConfigurationVariable" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfigurationVariables", propOrder = {
    "modelId",
    "configurationVariable"
})
public class ConfigurationVariablesXto {

    @XmlElement(required = true)
    protected String modelId;
    protected List<ConfigurationVariableXto> configurationVariable;

    /**
     * Gets the value of the modelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the value of the modelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelId(String value) {
        this.modelId = value;
    }

    /**
     * Gets the value of the configurationVariable property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the configurationVariable property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConfigurationVariable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationVariableXto }
     * 
     * 
     */
    public List<ConfigurationVariableXto> getConfigurationVariable() {
        if (configurationVariable == null) {
            configurationVariable = new ArrayList<ConfigurationVariableXto>();
        }
        return this.configurationVariable;
    }

}
