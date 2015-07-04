package com.graphhopper.http;

import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;

import java.util.*;

public class JsonWriter
{

    protected GraphHopper hopper;

    public JsonWriter(GraphHopper hopper)
    {
        this.hopper = hopper;
    }

    public Map<String, Object> createJson(GHResponse rsp,
                                          boolean calcPoints,
                                          boolean pointsEncoded,
                                          boolean includeElevation,
                                          boolean enableInstructions)
    {
        Map<String, Object> json = new HashMap<String, Object>();
        Map<String, Object> jsonInfo = new HashMap<String, Object>();
        json.put("info", jsonInfo);
        jsonInfo.put("copyrights", Arrays.asList("GraphHopper", "OpenStreetMap contributors"));

        if (rsp.hasErrors())
        {
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            for (Throwable t : rsp.getErrors())
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("message", t.getMessage());
                map.put("details", t.getClass().getName());
                list.add(map);
            }
            jsonInfo.put("errors", list);
        } else
        {
            Map<String, Object> jsonPath = new HashMap<String, Object>();
            jsonPath.put("distance", Helper.round(rsp.getDistance(), 3));
            jsonPath.put("weight", Helper.round6(rsp.getDistance()));
            jsonPath.put("time", rsp.getTime());

            if (calcPoints)
            {
                jsonPath.put("points_encoded", pointsEncoded);

                PointList points = rsp.getPoints();
                if (points.getSize() >= 2)
                {
                    BBox maxBounds = hopper.getGraph().getBounds();
                    BBox maxBounds2D = new BBox(maxBounds.minLon, maxBounds.maxLon, maxBounds.minLat, maxBounds.maxLat);
                    jsonPath.put("bbox", rsp.calcRouteBBox(maxBounds2D).toGeoJson());
                }

                jsonPath.put("points", createPoints(points, pointsEncoded, includeElevation));

                if (enableInstructions)
                {
                    InstructionList instructions = rsp.getInstructions();
                    jsonPath.put("instructions", instructions.createJson());
                }
            }
            json.put("paths", Collections.singletonList(jsonPath));
        }
        return json;
    }

    protected Object createPoints(PointList points, boolean pointsEncoded, boolean includeElevation)
    {
        if (pointsEncoded)
            return WebHelper.encodePolyline(points, includeElevation);

        Map<String, Object> jsonPoints = new HashMap<String, Object>();
        jsonPoints.put("type", "LineString");
        jsonPoints.put("coordinates", points.toGeoJson(includeElevation));
        return jsonPoints;
    }
}
