
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
 *         &lt;element name="processInstances" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessInstanceQueryResult"/>
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
    "processInstances"
})
@XmlRootElement(name = "findProcessesResponse")
public class FindProcessesResponse {

    @XmlElement(required = true, nillable = true)
    protected ProcessInstanceQueryResultXto processInstances;

    /**
     * Gets the value of the processInstances property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessInstanceQueryResultXto }
     *     
     */
    public ProcessInstanceQueryResultXto getProcessInstances() {
        return processInstances;
    }

    /**
     * Sets the value of the processInstances property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessInstanceQueryResultXto }
     *     
     */
    public void setProcessInstances(ProcessInstanceQueryResultXto value) {
        this.processInstances = value;
    }

}
