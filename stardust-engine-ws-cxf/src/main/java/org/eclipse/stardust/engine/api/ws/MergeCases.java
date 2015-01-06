
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
 *         &lt;element name="targetCaseOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="sourceCaseOids" type="{http://eclipse.org/stardust/ws/v2012a/api}OidList"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "targetCaseOid",
    "sourceCaseOids",
    "comment"
})
@XmlRootElement(name = "mergeCases")
public class MergeCases {

    protected long targetCaseOid;
    @XmlElement(required = true)
    protected OidListXto sourceCaseOids;
    @XmlElement(required = true)
    protected String comment;

    /**
     * Gets the value of the targetCaseOid property.
     * 
     */
    public long getTargetCaseOid() {
        return targetCaseOid;
    }

    /**
     * Sets the value of the targetCaseOid property.
     * 
     */
    public void setTargetCaseOid(long value) {
        this.targetCaseOid = value;
    }

    /**
     * Gets the value of the sourceCaseOids property.
     * 
     * @return
     *     possible object is
     *     {@link OidListXto }
     *     
     */
    public OidListXto getSourceCaseOids() {
        return sourceCaseOids;
    }

    /**
     * Sets the value of the sourceCaseOids property.
     * 
     * @param value
     *     allowed object is
     *     {@link OidListXto }
     *     
     */
    public void setSourceCaseOids(OidListXto value) {
        this.sourceCaseOids = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
