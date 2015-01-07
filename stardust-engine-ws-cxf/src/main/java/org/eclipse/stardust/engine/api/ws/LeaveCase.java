
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
 *         &lt;element name="memberOids" type="{http://eclipse.org/stardust/ws/v2012a/api}OidList"/>
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
    "memberOids"
})
@XmlRootElement(name = "leaveCase")
public class LeaveCase {

    protected long caseOid;
    @XmlElement(required = true)
    protected OidListXto memberOids;

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
     * Gets the value of the memberOids property.
     * 
     * @return
     *     possible object is
     *     {@link OidListXto }
     *     
     */
    public OidListXto getMemberOids() {
        return memberOids;
    }

    /**
     * Sets the value of the memberOids property.
     * 
     * @param value
     *     allowed object is
     *     {@link OidListXto }
     *     
     */
    public void setMemberOids(OidListXto value) {
        this.memberOids = value;
    }

}
