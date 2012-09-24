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
 * 	 	   A client side view of a workflow model.
 * 		   Contains information about the model as well as sub elements such as processes, roles, organisations etc.
 * 	       
 * 
 * <p>Java class for Model complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Model">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ModelDescription">
 *       &lt;sequence>
 *         &lt;element name="alive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="roles" type="{http://eclipse.org/stardust/ws/v2012a/api}Roles" minOccurs="0"/>
 *         &lt;element name="organizations" type="{http://eclipse.org/stardust/ws/v2012a/api}Organizations" minOccurs="0"/>
 *         &lt;element name="globalVariables">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="globalVariable" type="{http://eclipse.org/stardust/ws/v2012a/api}VariableDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="processes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="process" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="typeDeclarations" type="{http://eclipse.org/stardust/ws/v2012a/api}TypeDeclarations" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Model", propOrder = {
    "alive",
    "roles",
    "organizations",
    "globalVariables",
    "processes",
    "typeDeclarations"
})
public class ModelXto
    extends ModelDescriptionXto
{

    protected Boolean alive;
    protected RolesXto roles;
    protected OrganizationsXto organizations;
    @XmlElement(required = true)
    protected ModelXto.GlobalVariablesXto globalVariables;
    @XmlElement(required = true)
    protected ModelXto.ProcessesXto processes;
    protected TypeDeclarationsXto typeDeclarations;

    /**
     * Gets the value of the alive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAlive() {
        return alive;
    }

    /**
     * Sets the value of the alive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlive(Boolean value) {
        this.alive = value;
    }

    /**
     * Gets the value of the roles property.
     * 
     * @return
     *     possible object is
     *     {@link RolesXto }
     *     
     */
    public RolesXto getRoles() {
        return roles;
    }

    /**
     * Sets the value of the roles property.
     * 
     * @param value
     *     allowed object is
     *     {@link RolesXto }
     *     
     */
    public void setRoles(RolesXto value) {
        this.roles = value;
    }

    /**
     * Gets the value of the organizations property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationsXto }
     *     
     */
    public OrganizationsXto getOrganizations() {
        return organizations;
    }

    /**
     * Sets the value of the organizations property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationsXto }
     *     
     */
    public void setOrganizations(OrganizationsXto value) {
        this.organizations = value;
    }

    /**
     * Gets the value of the globalVariables property.
     * 
     * @return
     *     possible object is
     *     {@link ModelXto.GlobalVariablesXto }
     *     
     */
    public ModelXto.GlobalVariablesXto getGlobalVariables() {
        return globalVariables;
    }

    /**
     * Sets the value of the globalVariables property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelXto.GlobalVariablesXto }
     *     
     */
    public void setGlobalVariables(ModelXto.GlobalVariablesXto value) {
        this.globalVariables = value;
    }

    /**
     * Gets the value of the processes property.
     * 
     * @return
     *     possible object is
     *     {@link ModelXto.ProcessesXto }
     *     
     */
    public ModelXto.ProcessesXto getProcesses() {
        return processes;
    }

    /**
     * Sets the value of the processes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModelXto.ProcessesXto }
     *     
     */
    public void setProcesses(ModelXto.ProcessesXto value) {
        this.processes = value;
    }

    /**
     * Gets the value of the typeDeclarations property.
     * 
     * @return
     *     possible object is
     *     {@link TypeDeclarationsXto }
     *     
     */
    public TypeDeclarationsXto getTypeDeclarations() {
        return typeDeclarations;
    }

    /**
     * Sets the value of the typeDeclarations property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeDeclarationsXto }
     *     
     */
    public void setTypeDeclarations(TypeDeclarationsXto value) {
        this.typeDeclarations = value;
    }


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
     *         &lt;element name="globalVariable" type="{http://eclipse.org/stardust/ws/v2012a/api}VariableDefinition" maxOccurs="unbounded" minOccurs="0"/>
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
        "globalVariable"
    })
    public static class GlobalVariablesXto {

        protected List<VariableDefinitionXto> globalVariable;

        /**
         * Gets the value of the globalVariable property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the globalVariable property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getGlobalVariable().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link VariableDefinitionXto }
         * 
         * 
         */
        public List<VariableDefinitionXto> getGlobalVariable() {
            if (globalVariable == null) {
                globalVariable = new ArrayList<VariableDefinitionXto>();
            }
            return this.globalVariable;
        }

    }


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
     *         &lt;element name="process" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinition" maxOccurs="unbounded" minOccurs="0"/>
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
        "process"
    })
    public static class ProcessesXto {

        protected List<ProcessDefinitionXto> process;

        /**
         * Gets the value of the process property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the process property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProcess().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ProcessDefinitionXto }
         * 
         * 
         */
        public List<ProcessDefinitionXto> getProcess() {
            if (process == null) {
                process = new ArrayList<ProcessDefinitionXto>();
            }
            return this.process;
        }

    }

}
