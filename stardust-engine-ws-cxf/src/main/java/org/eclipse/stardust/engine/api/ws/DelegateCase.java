
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
 *         &lt;element name="caseOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="participantInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}ParticipantInfo"/>
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
    "caseOid",
    "participantInfo"
})
@XmlRootElement(name = "delegateCase")
public class DelegateCase {

    protected long caseOid;
    @XmlElement(required = true)
    protected ParticipantInfoXto participantInfo;

    /**
     * Gets the value of the caseOid property.
     * 
     */
    public long getCaseOid() {
        return caseOid;
    }

    /**
     * Sets the value of the caseOid property.
     * 
     */
    public void setCaseOid(long value) {
        this.caseOid = value;
    }

    /**
     * Gets the value of the participantInfo property.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public ParticipantInfoXto getParticipantInfo() {
        return participantInfo;
    }

    /**
     * Sets the value of the participantInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantInfoXto }
     *     
     */
    public void setParticipantInfo(ParticipantInfoXto value) {
        this.participantInfo = value;
    }

}
