
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
 * <p>Java-Klasse für Folder complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
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
     * Ruft den Wert der id-Eigenschaft ab.
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
     * Legt den Wert der id-Eigenschaft fest.
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
     * Ruft den Wert der path-Eigenschaft ab.
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
     * Legt den Wert der path-Eigenschaft fest.
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
     * Ruft den Wert der levelOfDetail-Eigenschaft ab.
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
     * Legt den Wert der levelOfDetail-Eigenschaft fest.
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
     * Ruft den Wert der documentCount-Eigenschaft ab.
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
     * Legt den Wert der documentCount-Eigenschaft fest.
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
     * Ruft den Wert der documents-Eigenschaft ab.
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
     * Legt den Wert der documents-Eigenschaft fest.
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
     * Ruft den Wert der folderCount-Eigenschaft ab.
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
     * Legt den Wert der folderCount-Eigenschaft fest.
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
     * Ruft den Wert der folders-Eigenschaft ab.
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
     * Legt den Wert der folders-Eigenschaft fest.
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
