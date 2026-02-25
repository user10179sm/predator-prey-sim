import java.util.Random;

/**
 * States of weather
 */
public enum Weather
{
    CLEAR("Clear"),
    RAIN("Rain"),
    FOG("Fog");

    private final String label;

    Weather(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public static Weather random(Random rand)
    {
        double roll = rand.nextDouble();
        if(roll < 0.50) {
            return CLEAR;
        }
        if(roll < 0.85) {
            return RAIN;
        }
        return FOG;
    }
}
