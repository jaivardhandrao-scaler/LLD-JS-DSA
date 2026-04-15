package com.assignment.question;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GraphicTest {

    private static Class<?> intrinsicStateClass;
    private static Class<?> extrinsicStateClass;

    private static Class<? extends FlyweightRegistry> registryClass;


    @BeforeAll
    public static void setUp() {

        String packageName = GraphicTest.class.getPackageName();
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false), new TypeAnnotationsScanner());

        intrinsicStateClass = loadClass(reflections, IntrinsicState.class);
        extrinsicStateClass = loadClass(reflections, ExtrinsicState.class);

        Set<Class<? extends FlyweightRegistry>> candidates = reflections.getSubTypesOf(FlyweightRegistry.class);
        if (candidates.isEmpty()) {
            fail("If the registry pattern is implemented, there should be a class implementing FlyweightRegistry interface");
        }

        registryClass = candidates.stream().findFirst().get();
    }

    private static Class<?> loadClass(Reflections reflections, Class<? extends Annotation> annotation) {

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation);
        if (classes.isEmpty()) {
            fail("If flyweight pattern is implemented, there should be a class annotated with " + annotation.getSimpleName());
        }
        if (classes.size() > 1) {
            fail("There should be exactly one class annotated with " + annotation.getSimpleName());
        }

        return classes.iterator().next();
    }

    @Test
    public void testIntrinsicStateFields() {

        Field[] fields = intrinsicStateClass.getDeclaredFields();
        assertNotNull(fields, "If intrinsic state class is implemented, it should have fields");

        String message = "If intrinsic state class is implemented, it should have the correct fields";
        assertTrue(hasField(fields, "image"), message);
        assertTrue(hasField(fields, "width"), message);
        assertTrue(hasField(fields, "height"), message);
        assertTrue(hasField(fields, "color"), message);
    }

    @Test
    public void testExtrinsicStateFields() {
        Field[] fields = extrinsicStateClass.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> System.out.println(field.getName()));

        assertNotNull(fields, "If extrinsic state class is implemented, it should have fields");

        String message = "If extrinsic state class is implemented, it should have the correct fields";
        assertTrue(hasField(fields, "x"), message);
        assertTrue(hasField(fields, "y"), message);
    }

    private boolean hasField(Field[] fields, String fieldName) {
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testIntrinsicReferenceField() {
        Field[] fields = extrinsicStateClass.getDeclaredFields();
        assertNotNull(fields, "If extrinsic state class is implemented, it should have fields");
        assertTrue(hasFieldOfType(fields, intrinsicStateClass), "If extrinsic state class is implemented, it should have the correct fields");
    }

    private boolean hasFieldOfType(Field[] fields, Class<?> type) {
        for (Field field : fields) {
            if (field.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testRegistry() {
        Field[] fields = registryClass.getDeclaredFields();
        boolean hasRegistryMap = Arrays.stream(fields).anyMatch(field -> field.getType().equals(Map.class));
        assertTrue(hasRegistryMap, "If the registry pattern is implemented, it should have a map for storing and fetching flyweights");
    }
}