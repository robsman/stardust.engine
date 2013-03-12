
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
 *         &lt;element name="departments" type="{http://eclipse.org/stardust/ws/v2012a/api}Departments"/>
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
    "departments"
})
@XmlRootElement(name = "findAllDepartmentsResponse")
public class FindAllDepartmentsResponse {

    @XmlElement(required = true, nillable = true)
    protected DepartmentsXto departments;

    /**
     * Gets the value of the departments property.
     * 
     * @return
     *     possible object is
     *     {@link DepartmentsXto }
     *     
     */
    public DepartmentsXto getDepartments() {
        return departments;
    }

    /**
     * Sets the value of the departments property.
     * 
     * @param value
     *     allowed object is
     *     {@link DepartmentsXto }
     *     
     */
    public void setDepartments(DepartmentsXto value) {
        this.departments = value;
    }

}
