
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
 * <p>Java-Klasse für ModelParticipant complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der departmentScoped-Eigenschaft ab.
     * 
     */
    public boolean isDepartmentScoped() {
        return departmentScoped;
    }

    /**
     * Legt den Wert der departmentScoped-Eigenschaft fest.
     * 
     */
    public void setDepartmentScoped(boolean value) {
        this.departmentScoped = value;
    }

    /**
     * Ruft den Wert der definesDepartmentScope-Eigenschaft ab.
     * 
     */
    public boolean isDefinesDepartmentScope() {
        return definesDepartmentScope;
    }

    /**
     * Legt den Wert der definesDepartmentScope-Eigenschaft fest.
     * 
     */
    public void setDefinesDepartmentScope(boolean value) {
        this.definesDepartmentScope = value;
    }

    /**
     * Ruft den Wert der department-Eigenschaft ab.
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
     * Legt den Wert der department-Eigenschaft fest.
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
     * Ruft den Wert der runtimeElementOid-Eigenschaft ab.
     * 
     */
    public long getRuntimeElementOid() {
        return runtimeElementOid;
    }

    /**
     * Legt den Wert der runtimeElementOid-Eigenschaft fest.
     * 
     */
    public void setRuntimeElementOid(long value) {
        this.runtimeElementOid = value;
    }

    /**
     * Ruft den Wert der allSuperOrganizations-Eigenschaft ab.
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
     * Legt den Wert der allSuperOrganizations-Eigenschaft fest.
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
