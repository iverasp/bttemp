package com.iegget.temp;

import android.widget.ImageView;

/**
 * Created by iver on 4/2/14.
 */
public class Food {

    public enum Type {
        POULTRY,
        BEEF,
        PORK,
        LAMB
    }

    private String name;
    private String description;
    private double temperature;
    private Type type;
    private int icon;

    public Food(String name, String description, double temperature, Type type) {
        this.name = name;
        this.description = description;
        this.temperature = temperature;
        this.type = type;
        icon = R.drawable.ic_launcher;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTemperature() {
        return String.valueOf(this.temperature);
    }

    public Type getType() {
        return this.type;
    }

    public int getIcon() {
        switch (type) {
            case POULTRY:
                return R.drawable.ic_poultry;
            case BEEF:
                return R.drawable.ic_beef;
            case PORK:
                return R.drawable.ic_pork;
            case LAMB:
                return R.drawable.ic_lamb;
            default:
                return icon;
        }

    }
}
