/*
 *  Licensed to Peter Karich under one or more contributor license
 *  agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  Peter Karich licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.reader.dem;

import com.graphhopper.storage.*;
import com.graphhopper.util.BitUtil;
import com.graphhopper.util.Downloader;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elevation data from NASA (SRTM). Downloaded from http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/
 * <p/>
 * Important information about SRTM: the coordinates of the lower-left corner of tile N40W118 are 40
 * degrees north latitude and 118 degrees west longitude. To be more exact, these coordinates refer
 * to the geometric center of the lower left sample, which in the case of SRTM3 data will be about
 * 90 meters in extent.
 * <p/>
 *
 * @author Peter Karich
 */
public class HighPrecisionSRTMProvider extends SRTMProvider
{
    public static void main(String[] args) throws IOException
    {
        HighPrecisionSRTMProvider provider = new HighPrecisionSRTMProvider();
        // 1046
        System.out.println(provider.getEle(47.468668, 14.575127));
        // 1113
        System.out.println(provider.getEle(47.467753, 14.573911));

        // 1946
        System.out.println(provider.getEle(46.468835, 12.578777));

        // 845
        System.out.println(provider.getEle(48.469123, 9.576393));

        // 1113 vs new: 
        provider.setCalcMean(true);
        System.out.println(provider.getEle(47.467753, 14.573911));
    }

    private static final BitUtil BIT_UTIL = BitUtil.BIG;
    // use a map as an array is not quite useful if we want to hold only parts of the world
    private final TIntObjectHashMap<HeightTile> cacheData = new TIntObjectHashMap<HeightTile>();
    private final double precision = 1e7;
    private final double invPrecision = 1 / precision;
    // mirror: base = "http://mirror.ufs.ac.za/datasets/SRTM3/"

    private LowPrecisionSRTMProvider lowResProvider = new LowPrecisionSRTMProvider();

    public HighPrecisionSRTMProvider()
    {
        WIDTH = 3601;
        baseUrl = "http://e4ftl01.cr.usgs.gov/SRTM/SRTMGL1.003/2000.02.11/";
    }

    String getFileString(double lat, double lon)
    {
        return getElevationAsString(lat, lon)  + ".SRTMGL1";
    }

    private String getElevationAsString(double lat, double lon)
    {
        int minLat = Math.abs(down(lat));
        int minLon = Math.abs(down(lon));

        return String.format("%s%02d%s%03d",
                (lat >= 0) ? "N" : "S",
                minLat,
                (lon >= 0) ? "E" : "W",
                minLon
        );
    }

    public double getEle(double lat, double lon)
    {
        try
        {
            return super.getEle(lat, lon);
        } catch (RuntimeException ex)
        {
            logger.warn("Falling back to low-resolution SRTM data for " + getElevationAsString(lat, lon));
            return lowResProvider.getEle(lat, lon);
        }
    }

    @Override
    public String toString()
    {
        return "HighPrecisionSRTM";
    }

}
