
package org.eclipse.stardust.engine.api.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


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
 *         &lt;element name="folderIds">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="folderId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="folderLevelOfDetail" type="{http://eclipse.org/stardust/ws/v2012a/api}FolderLevelOfDetail"/>
 *         &lt;element name="documentMetaDataType" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;element name="folderMetaDataType" type="{http://www.w3.org/2001/XMLSchema}QName"/>
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
    "folderIds",
    "folderLevelOfDetail",
    "documentMetaDataType",
    "folderMetaDataType"
})
@XmlRootElement(name = "getFolders")
public class GetFolders {

    @XmlElement(required = true, nillable = true)
    protected GetFolders.FolderIdsXto folderIds;
    @XmlElement(required = true, nillable = true)
    protected FolderLevelOfDetailXto folderLevelOfDetail;
    @XmlElement(required = true, nillable = true)
    protected QName documentMetaDataType;
    @XmlElement(required = true, nillable = true)
    protected QName folderMetaDataType;

    /**
     * Gets the value of the folderIds property.
     * 
     * @return
     *     possible object is
     *     {@link GetFolders.FolderIdsXto }
     *     
     */
    public GetFolders.FolderIdsXto getFolderIds() {
        return folderIds;
    }

    /**
     * Sets the value of the folderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetFolders.FolderIdsXto }
     *     
     */
    public void setFolderIds(GetFolders.FolderIdsXto value) {
        this.folderIds = value;
    }

    /**
     * Gets the value of the folderLevelOfDetail property.
     * 
     * @return
     *     possible object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public FolderLevelOfDetailXto getFolderLevelOfDetail() {
        return folderLevelOfDetail;
    }

    /**
     * Sets the value of the folderLevelOfDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public void setFolderLevelOfDetail(FolderLevelOfDetailXto value) {
        this.folderLevelOfDetail = value;
    }

    /**
     * Gets the value of the documentMetaDataType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getDocumentMetaDataType() {
        return documentMetaDataType;
    }

    /**
     * Sets the value of the documentMetaDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setDocumentMetaDataType(QName value) {
        this.documentMetaDataType = value;
    }

    /**
     * Gets the value of the folderMetaDataType property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getFolderMetaDataType() {
        return folderMetaDataType;
    }

    /**
     * Sets the value of the folderMetaDataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setFolderMetaDataType(QName value) {
        this.folderMetaDataType = value;
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
     *         &lt;element name="folderId" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
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
        "folderId"
    })
    public static class FolderIdsXto {

        protected List<String> folderId;

        /**
         * Gets the value of the folderId property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the folderId property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getFolderId().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getFolderId() {
            if (folderId == null) {
                folderId = new ArrayList<String>();
            }
            return this.folderId;
        }

    }

}
