
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Holds basic information of a document.
 * 			
 * 
 * <p>Java-Klasse für DocumentInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DocumentInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}ResourceInfo">
 *       &lt;sequence>
 *         &lt;element name="contentType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="documentType" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentInfo", propOrder = {
    "contentType",
    "documentType"
})
@XmlSeeAlso({
    DocumentXto.class
})
public class DocumentInfoXto
    extends ResourceInfoXto
{

    @XmlElement(required = true)
    protected String contentType;
    protected DocumentTypeXto documentType;

    /**
     * Ruft den Wert der contentType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Legt den Wert der contentType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Ruft den Wert der documentType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DocumentTypeXto }
     *     
     */
    public DocumentTypeXto getDocumentType() {
        return documentType;
    }

    /**
     * Legt den Wert der documentType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentTypeXto }
     *     
     */
    public void setDocumentType(DocumentTypeXto value) {
        this.documentType = value;
    }

}
