
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Represents rules that apply to passwords.
 * 			
 * 
 * <p>Java class for PasswordRules complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PasswordRules">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="passwordTracking" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="notificationMails" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="expirationTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="disableUserTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="differentCharacters" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="minimalPasswordLength" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="letters" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="digits" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="mixedCase" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="punctuation" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PasswordRules", propOrder = {
    "passwordTracking",
    "notificationMails",
    "expirationTime",
    "disableUserTime",
    "differentCharacters",
    "minimalPasswordLength",
    "letters",
    "digits",
    "mixedCase",
    "punctuation"
})
public class PasswordRulesXto {

    protected int passwordTracking;
    protected int notificationMails;
    protected int expirationTime;
    protected int disableUserTime;
    protected int differentCharacters;
    protected int minimalPasswordLength;
    protected int letters;
    protected int digits;
    protected int mixedCase;
    protected int punctuation;

    /**
     * Gets the value of the passwordTracking property.
     * 
     */
    public int getPasswordTracking() {
        return passwordTracking;
    }

    /**
     * Sets the value of the passwordTracking property.
     * 
     */
    public void setPasswordTracking(int value) {
        this.passwordTracking = value;
    }

    /**
     * Gets the value of the notificationMails property.
     * 
     */
    public int getNotificationMails() {
        return notificationMails;
    }

    /**
     * Sets the value of the notificationMails property.
     * 
     */
    public void setNotificationMails(int value) {
        this.notificationMails = value;
    }

    /**
     * Gets the value of the expirationTime property.
     * 
     */
    public int getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the value of the expirationTime property.
     * 
     */
    public void setExpirationTime(int value) {
        this.expirationTime = value;
    }

    /**
     * Gets the value of the disableUserTime property.
     * 
     */
    public int getDisableUserTime() {
        return disableUserTime;
    }

    /**
     * Sets the value of the disableUserTime property.
     * 
     */
    public void setDisableUserTime(int value) {
        this.disableUserTime = value;
    }

    /**
     * Gets the value of the differentCharacters property.
     * 
     */
    public int getDifferentCharacters() {
        return differentCharacters;
    }

    /**
     * Sets the value of the differentCharacters property.
     * 
     */
    public void setDifferentCharacters(int value) {
        this.differentCharacters = value;
    }

    /**
     * Gets the value of the minimalPasswordLength property.
     * 
     */
    public int getMinimalPasswordLength() {
        return minimalPasswordLength;
    }

    /**
     * Sets the value of the minimalPasswordLength property.
     * 
     */
    public void setMinimalPasswordLength(int value) {
        this.minimalPasswordLength = value;
    }

    /**
     * Gets the value of the letters property.
     * 
     */
    public int getLetters() {
        return letters;
    }

    /**
     * Sets the value of the letters property.
     * 
     */
    public void setLetters(int value) {
        this.letters = value;
    }

    /**
     * Gets the value of the digits property.
     * 
     */
    public int getDigits() {
        return digits;
    }

    /**
     * Sets the value of the digits property.
     * 
     */
    public void setDigits(int value) {
        this.digits = value;
    }

    /**
     * Gets the value of the mixedCase property.
     * 
     */
    public int getMixedCase() {
        return mixedCase;
    }

    /**
     * Sets the value of the mixedCase property.
     * 
     */
    public void setMixedCase(int value) {
        this.mixedCase = value;
    }

    /**
     * Gets the value of the punctuation property.
     * 
     */
    public int getPunctuation() {
        return punctuation;
    }

    /**
     * Sets the value of the punctuation property.
     * 
     */
    public void setPunctuation(int value) {
        this.punctuation = value;
    }

}
