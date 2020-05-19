package webelement.customElements.superElements;

import org.openqa.selenium.support.PageFactory;
import webelement.customElementsDecorator.CustomElementFieldDecorator;
import webelement.modules.WebElementTransformer;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Parent class for all complex web elements.
 **/
public abstract class CustomWebElement {

    /**
     * The webDriver which can be used in subclasses.
     **/
    private WebDriver webDriver;

    /**
     * The locator through which the element(s) used for an action will be identified.
     **/
    private By locator;

    /**
     * Used to access the locators of a webelement.
     * **/
    private WebElementTransformer transformer;

    /**
     * Used to store a WebElement so that driver.findElement() isn't called repetatively
     * Don't rename this variable name. We check for it and ignore it in our CustomElementFieldDecorator class
     */
    protected WebElement __actual_web_element_reference;

    /**
     * Used to store list index of WebElement within a List<WebElement>
     * Can be used to determine getBy() with this index
     */
    private int listIndex = -1;

    /**
     * Constructor.
     *
     * @param webDriver The webDriver used to interact with the webbrowser.
     * @param by        The locator used to identify the element(s) on the website.
     **/
    public CustomWebElement(WebDriver webDriver, By by) {
        this.webDriver = webDriver;
        locator = by;
        transformer = new WebElementTransformer();
        
        // Call the page factory on this object to initialize custom webelements in custom webelements (aka nesting)
        PageFactory.initElements(new CustomElementFieldDecorator(webDriver, webDriver), this);
        //The following assignment needs to be done after initElements
        //initElements first scans all Page classes and CustomWebElement classes for WebElement or CustomWebElement fields nested within
        //If the field name happens to be the following, null is assigned by initElements
        //Hence we need to assign the actual value after initELements is done assigning null otherwise our value is bound to get overwritten by null
        __actual_web_element_reference = this.webDriver.findElement(locator);

    }

    /**
     * Constructor.
     *
     * @param webDriver The webDriver used to interact with the webbrowser.
     * @param by        The locator used to identify the element(s) on the website. This is expected to return List<WebElement>
     * @param webElement The webElement which forms part of List<WebElement>
     * @param listIndex The list index of webElement within the List<WebElement>
     **/
    public CustomWebElement(WebDriver webDriver, By by, WebElement webElement, int listIndex) {
        this.webDriver = webDriver;
        locator = by;
        transformer = new WebElementTransformer();
        this.listIndex = listIndex;

        // Call the page factory on this object to initialize custom webelements in custom webelements (aka nesting)
        PageFactory.initElements(new CustomElementFieldDecorator(webDriver, webDriver), this);
        //The following assignment needs to be done after initElements
        //initElements first scans all Page classes and CustomWebElement classes for WebElement or CustomWebElement fields nested within
        //If the field name happens to be the following, null is assigned by initElements
        //Hence we need to assign the actual value after initELements is done assigning null otherwise our value is bound to get overwritten by null
        __actual_web_element_reference = webElement;
    }

    /**
     * Returns the locator to identify the element(s) on the website.
     *
     * @return Returns the locator to identify the element(s) on the website.
     **/
    public By getBy() {
        return locator;
    }

    /**
     * Returns the WebElement corresponding to this CustomWebElement
     * @return WebElement
     */
    protected WebElement getWebElement() {
    	return __actual_web_element_reference;
    }

    /**
     * Returns the list index within a List<WebElement>
     * @return listIndex
     */
    public int getListIndex() {
    	return this.listIndex;
    }

    /**
     * The element must be visible in order to retrieve its attribute.
     * Get the value of a the given attribute of the element. Will return the current value, even if
     * this has been modified after the page has been loaded. More exactly, this method will return
     * the value of the given attribute, unless that attribute is not present, in which case the value
     * of the property with the same name is returned (for example for the "value" property of a
     * textarea element). If neither value is set, null is returned. The "style" attribute is
     * converted as best can be to a text representation with a trailing semi-colon. The following are
     * deemed to be "boolean" attributes, and will return either "true" or null:
     * <p>
     * async, autofocus, autoplay, checked, compact, complete, controls, declare, defaultchecked,
     * defaultselected, defer, disabled, draggable, ended, formnovalidate, hidden, indeterminate,
     * iscontenteditable, ismap, itemscope, loop, multiple, muted, nohref, noresize, noshade,
     * novalidate, nowrap, open, paused, pubdate, readonly, required, reversed, scoped, seamless,
     * seeking, selected, spellcheck, truespeed, willvalidate
     * <p>
     * Finally, the following commonly mis-capitalized attribute/property names are evaluated as
     * expected:
     * <p>
     * <ul>
     * <li>"class"
     * <li>"readonly"
     * </ul>
     *
     * @param attributeName The name of the attribute.
     * @return The attribute/property's current value or null if the value is not set.
     */
    public String getAttribute(String attributeName) {
        return __actual_web_element_reference.getAttribute(attributeName);
    }

    /**
     * Returns the module to transform stuff.
     *
     * @return Returns the module to transform stuff.
     **/
    protected WebElementTransformer transformer() {
        return transformer;
    }

    /**
     * Returns the webDriver.
     *
     * @return Returns the webDriver.
     **/
    public WebDriver getWebDriver() {
        return webDriver;
    }

    /**
     * Returns the used type of a given by locator.
     *
     * @return Returns the used type of a given by locator.
     **/
    protected WebElementTransformer.LocatorType getLocatorType() {
        return transformer.getLocatorType(getBy());
    }

    /**
     * Returns the locator value of a locator.
     *
     * @param type The type of the locator.
     * @return The value of the locator.
     **/
    protected String getLocatorValue(WebElementTransformer.LocatorType type) {
        return transformer.getLocatorValue(getBy(), type);
    }
    
    /**
     * Returns the tag name
     * 
     *   @return tag name
     * */
    public String getTagName()
    {
    	return __actual_web_element_reference.getTagName();
    }
    
    public void sendKeys(Keys keys)
    {
    	__actual_web_element_reference.sendKeys(keys);
    }
    
    /**
     * Reduce the complexity of hierarchy 
     * No need to implement WebPageElement class.
     * 
     * Clicks on the button.
     **/
    public void click() {
        __actual_web_element_reference.click();
    }

    /**
     * Sets the text of the element.
     **/
    public void setText(String text) {
        __actual_web_element_reference.clear();
        __actual_web_element_reference.sendKeys(text);
    }

    /**
     * Finds an element which uses the locator of this element as base.
     *
     * @return The found sub web element of this complex web element.
     **/
    public WebElement findElement(By locator) {
        return __actual_web_element_reference.findElement(locator);
    }

    /**
     * Finds elements which uses the locator of this element as base.
     *
     * @return The found sub web elements of this complex web element.
     **/
    public List<WebElement> findElements(By locator) {
        return __actual_web_element_reference.findElements(locator);
    }

    /**
     * Returns the node text of the element.
     *
     * @return Returns the node text of the element.
     **/
    public String getText() {
        return __actual_web_element_reference.getText();
    }
    
    public boolean isSelected()
    {
    	return __actual_web_element_reference.isSelected();
    }
    
    public boolean isDisplayed()
    {
    	return __actual_web_element_reference.isDisplayed();
    }
    
    public boolean isEnabled()
    {
    	return __actual_web_element_reference.isEnabled();
    }
}