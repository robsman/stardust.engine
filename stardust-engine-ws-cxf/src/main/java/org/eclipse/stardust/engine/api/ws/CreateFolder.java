
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
 *         &lt;element name="parentFolderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="folderInfo" type="{http://eclipse.org/stardust/ws/v2012a/api}FolderInfo"/>
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
    "parentFolderId",
    "folderInfo"
})
@XmlRootElement(name = "createFolder")
public class CreateFolder {

    @XmlElement(required = true, nillable = true)
    protected String parentFolderId;
    @XmlElement(required = true, nillable = true)
    protected FolderInfoXto folderInfo;

    /**
     * Gets the value of the parentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentFolderId() {
        return parentFolderId;
    }

    /**
     * Sets the value of the parentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentFolderId(String value) {
        this.parentFolderId = value;
    }

    /**
     * Gets the value of the folderInfo property.
     * 
     * @return
     *     possible object is
     *     {@link FolderInfoXto }
     *     
     */
    public FolderInfoXto getFolderInfo() {
        return folderInfo;
    }

    /**
     * Sets the value of the folderInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderInfoXto }
     *     
     */
    public void setFolderInfo(FolderInfoXto value) {
        this.folderInfo = value;
    }

}
