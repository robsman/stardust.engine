
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
 *         &lt;element name="updateFolder" type="{http://eclipse.org/stardust/ws/v2012a/api}Folder"/>
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
    "updateFolder"
})
@XmlRootElement(name = "updateFolder")
public class UpdateFolder {

    @XmlElement(required = true, nillable = true)
    protected FolderXto updateFolder;

    /**
     * Gets the value of the updateFolder property.
     * 
     * @return
     *     possible object is
     *     {@link FolderXto }
     *     
     */
    public FolderXto getUpdateFolder() {
        return updateFolder;
    }

    /**
     * Sets the value of the updateFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderXto }
     *     
     */
    public void setUpdateFolder(FolderXto value) {
        this.updateFolder = value;
    }

}
