
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
 * <p>Java-Klasse für Model complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der alive-Eigenschaft ab.
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
     * Legt den Wert der alive-Eigenschaft fest.
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
     * Ruft den Wert der roles-Eigenschaft ab.
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
     * Legt den Wert der roles-Eigenschaft fest.
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
     * Ruft den Wert der organizations-Eigenschaft ab.
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
     * Legt den Wert der organizations-Eigenschaft fest.
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
     * Ruft den Wert der globalVariables-Eigenschaft ab.
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
     * Legt den Wert der globalVariables-Eigenschaft fest.
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
     * Ruft den Wert der processes-Eigenschaft ab.
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
     * Legt den Wert der processes-Eigenschaft fest.
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
     * Ruft den Wert der typeDeclarations-Eigenschaft ab.
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
     * Legt den Wert der typeDeclarations-Eigenschaft fest.
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
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
