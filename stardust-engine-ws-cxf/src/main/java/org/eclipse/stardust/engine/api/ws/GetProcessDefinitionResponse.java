
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
 *         &lt;element name="processDefinition" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinition"/>
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
    "processDefinition"
})
@XmlRootElement(name = "getProcessDefinitionResponse")
public class GetProcessDefinitionResponse {

    @XmlElement(required = true, nillable = true)
    protected ProcessDefinitionXto processDefinition;

    /**
     * Gets the value of the processDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionXto }
     *     
     */
    public ProcessDefinitionXto getProcessDefinition() {
        return processDefinition;
    }

    /**
     * Sets the value of the processDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionXto }
     *     
     */
    public void setProcessDefinition(ProcessDefinitionXto value) {
        this.processDefinition = value;
    }

}
