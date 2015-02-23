
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Contains only important core information of a model participant that is needed to identify a ModelParticipant.
 * 	        
 * 
 * <p>Java-Klasse für ModelParticipantInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ModelParticipantInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo">
 *       &lt;sequence>
 *         &lt;element name="runtimeElementOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="departmentScoped" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="definesDepartmentScope" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="department" type="{http://eclipse.org/stardust/ws/v2012a/api}DepartmentInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelParticipantInfo", propOrder = {
    "runtimeElementOid",
    "departmentScoped",
    "definesDepartmentScope",
    "department"
})
@XmlSeeAlso({
    OrganizationInfoXto.class,
    RoleInfoXto.class,
    ConditionalPerformerInfoXto.class
})
public class ModelParticipantInfoXto
    extends ParticipantInfoXto
{

    protected long runtimeElementOid;
    protected boolean departmentScoped;
    protected boolean definesDepartmentScope;
    protected DepartmentInfoXto department;

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

}
