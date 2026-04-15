package apidecorator;

import apidecorator.api.Api;
import apidecorator.api.SimpleEcommerceAPI;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ApiDecoratorTest {

    private static final Class<?> apiInterfaceClass = Api.class;
    private static final Class<?> simpleEcommerceApiClass = SimpleEcommerceAPI.class;
    private static final Class<?> baseApiDecoratorClass = BaseApiDecorator.class;
    private static final Class<?> loggingDecoratorClass = LoggingDecorator.class;
    private static final Class<?> rateLimitingDecoratorClass = RateLimitingDecorator.class;

    @Test
    public void testApiInterfaceMethods() {
        Method[] methods = apiInterfaceClass.getDeclaredMethods();
        assertTrue(methods.length >= 1, "If the decorator pattern is implemented correctly, apidecorator.api.Api should have at least 1 method: executeRequest.");

        boolean hasExecuteRequestMethod = Stream.of(methods).anyMatch(method -> method.getName().equals("executeRequest") && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(String.class) && method.getReturnType().equals(String.class));

        assertTrue(hasExecuteRequestMethod, "If the decorator pattern is implemented correctly, apidecorator.api.Api should have an executeRequest method that takes a String parameter and returns a String.");
    }

    @Test
    public void testSimpleEcommerceApiImplementation() {
        assertTrue(apiInterfaceClass.isAssignableFrom(simpleEcommerceApiClass), "If the decorator pattern is implemented correctly, apidecorator.api.SimpleEcommerceAPI should implement the apidecorator.api.Api interface.");
    }

    @Test
    public void testBaseApiDecoratorImplementation() {
        assertTrue(apiInterfaceClass.isAssignableFrom(baseApiDecoratorClass), "If the decorator pattern is implemented correctly, apidecorator.BaseApiDecorator should implement the apidecorator.api.Api interface.");

        var fields = baseApiDecoratorClass.getDeclaredFields();
        boolean hasNextLayerField = Stream.of(fields).anyMatch(field -> field.getType().equals(Api.class));

        assertTrue(hasNextLayerField, "If the decorator pattern is implemented correctly, apidecorator.BaseApiDecorator should have a field to store the next layer.");

        try {
            Constructor<?> constructor = baseApiDecoratorClass.getConstructor(Api.class);
            assertNotNull(constructor, "If the decorator pattern is implemented correctly, apidecorator.BaseApiDecorator should have a constructor that takes a apidecorator.api.Api instance.");
        } catch (NoSuchMethodException e) {
            fail("If the decorator pattern is implemented correctly, apidecorator.BaseApiDecorator should have a constructor that takes a apidecorator.api.Api instance.");
        }
    }

    @Test
    public void testLoggingDecoratorImplementation() {
        assertTrue(baseApiDecoratorClass.isAssignableFrom(loggingDecoratorClass), "If the decorator pattern is implemented correctly, apidecorator.LoggingDecorator should extend the apidecorator.BaseApiDecorator class.");

        Constructor<?>[] constructors = loggingDecoratorClass.getDeclaredConstructors();
        boolean hasConstructorWithApi = Stream.of(constructors).anyMatch(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0].equals(Api.class);
        });

        assertTrue(hasConstructorWithApi, "If the decorator pattern is implemented correctly, apidecorator.LoggingDecorator should have a constructor that takes a apidecorator.api.Api instance.");
    }

    @Test
    public void testRateLimitingDecoratorImplementation() {

        assertTrue(baseApiDecoratorClass.isAssignableFrom(rateLimitingDecoratorClass), "If the decorator pattern is implemented correctly, apidecorator.RateLimitingDecorator should extend the apidecorator.BaseApiDecorator class.");


        Constructor<?>[] constructors = rateLimitingDecoratorClass.getDeclaredConstructors();
        boolean hasConstructorWithApi = Stream.of(constructors).anyMatch(constructor -> {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            return parameterTypes.length == 1 && parameterTypes[0].equals(Api.class);
        });

        assertTrue(hasConstructorWithApi, "If the decorator pattern is implemented correctly, apidecorator.RateLimitingDecorator should have a constructor that takes a apidecorator.api.Api instance.");
    }
}
