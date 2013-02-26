
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
 *         &lt;element name="folders" type="{http://eclipse.org/stardust/ws/v2012a/api}Folders"/>
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
    "folders"
})
@XmlRootElement(name = "findFoldersResponse")
public class FindFoldersResponse {

    @XmlElement(required = true, nillable = true)
    protected FoldersXto folders;

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
