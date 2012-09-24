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
 * <p>Java class for Grants complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Grants">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="grant" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                   &lt;element name="elementOid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *                   &lt;element name="department" type="{http://eclipse.org/stardust/ws/v2012a/api}Department" minOccurs="0"/>
 *                   &lt;element name="organization" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="superGrants" type="{http://eclipse.org/stardust/ws/v2012a/api}Grants" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Grants", propOrder = {
    "grant"
})
public class GrantsXto {

    protected List<GrantsXto.GrantXto> grant;

    /**
     * Gets the value of the grant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the grant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GrantsXto.GrantXto }
     * 
     * 
     */
    public List<GrantsXto.GrantXto> getGrant() {
        if (grant == null) {
            grant = new ArrayList<GrantsXto.GrantXto>();
        }
        return this.grant;
    }


    /**
     * 
     * 			       The 'grant' represents a permission granted to a user to perform as a specific participant (role or organization) of a model.
     * 			       
     * 
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *         &lt;element name="elementOid" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
     *         &lt;element name="department" type="{http://eclipse.org/stardust/ws/v2012a/api}Department" minOccurs="0"/>
     *         &lt;element name="organization" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="superGrants" type="{http://eclipse.org/stardust/ws/v2012a/api}Grants" minOccurs="0"/>
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
        "id",
        "name",
        "modelOid",
        "elementOid",
        "department",
        "organization",
        "superGrants"
    })
    public static class GrantXto {

        @XmlElement(required = true)
        protected String id;
        protected String name;
        protected Integer modelOid;
        protected Integer elementOid;
        protected DepartmentXto department;
        protected Boolean organization;
        protected GrantsXto superGrants;

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
         * Gets the value of the modelOid property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getModelOid() {
            return modelOid;
        }

        /**
         * Sets the value of the modelOid property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setModelOid(Integer value) {
            this.modelOid = value;
        }

        /**
         * Gets the value of the elementOid property.
         * 
         * @return
         *     possible object is
         *     {@link Integer }
         *     
         */
        public Integer getElementOid() {
            return elementOid;
        }

        /**
         * Sets the value of the elementOid property.
         * 
         * @param value
         *     allowed object is
         *     {@link Integer }
         *     
         */
        public void setElementOid(Integer value) {
            this.elementOid = value;
        }

        /**
         * Gets the value of the department property.
         * 
         * @return
         *     possible object is
         *     {@link DepartmentXto }
         *     
         */
        public DepartmentXto getDepartment() {
            return department;
        }

        /**
         * Sets the value of the department property.
         * 
         * @param value
         *     allowed object is
         *     {@link DepartmentXto }
         *     
         */
        public void setDepartment(DepartmentXto value) {
            this.department = value;
        }

        /**
         * Gets the value of the organization property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isOrganization() {
            return organization;
        }

        /**
         * Sets the value of the organization property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setOrganization(Boolean value) {
            this.organization = value;
        }

        /**
         * Gets the value of the superGrants property.
         * 
         * @return
         *     possible object is
         *     {@link GrantsXto }
         *     
         */
        public GrantsXto getSuperGrants() {
            return superGrants;
        }

        /**
         * Sets the value of the superGrants property.
         * 
         * @param value
         *     allowed object is
         *     {@link GrantsXto }
         *     
         */
        public void setSuperGrants(GrantsXto value) {
            this.superGrants = value;
        }

    }

}
