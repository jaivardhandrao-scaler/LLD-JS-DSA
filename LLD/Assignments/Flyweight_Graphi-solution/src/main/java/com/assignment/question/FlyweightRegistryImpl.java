package com.assignment.question;

import java.util.HashMap;
import java.util.Map;

public class FlyweightRegistryImpl implements FlyweightRegistry {
    private Map<GraphicType, GraphicIntrinsicState> registry = new HashMap<>();

    @Override
    public void addFlyweight(GraphicIntrinsicState flyweight) {
        registry.put(flyweight.getType(), flyweight);
    }

    @Override
    public GraphicIntrinsicState getFlyweight(GraphicType graphicType) {
        return registry.get(graphicType);
    }
}