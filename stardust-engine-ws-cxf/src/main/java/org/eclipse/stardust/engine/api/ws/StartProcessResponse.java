
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
 *         &lt;element name="processInstance" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstance"/>
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
    "processInstance"
})
@XmlRootElement(name = "startProcessResponse")
public class StartProcessResponse {

    @XmlElement(required = true, nillable = true)
    protected ProcessInstanceXto processInstance;

    /**
     * Gets the value of the processInstance property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceXto }
     *     
     */
    public ProcessInstanceXto getProcessInstance() {
        return processInstance;
    }

    /**
     * Sets the value of the processInstance property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceXto }
     *     
     */
    public void setProcessInstance(ProcessInstanceXto value) {
        this.processInstance = value;
    }

}
