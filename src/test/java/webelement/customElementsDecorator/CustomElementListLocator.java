package webelement.customElementsDecorator;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.internal.LocatingElementListHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * This class creates handles the calls of methods of List of custom webelements.
 **/
public class CustomElementListLocator implements MethodInterceptor {

    /**
     * The locator to get the webelement from the webpage.
     **/
    private final ElementLocator locator;

    /**
     * The constructor.
     *
     * @param locator The locator to get the webelement from the webpage.
     **/
    public CustomElementListLocator(ElementLocator locator) {
        this.locator = locator;
    }

    /**
     * Handles the method calls to a custom webelement.
     *
     * @param o           The object from which the method was called.
     * @param method      The called method.
     * @param objects     The parameter object of the value.
     * @param methodProxy Used to call the method of the superclass.
     **/
    //@Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        // Configure a custom webelement (WebButton etc.)
        if (o instanceof List) {
            // Invokes the method of the original object
            try {
                return methodProxy.invokeSuper(o, objects);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        // Configure a normal webelement
        // Should never be called in the current usecase because it gets handled in the CustomElementFieldDecorator class
        else if (o instanceof WebElement) {
            // Only handle first displayed
            // Get the first default webelement which matches the locator
            List<WebElement> displayedElement = locateElement();

            if (displayedElement != null) {
                return method.invoke(displayedElement, objects);
            }
            else {
                return methodProxy.invokeSuper(o, objects);
            }
        }

        return null;
    }

    private List<WebElement> locateElement() {
        return proxyForListLocator(ElementLocator.class.getClassLoader(), locator);
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> proxyForListLocator(ClassLoader loader, ElementLocator locator) {
      InvocationHandler handler = new LocatingElementListHandler(locator);

      List<WebElement> proxy;
      proxy = (List<WebElement>) Proxy.newProxyInstance(
          loader, new Class[]{List.class}, handler);
      return proxy;
    }

}
