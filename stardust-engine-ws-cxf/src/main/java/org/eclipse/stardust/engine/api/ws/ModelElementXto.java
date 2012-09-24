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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *             Abstract element containing base data for all model elements
 * 			
 * 
 * <p>Java class for ModelElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModelElement">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partitionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partitionOid" type="{http://www.w3.org/2001/XMLSchema}short"/>
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
@XmlType(name = "ModelElement", propOrder = {
    "modelOid",
    "id",
    "name",
    "description",
    "partitionId",
    "partitionOid",
    "attributes"
})
@XmlSeeAlso({
    ProcessDefinitionXto.class,
    ModelDescriptionXto.class,
    InteractionContextXto.class,
    DataPathXto.class,
    ModelParticipantXto.class,
    TypeDeclarationXto.class,
    EventHandlerDefinitionXto.class,
    DeployedModelDescriptionXto.class,
    ActivityDefinitionXto.class,
    ApplicationXto.class,
    DataFlowXto.class,
    TriggerXto.class,
    ParameterMappingXto.class,
    VariableDefinitionXto.class,
    EventActionDefinitionXto.class
})
public class ModelElementXto {

    protected long modelOid;
    protected String id;
    protected String name;
    protected String description;
    protected String partitionId;
    protected short partitionOid;
    protected AttributesXto attributes;

    /**
     * Gets the value of the modelOid property.
     * 
     */
    public long getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     */
    public void setModelOid(long value) {
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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the partitionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Sets the value of the partitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartitionId(String value) {
        this.partitionId = value;
    }

    /**
     * Gets the value of the partitionOid property.
     * 
     */
    public short getPartitionOid() {
        return partitionOid;
    }

    /**
     * Sets the value of the partitionOid property.
     * 
     */
    public void setPartitionOid(short value) {
        this.partitionOid = value;
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
