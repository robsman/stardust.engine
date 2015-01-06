
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Represents the state of a Document.
 * 			This does not include document content.
 * 			
 * 
 * <p>Java class for Document complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Document">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}DocumentInfo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="revisionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="revisionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="revisionComment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="versionLabels" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="versionLabel" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Document", propOrder = {
    "id",
    "path",
    "size",
    "revisionId",
    "revisionName",
    "revisionComment",
    "versionLabels",
    "encoding"
})
public class DocumentXto
    extends DocumentInfoXto
{

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String path;
    protected long size;
    protected String revisionId;
    protected String revisionName;
    protected String revisionComment;
    protected DocumentXto.VersionLabelsXto versionLabels;
    protected String encoding;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(long value) {
        this.size = value;
    }

    /**
     * Gets the value of the revisionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisionId() {
        return revisionId;
    }

    /**
     * Sets the value of the revisionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionId(String value) {
        this.revisionId = value;
    }

    /**
     * Gets the value of the revisionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisionName() {
        return revisionName;
    }

    /**
     * Sets the value of the revisionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionName(String value) {
        this.revisionName = value;
    }

    /**
     * Gets the value of the revisionComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevisionComment() {
        return revisionComment;
    }

    /**
     * Sets the value of the revisionComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevisionComment(String value) {
        this.revisionComment = value;
    }

    /**
     * Gets the value of the versionLabels property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentXto.VersionLabelsXto }
     *     
     */
    public DocumentXto.VersionLabelsXto getVersionLabels() {
        return versionLabels;
    }

    /**
     * Sets the value of the versionLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentXto.VersionLabelsXto }
     *     
     */
    public void setVersionLabels(DocumentXto.VersionLabelsXto value) {
        this.versionLabels = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
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
     *         &lt;element name="versionLabel" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "versionLabel"
    })
    public static class VersionLabelsXto {

        protected List<String> versionLabel;

        /**
         * Gets the value of the versionLabel property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the versionLabel property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVersionLabel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getVersionLabel() {
            if (versionLabel == null) {
                versionLabel = new ArrayList<String>();
            }
            return this.versionLabel;
        }

    }

}
