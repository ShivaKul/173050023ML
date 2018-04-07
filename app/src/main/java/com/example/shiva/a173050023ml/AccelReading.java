package com.example.shiva.a173050023ml;

/**
 * Created by Shiva on 4/7/2018.
 */

public class AccelReading {
    public Double x, y, z;
    public String label;
    public AccelReading(String csvLine)
    {
        String[] vals = csvLine.split(",");
        this.x = Double.parseDouble(vals[3]);
        this.y = Double.parseDouble(vals[4]);
        this.z = Double.parseDouble(vals[5]);
        this.label = vals[6];
    }
}
