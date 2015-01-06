
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
 *         &lt;element name="worklist" type="{http://eclipse.org/stardust/ws/v2012a/api}Worklist"/>
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
    "worklist"
})
@XmlRootElement(name = "findWorklistResponse")
public class FindWorklistResponse {

    @XmlElement(required = true, nillable = true)
    protected WorklistXto worklist;

    /**
     * Gets the value of the worklist property.
     * 
     * @return
     *     possible object is
     *     {@link WorklistXto }
     *     
     */
    public WorklistXto getWorklist() {
        return worklist;
    }

    /**
     * Sets the value of the worklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorklistXto }
     *     
     */
    public void setWorklist(WorklistXto value) {
        this.worklist = value;
    }

}
