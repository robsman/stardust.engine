
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="logType" type="{http://eclipse.org/stardust/ws/v2012a/api}LogType"/>
 *         &lt;element name="activityOid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="processOid" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "logType",
    "activityOid",
    "processOid",
    "message"
})
@XmlRootElement(name = "writeLogEntry")
public class WriteLogEntry {

    @XmlElement(required = true, nillable = true)
    protected LogTypeXto logType;
    @XmlElementRef(name = "activityOid", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Long> activityOid;
    @XmlElementRef(name = "processOid", namespace = "http://eclipse.org/stardust/ws/v2012a/api", type = JAXBElement.class)
    protected JAXBElement<Long> processOid;
    @XmlElement(required = true, nillable = true)
    protected String message;

    /**
     * Gets the value of the logType property.
     * 
     * @return
     *     possible object is
     *     {@link LogTypeXto }
     *     
     */
    public LogTypeXto getLogType() {
        return logType;
    }

    /**
     * Sets the value of the logType property.
     * 
     * @param value
     *     allowed object is
     *     {@link LogTypeXto }
     *     
     */
    public void setLogType(LogTypeXto value) {
        this.logType = value;
    }

    /**
     * Gets the value of the activityOid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getActivityOid() {
        return activityOid;
    }

    /**
     * Sets the value of the activityOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setActivityOid(JAXBElement<Long> value) {
        this.activityOid = ((JAXBElement<Long> ) value);
    }

    /**
     * Gets the value of the processOid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getProcessOid() {
        return processOid;
    }

    /**
     * Sets the value of the processOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setProcessOid(JAXBElement<Long> value) {
        this.processOid = ((JAXBElement<Long> ) value);
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

}
