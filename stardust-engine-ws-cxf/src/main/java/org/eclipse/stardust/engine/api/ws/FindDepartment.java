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
 *         &lt;element name="parent" type="{http://eclipse.org/stardust/ws/v2012a/api}DepartmentInfo"/>
 *         &lt;element name="organization" type="{http://eclipse.org/stardust/ws/v2012a/api}OrganizationInfo"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "parent",
    "organization",
    "id"
})
@XmlRootElement(name = "findDepartment")
public class FindDepartment {

    @XmlElement(required = true, nillable = true)
    protected DepartmentInfoXto parent;
    @XmlElement(required = true, nillable = true)
    protected OrganizationInfoXto organization;
    @XmlElement(required = true, nillable = true)
    protected String id;

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public DepartmentInfoXto getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public void setParent(DepartmentInfoXto value) {
        this.parent = value;
    }

    /**
     * Gets the value of the organization property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationInfoXto }
     *     
     */
    public OrganizationInfoXto getOrganization() {
        return organization;
    }

    /**
     * Sets the value of the organization property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationInfoXto }
     *     
     */
    public void setOrganization(OrganizationInfoXto value) {
        this.organization = value;
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

}
