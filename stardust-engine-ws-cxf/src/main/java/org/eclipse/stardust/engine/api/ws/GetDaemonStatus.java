
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="daemonParameters" type="{http://eclipse.org/stardust/ws/v2012a/api}DaemonParameters" minOccurs="0"/>
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
    "daemonParameters"
})
@XmlRootElement(name = "getDaemonStatus")
public class GetDaemonStatus {

    @XmlElementRef(name = "daemonParameters", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<DaemonParametersXto> daemonParameters;

    /**
     * Gets the value of the daemonParameters property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DaemonParametersXto }{@code >}
     *     
     */
    public JAXBElement<DaemonParametersXto> getDaemonParameters() {
        return daemonParameters;
    }

    /**
     * Sets the value of the daemonParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DaemonParametersXto }{@code >}
     *     
     */
    public void setDaemonParameters(JAXBElement<DaemonParametersXto> value) {
        this.daemonParameters = ((JAXBElement<DaemonParametersXto> ) value);
    }

}
