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
 * 			Result of an ProcessDefinitionQuery execution.
 * 			
 * 
 * <p>Java class for ProcessDefinitionQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessDefinitionQueryResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}QueryResult">
 *       &lt;sequence>
 *         &lt;element name="processDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinitions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinitionQueryResult", propOrder = {
    "processDefinitions"
})
public class ProcessDefinitionQueryResultXto
    extends QueryResultXto
{

    protected ProcessDefinitionsXto processDefinitions;

    /**
     * Gets the value of the processDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionsXto }
     *     
     */
    public ProcessDefinitionsXto getProcessDefinitions() {
        return processDefinitions;
    }

    /**
     * Sets the value of the processDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionsXto }
     *     
     */
    public void setProcessDefinitions(ProcessDefinitionsXto value) {
        this.processDefinitions = value;
    }

}
