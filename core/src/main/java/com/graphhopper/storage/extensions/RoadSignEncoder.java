package com.graphhopper.storage.extensions;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.NodeAccess;


public class RoadSignEncoder
{
    /**
     * Positions of bits for encoding various pieces of information
     */
    final byte TRAFFIC_LIGHT_BIT = 1;

    RoadSignExtension storage;

    NodeAccess nodes;

    public RoadSignEncoder(Graph graph)
    {
        if (!(graph.getExtension() instanceof RoadSignExtension))
        {
            throw new IllegalArgumentException("Wrong type of storage extension");
        }
        storage = (RoadSignExtension)graph.getExtension();
        nodes = graph.getNodeAccess();
    }

    public boolean hasTrafficLight(int nodeId)
    {
        int nodeFieldValue = nodes.getAdditionalNodeField(nodeId);

        return hasBitSet(nodeFieldValue, TRAFFIC_LIGHT_BIT);
    }

    public int markTrafficLight(int nodeId, boolean value)
    {
        int nodeFieldValue = nodes.getAdditionalNodeField(nodeId);
        nodeFieldValue = setBitInField(nodeFieldValue, TRAFFIC_LIGHT_BIT, value);
        nodes.setAdditionalNodeField(nodeId, nodeFieldValue);

        return nodeFieldValue;
    }

    private boolean hasBitSet(int value, byte bitPosition)
    {
        return ((value >>> bitPosition - 1) & 1) != 0;
    }

    private int setBitInField(int currentValue, byte bitPosition, boolean value)
    {
        int mask = 1 << (bitPosition - 1);

        // clear bit
        currentValue &= ~mask;

        // set bit if necessary
        if (value)
        {
            currentValue |= mask;
        }

        return currentValue;
    }

}
