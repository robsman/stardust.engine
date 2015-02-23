
package org.eclipse.stardust.engine.api.ws;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			The InputDocument is used to specify a non existing document including content and targetFolder.
 * 			
 * 
 * <p>Java-Klasse für InputDocument complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="InputDocument">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="documentInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentInfo"/>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="versionInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}DocumentVersionInfo" minOccurs="0"/>
 *         &lt;element name="targetFolder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="globalVariableId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InputDocument", propOrder = {
    "documentInfo",
    "content",
    "versionInfo",
    "targetFolder",
    "globalVariableId"
})
public class InputDocumentXto {

    @XmlElement(required = true, nillable = true)
    protected DocumentInfoXto documentInfo;
    @XmlMimeType("*/*")
    protected DataHandler content;
    protected DocumentVersionInfoXto versionInfo;
    protected String targetFolder;
    protected String globalVariableId;

    /**
     * Ruft den Wert der documentInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DocumentInfoXto }
     *     
     */
    public DocumentInfoXto getDocumentInfo() {
        return documentInfo;
    }

    /**
     * Legt den Wert der documentInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentInfoXto }
     *     
     */
    public void setDocumentInfo(DocumentInfoXto value) {
        this.documentInfo = value;
    }

    /**
     * Ruft den Wert der content-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getContent() {
        return content;
    }

    /**
     * Legt den Wert der content-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setContent(DataHandler value) {
        this.content = value;
    }

    /**
     * Ruft den Wert der versionInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DocumentVersionInfoXto }
     *     
     */
    public DocumentVersionInfoXto getVersionInfo() {
        return versionInfo;
    }

    /**
     * Legt den Wert der versionInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentVersionInfoXto }
     *     
     */
    public void setVersionInfo(DocumentVersionInfoXto value) {
        this.versionInfo = value;
    }

    /**
     * Ruft den Wert der targetFolder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetFolder() {
        return targetFolder;
    }

    /**
     * Legt den Wert der targetFolder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetFolder(String value) {
        this.targetFolder = value;
    }

    /**
     * Ruft den Wert der globalVariableId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGlobalVariableId() {
        return globalVariableId;
    }

    /**
     * Legt den Wert der globalVariableId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGlobalVariableId(String value) {
        this.globalVariableId = value;
    }

}
