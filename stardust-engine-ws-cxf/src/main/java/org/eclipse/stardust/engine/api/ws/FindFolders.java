
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
 *         &lt;element name="folderQuery" type="{http://eclipse.org/stardust/ws/v2012a/api}FolderQuery"/>
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
    "folderQuery"
})
@XmlRootElement(name = "findFolders")
public class FindFolders {

    @XmlElement(required = true, nillable = true)
    protected FolderQueryXto folderQuery;

    /**
     * Gets the value of the folderQuery property.
     * 
     * @return
     *     possible object is
     *     {@link FolderQueryXto }
     *     
     */
    public FolderQueryXto getFolderQuery() {
        return folderQuery;
    }

    /**
     * Sets the value of the folderQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderQueryXto }
     *     
     */
    public void setFolderQuery(FolderQueryXto value) {
        this.folderQuery = value;
    }

}
