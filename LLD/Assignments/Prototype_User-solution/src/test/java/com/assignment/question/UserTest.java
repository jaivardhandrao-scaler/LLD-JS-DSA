package com.assignment.question;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserImplementsClonableObject() {
        Class<?> userClass = User.class;
        Class<?>[] interfaces = userClass.getInterfaces();

        boolean implementsClonableObject = false;

        for (Class<?> iface : interfaces) {
            if (iface == ClonableObject.class) {
                implementsClonableObject = true;
                break;
            }
        }

        assertTrue(implementsClonableObject, "If the prototype pattern is implemented correctly, the User class should implement the ClonableObject interface");
    }

    @Test
    public void testUserCloneMethodCreatesDistinctObject() {
        User user = new User(1, "testuser", "test@example.com", "Test User", 25, UserType.ADMIN);

        // Using reflection to find and invoke the clone method
        try {
            Method cloneMethod = user.getClass().getMethod("cloneObject");
            assertNotNull(cloneMethod, "If the prototype pattern is implemented correctly, the User class should have a cloneObject method");

            // Calling the clone method to create a copy
            User clonedUser = (User) cloneMethod.invoke(user);
            assertNotNull(clonedUser, "If the clone method is implemented correctly, it should return a non-null object");

            // Validating that the clonedUser is not the same object as user
            assertNotSame(user, clonedUser, "If the clone method is implemented correctly, it should return a new object");

            // Asserting that the cloned user has the same values as the original user
            assertEquals(user.getUserId(), clonedUser.getUserId(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
            assertEquals(user.getUsername(), clonedUser.getUsername(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
            assertEquals(user.getEmail(), clonedUser.getEmail(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
            assertEquals(user.getDisplayName(), clonedUser.getDisplayName(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
            assertEquals(user.getAge(), clonedUser.getAge(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
            assertEquals(user.getType(), clonedUser.getType(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail("If the prototype pattern is implemented correctly, the User class should have a cloneObject method");
        }
    }

    @Test
    public void testRegistry() {

        UserPrototypeRegistry registry = getRegistry();
        assertNotNull(registry, "If the registry pattern is implemented correctly, the registry should not be null");

        User user = new User(1, "testuser", "test@example.com", "Test User", 25, UserType.ADMIN);
        registry.addPrototype(user);

        User prototype = registry.getPrototype(user.getType());
        assertNotNull(prototype, "If the clone method is implemented correctly, it should return a non-null object");
        assertSame(user, prototype, "If the registry pattern is implemented correctly, the registry should return the same object that was added");
    }

    private static UserPrototypeRegistry getRegistry() {
        // Use Reflections to find the class implementing UserPrototypeRegistry
        Reflections reflections = new Reflections(UserTest.class.getPackageName(),
                new SubTypesScanner(false));
        Set<Class<? extends UserPrototypeRegistry>> classes = reflections.getSubTypesOf(UserPrototypeRegistry.class);

        // Ensure there is exactly one class implementing the interface
        assertEquals(1, classes.size(), "If the registry pattern is implemented correctly, there should be exactly one class implementing UserPrototypeRegistry");

        // Instantiate the registry implementation
        Class<? extends UserPrototypeRegistry> registryClass = classes.iterator().next();
        try {
            return registryClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            fail("If the registry pattern is implemented correctly, the registry class should have a public no-args constructor");
        }
        return null;
    }

    @Test
    public void testRegistryClone() {
        
        User user = new User(1, "testuser", "test@example.com", "Test User", 25, UserType.ADMIN);
        UserPrototypeRegistry registry = getRegistry();
        assertNotNull(registry, "If the registry pattern is implemented correctly, the registry should not be null");

        registry.addPrototype(user);

        // Clone the prototype and validate it's a distinct object with the same values
        User clonedUser = registry.clone(user.getType());
        assertNotNull(clonedUser, "If the clone method is implemented correctly, it should return a non-null object");
        assertNotSame(user, clonedUser, "If the clone method is implemented correctly, it should return a new object");
        
        assertEquals(user.getUserId(), clonedUser.getUserId(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
        assertEquals(user.getUsername(), clonedUser.getUsername(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
        assertEquals(user.getEmail(), clonedUser.getEmail(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
        assertEquals(user.getDisplayName(), clonedUser.getDisplayName(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
        assertEquals(user.getAge(), clonedUser.getAge(), "If the clone method is implemented correctly, it should return a new object with the same values as the original object");
    }
}