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
 *         &lt;element name="deploymentInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}DeploymentInfo"/>
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
    "deploymentInfo"
})
@XmlRootElement(name = "deployModelResponse")
public class DeployModelResponse {

    @XmlElement(required = true, nillable = true)
    protected DeploymentInfoXto deploymentInfo;

    /**
     * Gets the value of the deploymentInfo property.
     * 
     * @return
     *     possible object is
     *     {@link DeploymentInfoXto }
     *     
     */
    public DeploymentInfoXto getDeploymentInfo() {
        return deploymentInfo;
    }

    /**
     * Sets the value of the deploymentInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeploymentInfoXto }
     *     
     */
    public void setDeploymentInfo(DeploymentInfoXto value) {
        this.deploymentInfo = value;
    }

}
