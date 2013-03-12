
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
 *         &lt;element name="downloadToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "downloadToken"
})
@XmlRootElement(name = "requestDocumentContentDownloadResponse")
public class RequestDocumentContentDownloadResponse {

    @XmlElement(required = true, nillable = true)
    protected String downloadToken;

    /**
     * Gets the value of the downloadToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownloadToken() {
        return downloadToken;
    }

    /**
     * Sets the value of the downloadToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownloadToken(String value) {
        this.downloadToken = value;
    }

}
