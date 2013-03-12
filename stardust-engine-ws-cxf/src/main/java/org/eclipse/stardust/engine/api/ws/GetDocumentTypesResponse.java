
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
 *         &lt;element name="documentTypeResults" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentTypeResults"/>
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
    "documentTypeResults"
})
@XmlRootElement(name = "getDocumentTypesResponse")
public class GetDocumentTypesResponse {

    @XmlElement(required = true, nillable = true)
    protected DocumentTypeResultsXto documentTypeResults;

    /**
     * Gets the value of the documentTypeResults property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentTypeResultsXto }
     *     
     */
    public DocumentTypeResultsXto getDocumentTypeResults() {
        return documentTypeResults;
    }

    /**
     * Sets the value of the documentTypeResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentTypeResultsXto }
     *     
     */
    public void setDocumentTypeResults(DocumentTypeResultsXto value) {
        this.documentTypeResults = value;
    }

}
