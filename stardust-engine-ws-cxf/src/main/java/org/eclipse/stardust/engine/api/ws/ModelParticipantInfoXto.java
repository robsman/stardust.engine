
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
 * <p>Java class for ModelParticipantInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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

}
