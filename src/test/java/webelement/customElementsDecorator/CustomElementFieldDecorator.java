package webelement.customElementsDecorator;

import webelement.customElements.superElements.CustomWebElement;
import webelement.modules.WebElementTransformer;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.FieldDecorator;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/*
 * Good sources:
 * http://www.alechenninger.com/2014/07/a-case-study-of-javas-dynamic-proxies_14.html
 * http://www.mograblog.com/2013/08/extending-selenium-in-java.html
 * https://www.baeldung.com/cglib
 */

/*
 * Short summary:
 * The page factory runs through all fields of a class an calls the "decorate()" method of the "FieldDecorator" interface.
 * The "decorate()" method has to return an object which has the correct type.
 * 
 * For that it needs the locator of the field so that the webelement can be identified on the website.
 * 
 * It should work like this:
 * 
 * Page class should be instantiated -> PageFactory.init() -> decorate() for each field
 * 		choice 1: decorate for a default webelement: DefaultFieldDecorator is used for default behavior
 * 		choice 2: decorate has create proper custom webelement -> getLocator() to be able do get the webelement
 * 			-> getEnhancedObject() to add callback method to lazy initialize and configure the custom webelement
 * 			-> getElementHandler(), which implements the callback method to add callback method to lazy initialize and configure the custom webelement.
 * */

/**
 * Idea and code (partly) was taken from: http://www.mograblog.com/2013/08/extending-selenium-in-java.html.
 * <p>
 * An implementation of a FieldDecorator to enable the usage of custom webelements via a page factory.
 * Custom webelements will be created via lazy initialisation.
 **/
public class CustomElementFieldDecorator implements FieldDecorator {

    /**
     * The decorator which is used when a default WebElement is used and the default behavior can be used.
     **/
    private final DefaultFieldDecorator defaultFieldDecorator;

    /**
     * The search context for the (custom) webelement. Mostly the webdriver.
     **/
    private final SearchContext searchContext;

    /**
     * The webdriver.
     **/
    private final WebDriver webDriver;

    /**
     * The constructor. It constructs.
     *
     * @param searchContext The search context for the (custom) webelement. Mostly just a webdriver object.
     *                      Used to find webelements on a webpage.
     * @param webDriver     The webDriver which will be used to create the webelement.
     **/
    public CustomElementFieldDecorator(SearchContext searchContext, WebDriver webDriver) {
        this.searchContext = searchContext;
        this.webDriver = webDriver;
        defaultFieldDecorator = new DefaultFieldDecorator(new DefaultElementLocatorFactory(searchContext));
    }

    /**
     * This method is called by the Selenium PageFactory on all fields to decide how to decorate the field.
     *
     * @param loader The class loader that was used for the page object
     * @param field  The field which should be decorated. Should be an FindBy annotated (custom) webelement.
     * @return Value to decorate the field with.
     **/
    //@Override
    public Object decorate(ClassLoader loader, Field field) {
    	//If it is a custom annotated webelement, then ensure proper initialisation via the adding of the callback method
        if (CustomWebElement.class.isAssignableFrom(field.getType())  && field.isAnnotationPresent(FindBy.class)) {
            return getEnhancedObject(field.getType(), getElementHandler(field), field.getAnnotation(FindBy.class));
        }
        //Else if it happens to be List<? extends CustomWebElement>
        else if(isDecoratableList(field)) {
            Type genericType = field.getGenericType();
            Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            try {
            	Class<?> listTypeClass = Class.forName(listType.getTypeName());
            	return getEnhancedListObject(field.getType(), getElementListHandler(field), field.getAnnotation(FindBy.class), listTypeClass, getElementHandler(field));
            } catch(ClassNotFoundException e) {
            	return null;
            }
        }
        // If it is a normal webelement, then use the default FieldDecorator implementation
        else {
            //ignore if WebElement happens to be __actual_web_element_reference
        	//we maintain this reference within CustomWebElement and we don't want it to be located as it isn't present on page.
        	if(!field.getName().equals("__actual_web_element_reference"))
        		return defaultFieldDecorator.decorate(loader, field);
        	else
        		return null;
        }
    }

    private boolean isDecoratableList(Field field) {
        if (!List.class.isAssignableFrom(field.getType())) {
          return false;
        }

        // Type erasure in Java isn't complete. Attempt to discover the generic
        // type of the list.
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
          return false;
        }

        Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        //System.out.println("Generic type within List is " + listType.getTypeName());
        boolean isAssignableFromCustomWebElement = false;
        try {
        	Class<?> listTypeClass = Class.forName(listType.getTypeName());
        	//System.out.println(" and Class formed is " + listTypeClass.getName());
            if (!CustomWebElement.class.isAssignableFrom(listTypeClass )) {
                return false;
              } else {
            	  isAssignableFromCustomWebElement = true;
              }

        } catch(ClassNotFoundException e) {
        	return false;
        }

        return (field.getAnnotation(FindBy.class) != null ||
               field.getAnnotation(FindBys.class) != null ||
               field.getAnnotation(FindAll.class) != null) &&
        		isAssignableFromCustomWebElement;
      }

    /**
     * Creates the class with the callback method. The callback method will be called when a method is called on
     * the given field object (e.g. a click() method call on a button).
     *
     * @return The class which contains the callback method.
     **/
    private CustomElementLocator getElementHandler(Field field) {
        return new CustomElementLocator(getLocator(field));
    }

    private CustomElementListLocator getElementListHandler(Field field) {
        return new CustomElementListLocator(getLocator(field));
    }

    /**
     * Returns the element handler for the field which melds the field and the locator (aka FindBy) together for further
     * usage. An ElementLocator locator can find a webelement on a webpage without any parameters since all the needed information
     * is already there.
     *
     * @param field The annotated field from which the element locator will be created.
     * @return The element locator object.
     **/
    private ElementLocator getLocator(Field field) {
        return new DefaultElementLocatorFactory(searchContext).createLocator(field);
    }

    /**
     * Enhances the class to call a specific method callback when a method of that class is called. Example: A button is
     * clicked -> Since the class might be a custom webelement, the callback method is called to handle the
     * initialisation of the custom webelement.
     *
     * @param clzz              The class which should be enhanced with the callback method (e.g. a custom button). If a method in
     *                          that class is called, the callback method will be triggered.
     * @param methodInterceptor The class which implements the callback method.
     * @param locator           The locator which was used to identify the webelement via the FindBy annotation.
     **/
    private Object getEnhancedObject(Class<?> clzz, MethodInterceptor methodInterceptor, FindBy locator) {
        Enhancer e = new Enhancer();
        WebElementTransformer transformer = new WebElementTransformer();

        e.setSuperclass(clzz);
        e.setCallback(methodInterceptor);

        return e.create(new Class[]{WebDriver.class, By.class}, new Object[]{webDriver, transformer.transformFindByToBy(locator)});
    }

    @SuppressWarnings("unchecked")
	private Object getEnhancedListObject(Class<?> listClass, MethodInterceptor methodInterceptor, FindBy locator, Class<?> elementClass, MethodInterceptor elementMethodInterceptor) {
        Enhancer listEnhancer = new Enhancer();

        if(listClass.isInterface()) {
        	listEnhancer.setSuperclass(ArrayList.class);
        } else {
        	listEnhancer.setSuperclass(listClass);
        }
        listEnhancer.setInterfaces(new Class[] {List.class});
        listEnhancer.setCallback(methodInterceptor);
        List<CustomWebElement> customElements = (List<CustomWebElement>) listEnhancer.create();
        WebElementTransformer transformer = new WebElementTransformer();
        By by = transformer.transformFindByToBy(locator);
        List<WebElement> elements = webDriver.findElements(by);
        Iterator<WebElement> webElementIterator = elements.iterator();
        int elementIndex = 0;
        while(webElementIterator.hasNext()) {
        	WebElement element = webElementIterator.next();
        	Enhancer elementEnhancer = new Enhancer();
        	elementEnhancer.setSuperclass(elementClass);
        	elementEnhancer.setCallback(elementMethodInterceptor);
        	CustomWebElement customElement = (CustomWebElement)elementEnhancer.create(new Class[]{WebDriver.class, By.class, WebElement.class, int.class}, new Object[]{webDriver, by, element, elementIndex});
        	customElements.add(customElement);
        	elementIndex = elementIndex + 1;
        }
        return customElements;
    }
}
