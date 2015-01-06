
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Represents the state of a folder.
 * 			
 * 
 * <p>Java class for Folder complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Folder">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/stardust/ws/v2012a/api}FolderInfo">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="path" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="levelOfDetail" type="{http://eclipse.org/stardust/ws/v2012a/api}FolderLevelOfDetail"/>
 *         &lt;element name="documentCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="documents" type="{http://eclipse.org/stardust/ws/v2012a/api}Documents" minOccurs="0"/>
 *         &lt;element name="folderCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="folders" type="{http://eclipse.org/stardust/ws/v2012a/api}Folders" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Folder", propOrder = {
    "id",
    "path",
    "levelOfDetail",
    "documentCount",
    "documents",
    "folderCount",
    "folders"
})
public class FolderXto
    extends FolderInfoXto
{

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String path;
    @XmlElement(required = true)
    protected FolderLevelOfDetailXto levelOfDetail;
    protected Integer documentCount;
    protected DocumentsXto documents;
    protected Integer folderCount;
    protected FoldersXto folders;

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
     * Gets the value of the levelOfDetail property.
     * 
     * @return
     *     possible object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public FolderLevelOfDetailXto getLevelOfDetail() {
        return levelOfDetail;
    }

    /**
     * Sets the value of the levelOfDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderLevelOfDetailXto }
     *     
     */
    public void setLevelOfDetail(FolderLevelOfDetailXto value) {
        this.levelOfDetail = value;
    }

    /**
     * Gets the value of the documentCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDocumentCount() {
        return documentCount;
    }

    /**
     * Sets the value of the documentCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDocumentCount(Integer value) {
        this.documentCount = value;
    }

    /**
     * Gets the value of the documents property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentsXto }
     *     
     */
    public DocumentsXto getDocuments() {
        return documents;
    }

    /**
     * Sets the value of the documents property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentsXto }
     *     
     */
    public void setDocuments(DocumentsXto value) {
        this.documents = value;
    }

    /**
     * Gets the value of the folderCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFolderCount() {
        return folderCount;
    }

    /**
     * Sets the value of the folderCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFolderCount(Integer value) {
        this.folderCount = value;
    }

    /**
     * Gets the value of the folders property.
     * 
     * @return
     *     possible object is
     *     {@link FoldersXto }
     *     
     */
    public FoldersXto getFolders() {
        return folders;
    }

    /**
     * Sets the value of the folders property.
     * 
     * @param value
     *     allowed object is
     *     {@link FoldersXto }
     *     
     */
    public void setFolders(FoldersXto value) {
        this.folders = value;
    }

}
