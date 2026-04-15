package com.assignment.question;

import org.junit.jupiter.api.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolTest {

    private static Class<? extends ConnectionPool> implementationClass;
    private static final int MAX_CONNECTIONS = 10;

    @BeforeAll
    public static void setUpClass() {
        // Get the package name of the test class
        String packageName = ConnectionPoolTest.class.getPackageName();

        // Use Reflections to scan for classes within the package
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Set<Class<? extends ConnectionPool>> subTypes = reflections.getSubTypesOf(ConnectionPool.class);

        // Find a non-abstract implementation of ConnectionPool
        for (Class<?> clazz : subTypes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                implementationClass = (Class<? extends ConnectionPool>) clazz;
                break;
            }
        }
    }

    @Test
    public void preTestConnectionPoolImplementationFound() {
        assertNotNull(implementationClass,
                "If an implementation of ConnectionPool exists, it should be found");

        Constructor<?>[] constructors = implementationClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            assertFalse(Modifier.isPublic(constructor.getModifiers()),
                    "If an implementation of ConnectionPool exists, it should not have a public constructor");
        }
        try {
            Method getInstanceMethod = implementationClass.getDeclaredMethod("getInstance", int.class);
            assertTrue(Modifier.isStatic(getInstanceMethod.getModifiers()),
                    "If getInstance() is present, it should be a static method");
        } catch (NoSuchMethodException e) {
            fail("If an implementation of ConnectionPool exists, it should have a static getInstance() method");
        }

        try {
            Method resetInstanceMethod = implementationClass.getDeclaredMethod("resetInstance");
            assertTrue(Modifier.isStatic(resetInstanceMethod.getModifiers()),
                    "If resetInstance() is present, it should be a static method");

        } catch (NoSuchMethodException e) {
            fail("If an implementation of ConnectionPool exists, it should have a static resetInstance() method");
        }
    }

    @BeforeEach
    public void setUp(TestInfo info) {
        if (!info.getDisplayName().startsWith("preTest")) {
            resetInstance();
        }
    }

    private static void resetInstance() {
        try {
            Method resetInstanceMethod = implementationClass.getDeclaredMethod("resetInstance");
            resetInstanceMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetInstanceMethod() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance = getInstance();
        assertNotNull(instance, "If getInstance() is called, it should return a non-null instance");
    }

    private static ConnectionPool getInstance() {
        try {

            Method getInstanceMethod = implementationClass.getDeclaredMethod("getInstance", int.class);
            ConnectionPool instance = (ConnectionPool) getInstanceMethod.invoke(null, MAX_CONNECTIONS);
            return instance;
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void testSingletonBehavior() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance1 = getInstance();
        Assumptions.assumeTrue(instance1 != null);

        ConnectionPool instance2 = getInstance();

        assertSame(instance1, instance2,
                "If getInstance() is called multiple times, it should return the same instance");
    }

    @Test
    public void testGetConnection() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance = getInstance();
        Assumptions.assumeTrue(instance != null);

        DatabaseConnection connection = instance.getConnection();

        assertNotNull(connection, "A valid connection should be returned");

        int availableConnections = instance.getAvailableConnectionsCount();
        assertEquals(MAX_CONNECTIONS - 1, availableConnections,
                "The available connections count should decrease after obtaining a connection");
    }

    @Test
    public void testReleaseConnection() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance = getInstance();
        Assumptions.assumeTrue(instance != null);

        DatabaseConnection connection = instance.getConnection();
        assertNotNull(connection, "A valid connection should be returned");

        instance.releaseConnection(connection);

        int availableConnections = instance.getAvailableConnectionsCount();
        assertEquals(MAX_CONNECTIONS, availableConnections,
                "The available connections count should increase after releasing a connection");
    }

    @Test
    public void testAvailableConnectionsCount() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance = getInstance();
        Assumptions.assumeTrue(instance != null);

        int initialAvailableConnections = instance.getAvailableConnectionsCount();

        instance.getConnection();
        instance.getConnection();

        int availableConnections = instance.getAvailableConnectionsCount();
        assertEquals(initialAvailableConnections - 2, availableConnections,
                "The available connections count should decrease after obtaining connections");
    }

    @Test
    public void testTotalConnectionsCount() {

        Assumptions.assumeTrue(implementationClass != null);

        ConnectionPool instance = getInstance();
        Assumptions.assumeTrue(instance != null);

        int totalConnections = instance.getTotalConnectionsCount();

        assertEquals(MAX_CONNECTIONS, totalConnections,
                "The total connections count should match the maximum connections specified");
    }
}