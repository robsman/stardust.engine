
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
 *         &lt;element name="processDefinitions" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessDefinitionQueryResult"/>
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
    "processDefinitions"
})
@XmlRootElement(name = "findProcessDefinitionsResponse")
public class FindProcessDefinitionsResponse {

    @XmlElement(required = true, nillable = true)
    protected ProcessDefinitionQueryResultXto processDefinitions;

    /**
     * Gets the value of the processDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinitionQueryResultXto }
     *     
     */
    public ProcessDefinitionQueryResultXto getProcessDefinitions() {
        return processDefinitions;
    }

    /**
     * Sets the value of the processDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinitionQueryResultXto }
     *     
     */
    public void setProcessDefinitions(ProcessDefinitionQueryResultXto value) {
        this.processDefinitions = value;
    }

}
