
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
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="bindingInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}ProcessEventBinding"/>
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
    "processOid",
    "bindingInfo"
})
@XmlRootElement(name = "createProcessEventBinding")
public class CreateProcessEventBinding {

    protected long processOid;
    @XmlElement(required = true, nillable = true)
    protected ProcessEventBindingXto bindingInfo;

    /**
     * Gets the value of the processOid property.
     * 
     */
    public long getProcessOid() {
        return processOid;
    }

    /**
     * Sets the value of the processOid property.
     * 
     */
    public void setProcessOid(long value) {
        this.processOid = value;
    }

    /**
     * Gets the value of the bindingInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessEventBindingXto }
     *     
     */
    public ProcessEventBindingXto getBindingInfo() {
        return bindingInfo;
    }

    /**
     * Sets the value of the bindingInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessEventBindingXto }
     *     
     */
    public void setBindingInfo(ProcessEventBindingXto value) {
        this.bindingInfo = value;
    }

}
