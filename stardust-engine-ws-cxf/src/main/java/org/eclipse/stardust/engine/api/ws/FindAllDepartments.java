
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
    "organization"
})
@XmlRootElement(name = "findAllDepartments")
public class FindAllDepartments {

    @XmlElement(required = true, nillable = true)
    protected DepartmentInfoXto parent;
    @XmlElement(required = true, nillable = true)
    protected OrganizationInfoXto organization;

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

}
