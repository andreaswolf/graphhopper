package com.graphhopper.storage.extensions;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.NodeAccess;

/**
 * Storage extension to hold road signs, e.g. stop signs or traffic lights.
 *
 * We might extend this to also hold speed bumps etc.
 */
public class RoadSignExtension implements GraphExtension
{
    /**
     * Positions of header definitions within the storage
     */
    final int HEADER_BYTES_PER_ENTRY = 0;
    final int HEADER_ENTRY_COUNT = 4;

    private int bytesPerEntry;
    private int entryCount = 0;

    /**
     * The storage that does the actual work for us
     */
    private DataAccess storage;

    private GraphStorage graph;
    private NodeAccess nodeAccess;

    @Override
    public boolean isRequireNodeField()
    {
        return true;
    }

    @Override
    public boolean isRequireEdgeField()
    {
        return false;
    }

    @Override
    public int getDefaultNodeFieldValue()
    {
        return 0;
    }

    @Override
    public int getDefaultEdgeFieldValue()
    {
        return 0;
    }

    @Override
    public void init(GraphStorage graph)
    {
        if (entryCount > 0)
            throw new AssertionError("The road sign extension must be initialized only once.");

        this.graph = graph;
        this.nodeAccess = graph.getNodeAccess();
        this.storage = this.graph.getDirectory().find("road_signs");
    }

    @Override
    public void setSegmentSize(int bytes)
    {
        storage.setSegmentSize(bytes);
    }

    @Override
    public GraphExtension copyTo(GraphExtension extStorage)
    {
        if (!(extStorage instanceof RoadSignExtension))
        {
            throw new IllegalStateException("the extended storage to clone must be the same");
        }

        RoadSignExtension clonedTC = (RoadSignExtension) extStorage;

        storage.copyTo(clonedTC.storage);
        clonedTC.entryCount = entryCount;

        return extStorage;
    }

    @Override
    public boolean loadExisting()
    {
        if (!storage.loadExisting())
            return false;

        bytesPerEntry = storage.getHeader(0);
        entryCount = storage.getHeader(4);
        return true;
    }

    @Override
    public GraphExtension create(long byteCount)
    {
        storage.create(byteCount * bytesPerEntry);
        return this;
    }

    @Override
    public void flush()
    {
        storage.setHeader(HEADER_BYTES_PER_ENTRY, bytesPerEntry);
        storage.setHeader(HEADER_ENTRY_COUNT, entryCount);
        storage.flush();
    }

    @Override
    public void close()
    {
        storage.close();
    }

    @Override
    public boolean isClosed()
    {
        return storage.isClosed();
    }

    @Override
    public long getCapacity()
    {
        return storage.getCapacity();
    }
}
