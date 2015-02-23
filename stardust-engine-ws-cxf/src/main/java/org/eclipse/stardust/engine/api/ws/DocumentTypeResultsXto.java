
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für DocumentTypeResults complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DocumentTypeResults">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="documentTypeResult" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="modelId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *                   &lt;element name="documentTypes" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentTypes"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentTypeResults", propOrder = {
    "documentTypeResult"
})
public class DocumentTypeResultsXto {

    protected List<DocumentTypeResultsXto.DocumentTypeResultXto> documentTypeResult;

    /**
     * Gets the value of the documentTypeResult property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the documentTypeResult property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocumentTypeResult().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DocumentTypeResultsXto.DocumentTypeResultXto }
     * 
     * 
     */
    public List<DocumentTypeResultsXto.DocumentTypeResultXto> getDocumentTypeResult() {
        if (documentTypeResult == null) {
            documentTypeResult = new ArrayList<DocumentTypeResultsXto.DocumentTypeResultXto>();
        }
        return this.documentTypeResult;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="modelId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="modelOid" type="{http://www.w3.org/2001/XMLSchema}long"/>
     *         &lt;element name="documentTypes" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentTypes"/>
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
        "modelId",
        "modelOid",
        "documentTypes"
    })
    public static class DocumentTypeResultXto {

        @XmlElement(required = true)
        protected String modelId;
        protected long modelOid;
        @XmlElement(required = true, nillable = true)
        protected DocumentTypesXto documentTypes;

        /**
         * Ruft den Wert der modelId-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getModelId() {
            return modelId;
        }

        /**
         * Legt den Wert der modelId-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setModelId(String value) {
            this.modelId = value;
        }

        /**
         * Ruft den Wert der modelOid-Eigenschaft ab.
         * 
         */
        public long getModelOid() {
            return modelOid;
        }

        /**
         * Legt den Wert der modelOid-Eigenschaft fest.
         * 
         */
        public void setModelOid(long value) {
            this.modelOid = value;
        }

        /**
         * Ruft den Wert der documentTypes-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link DocumentTypesXto }
         *     
         */
        public DocumentTypesXto getDocumentTypes() {
            return documentTypes;
        }

        /**
         * Legt den Wert der documentTypes-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link DocumentTypesXto }
         *     
         */
        public void setDocumentTypes(DocumentTypesXto value) {
            this.documentTypes = value;
        }

    }

}
