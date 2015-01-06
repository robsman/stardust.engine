
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RepositoryCapabilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RepositoryCapabilities">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fullTextSearchSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="metaDataSearchSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="metaDataWriteSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="versioningSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="transactionSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="accessControlPolicySupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="writeSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryCapabilities", propOrder = {
    "fullTextSearchSupported",
    "metaDataSearchSupported",
    "metaDataWriteSupported",
    "versioningSupported",
    "transactionSupported",
    "accessControlPolicySupported",
    "writeSupported"
})
@XmlSeeAlso({
    RepositoryProviderInfoXto.class,
    RepositoryInstanceInfoXto.class
})
public class RepositoryCapabilitiesXto {

    protected boolean fullTextSearchSupported;
    protected boolean metaDataSearchSupported;
    protected boolean metaDataWriteSupported;
    protected boolean versioningSupported;
    protected boolean transactionSupported;
    protected boolean accessControlPolicySupported;
    protected boolean writeSupported;

    /**
     * Gets the value of the fullTextSearchSupported property.
     * 
     */
    public boolean isFullTextSearchSupported() {
        return fullTextSearchSupported;
    }

    /**
     * Sets the value of the fullTextSearchSupported property.
     * 
     */
    public void setFullTextSearchSupported(boolean value) {
        this.fullTextSearchSupported = value;
    }

    /**
     * Gets the value of the metaDataSearchSupported property.
     * 
     */
    public boolean isMetaDataSearchSupported() {
        return metaDataSearchSupported;
    }

    /**
     * Sets the value of the metaDataSearchSupported property.
     * 
     */
    public void setMetaDataSearchSupported(boolean value) {
        this.metaDataSearchSupported = value;
    }

    /**
     * Gets the value of the metaDataWriteSupported property.
     * 
     */
    public boolean isMetaDataWriteSupported() {
        return metaDataWriteSupported;
    }

    /**
     * Sets the value of the metaDataWriteSupported property.
     * 
     */
    public void setMetaDataWriteSupported(boolean value) {
        this.metaDataWriteSupported = value;
    }

    /**
     * Gets the value of the versioningSupported property.
     * 
     */
    public boolean isVersioningSupported() {
        return versioningSupported;
    }

    /**
     * Sets the value of the versioningSupported property.
     * 
     */
    public void setVersioningSupported(boolean value) {
        this.versioningSupported = value;
    }

    /**
     * Gets the value of the transactionSupported property.
     * 
     */
    public boolean isTransactionSupported() {
        return transactionSupported;
    }

    /**
     * Sets the value of the transactionSupported property.
     * 
     */
    public void setTransactionSupported(boolean value) {
        this.transactionSupported = value;
    }

    /**
     * Gets the value of the accessControlPolicySupported property.
     * 
     */
    public boolean isAccessControlPolicySupported() {
        return accessControlPolicySupported;
    }

    /**
     * Sets the value of the accessControlPolicySupported property.
     * 
     */
    public void setAccessControlPolicySupported(boolean value) {
        this.accessControlPolicySupported = value;
    }

    /**
     * Gets the value of the writeSupported property.
     * 
     */
    public boolean isWriteSupported() {
        return writeSupported;
    }

    /**
     * Sets the value of the writeSupported property.
     * 
     */
    public void setWriteSupported(boolean value) {
        this.writeSupported = value;
    }

}
