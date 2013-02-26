
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *  			The Department represents a snapshot of the department state.
 *  			It contains information about the parent Department and the linked organization.
 * 	        
 * 
 * <p>Java class for Department complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Department">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}DepartmentInfo">
 *       &lt;sequence>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parentDepartment" type="{http://eclipse.org/stardust/ws/v2012a/api}Department" minOccurs="0"/>
 *         &lt;element name="organization" type="{http://eclipse.org/stardust/ws/v2012a/api}Organization"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Department", propOrder = {
    "description",
    "parentDepartment",
    "organization"
})
public class DepartmentXto
    extends DepartmentInfoXto
{

    @XmlElement(required = true)
    protected String description;
    protected DepartmentXto parentDepartment;
    @XmlElement(required = true)
    protected OrganizationXto organization;

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
     * Gets the value of the parentDepartment property.
     * 
     * @return
     *     possible object is
     *     {@link DepartmentXto }
     *     
     */
    public DepartmentXto getParentDepartment() {
        return parentDepartment;
    }

    /**
     * Sets the value of the parentDepartment property.
     * 
     * @param value
     *     allowed object is
     *     {@link DepartmentXto }
     *     
     */
    public void setParentDepartment(DepartmentXto value) {
        this.parentDepartment = value;
    }

    /**
     * Gets the value of the organization property.
     * 
     * @return
     *     possible object is
     *     {@link OrganizationXto }
     *     
     */
    public OrganizationXto getOrganization() {
        return organization;
    }

    /**
     * Sets the value of the organization property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrganizationXto }
     *     
     */
    public void setOrganization(OrganizationXto value) {
        this.organization = value;
    }

}
