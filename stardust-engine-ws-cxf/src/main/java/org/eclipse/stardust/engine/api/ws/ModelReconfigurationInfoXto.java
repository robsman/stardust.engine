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
 * 				The ModelReconfigurationInfo class is used to receive information about a model reconfiguration operation.
 * 				Model reconfiguration operations are all operations which modifies the models in audit trail, their attributes or behavior,
 * 				e.g. model deployment, configuration variable modification.
 * 			
 * 
 * <p>Java class for ModelReconfigurationInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModelReconfigurationInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="errors" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="warnings" type="{http://eclipse.org/stardust/ws/v2012a/api}Inconsistency" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelReconfigurationInfo", propOrder = {
    "modelOid",
    "id",
    "errors",
    "warnings"
})
public class ModelReconfigurationInfoXto {

    protected int modelOid;
    @XmlElement(required = true)
    protected String id;
    protected List<InconsistencyXto> errors;
    protected List<InconsistencyXto> warnings;

    /**
     * Gets the value of the modelOid property.
     * 
     */
    public int getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     */
    public void setModelOid(int value) {
        this.modelOid = value;
    }

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
     * Gets the value of the errors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InconsistencyXto }
     * 
     * 
     */
    public List<InconsistencyXto> getErrors() {
        if (errors == null) {
            errors = new ArrayList<InconsistencyXto>();
        }
        return this.errors;
    }

    /**
     * Gets the value of the warnings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warnings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarnings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InconsistencyXto }
     * 
     * 
     */
    public List<InconsistencyXto> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<InconsistencyXto>();
        }
        return this.warnings;
    }

}
