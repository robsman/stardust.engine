
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
 *         &lt;element name="participantId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
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
    "participantId",
    "modelOid"
})
@XmlRootElement(name = "getParticipant")
public class GetParticipant {

    @XmlElement(required = true, nillable = true)
    protected String participantId;
    @XmlElement(required = true, type = Long.class, nillable = true)
    protected Long modelOid;

    /**
     * Gets the value of the participantId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParticipantId() {
        return participantId;
    }

    /**
     * Sets the value of the participantId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParticipantId(String value) {
        this.participantId = value;
    }

    /**
     * Gets the value of the modelOid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getModelOid() {
        return modelOid;
    }

    /**
     * Sets the value of the modelOid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setModelOid(Long value) {
        this.modelOid = value;
    }

}
