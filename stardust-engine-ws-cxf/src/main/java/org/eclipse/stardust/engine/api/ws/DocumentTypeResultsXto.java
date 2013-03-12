
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DocumentTypeResults complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
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
         * Gets the value of the modelId property.
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
         * Sets the value of the modelId property.
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
         * Gets the value of the modelOid property.
         * 
         */
        public long getModelOid() {
            return modelOid;
        }

        /**
         * Sets the value of the modelOid property.
         * 
         */
        public void setModelOid(long value) {
            this.modelOid = value;
        }

        /**
         * Gets the value of the documentTypes property.
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
         * Sets the value of the documentTypes property.
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
