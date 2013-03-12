
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
 *         &lt;element name="documentQuery" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentQuery"/>
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
    "documentQuery"
})
@XmlRootElement(name = "findDocuments")
public class FindDocuments {

    @XmlElement(required = true, nillable = true)
    protected DocumentQueryXto documentQuery;

    /**
     * Gets the value of the documentQuery property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentQueryXto }
     *     
     */
    public DocumentQueryXto getDocumentQuery() {
        return documentQuery;
    }

    /**
     * Sets the value of the documentQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentQueryXto }
     *     
     */
    public void setDocumentQuery(DocumentQueryXto value) {
        this.documentQuery = value;
    }

}
