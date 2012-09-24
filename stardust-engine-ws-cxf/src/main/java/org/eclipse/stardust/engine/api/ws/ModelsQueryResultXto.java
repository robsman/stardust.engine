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
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Result of an DeployedModelQuery execution.
 * 			
 * 
 * <p>Java class for ModelsQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModelsQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="deployedModels" type="{http://eclipse.org/stardust/ws/v2012a/api}Models" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelsQueryResult", propOrder = {
    "deployedModels"
})
public class ModelsQueryResultXto
    extends QueryResultXto
{

    protected ModelsXto deployedModels;

    /**
     * Gets the value of the deployedModels property.
     * 
     * @return
     *     possible object is
     *     {@link ModelsXto }
     *     
     */
    public ModelsXto getDeployedModels() {
        return deployedModels;
    }

    /**
     * Sets the value of the deployedModels property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelsXto }
     *     
     */
    public void setDeployedModels(ModelsXto value) {
        this.deployedModels = value;
    }

}
