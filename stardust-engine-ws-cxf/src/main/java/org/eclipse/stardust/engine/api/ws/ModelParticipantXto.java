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
 * 	 	   A client side view of a workflow participant defined in a workflow model.
 *            A participant is a workflow element which performs manual or interactive activities.
 * 	       
 * 
 * <p>Java class for ModelParticipant complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ModelParticipant">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelElement">
 *       &lt;sequence>
 *         &lt;element name="departmentScoped" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="definesDepartmentScope" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="department" type="{http://eclipse.org/stardust/ws/v2012a/api}DepartmentInfo" minOccurs="0"/>
 *         &lt;element name="runtimeElementOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="allSuperOrganizations" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelParticipant", propOrder = {
    "departmentScoped",
    "definesDepartmentScope",
    "department",
    "runtimeElementOid",
    "allSuperOrganizations"
})
@XmlSeeAlso({
    RoleXto.class,
    OrganizationXto.class
})
public class ModelParticipantXto
    extends ModelElementXto
{

    protected boolean departmentScoped;
    protected boolean definesDepartmentScope;
    protected DepartmentInfoXto department;
    protected long runtimeElementOid;
    protected OrganizationsXto allSuperOrganizations;

    /**
     * Gets the value of the departmentScoped property.
     * 
     */
    public boolean isDepartmentScoped() {
        return departmentScoped;
    }

    /**
     * Sets the value of the departmentScoped property.
     * 
     */
    public void setDepartmentScoped(boolean value) {
        this.departmentScoped = value;
    }

    /**
     * Gets the value of the definesDepartmentScope property.
     * 
     */
    public boolean isDefinesDepartmentScope() {
        return definesDepartmentScope;
    }

    /**
     * Sets the value of the definesDepartmentScope property.
     * 
     */
    public void setDefinesDepartmentScope(boolean value) {
        this.definesDepartmentScope = value;
    }

    /**
     * Gets the value of the department property.
     * 
     * @return
     *     possible object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public DepartmentInfoXto getDepartment() {
        return department;
    }

    /**
     * Sets the value of the department property.
     * 
     * @param value
     *     allowed object is
     *     {@link DepartmentInfoXto }
     *     
     */
    public void setDepartment(DepartmentInfoXto value) {
        this.department = value;
    }

    /**
     * Gets the value of the runtimeElementOid property.
     * 
     */
    public long getRuntimeElementOid() {
        return runtimeElementOid;
    }

    /**
     * Sets the value of the runtimeElementOid property.
     * 
     */
    public void setRuntimeElementOid(long value) {
        this.runtimeElementOid = value;
    }

    /**
     * Gets the value of the allSuperOrganizations property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getAllSuperOrganizations() {
        return allSuperOrganizations;
    }

    /**
     * Sets the value of the allSuperOrganizations property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setAllSuperOrganizations(OrganizationsXto value) {
        this.allSuperOrganizations = value;
    }

}
