import java.util.Random;

/**
 * Keeps track of the time of day and weather as the simulation runs.
 * Each step is one hour, so a full day cycle takes 24 steps.
 * Weather gets re-rolled at midnight and again at dawn.
 */
public class SimulationContext
{
    public enum TimePhase { NIGHT, DAWN, DAY, DUSK }

    public enum WeatherCondition { SUNNY, RAINY, FOGGY, STORMY }

    private static final int STEPS_PER_DAY = 24;

    // probability weights for each weather type (day can be sunny, night can't)
    private static final double[] DAY_WEATHER_PROB = { 0.45, 0.30, 0.15, 0.10 };
    private static final double[] NIGHT_WEATHER_PROB = { 0.40, 0.35, 0.25 };

    private final Random rand = Randomizer.getRandom();

    private int totalStep = 0;
    private int stepInDay = 0;

    private TimePhase timePhase = TimePhase.NIGHT;
    private WeatherCondition weather = WeatherCondition.FOGGY;


    public TimePhase getTimePhase() { return timePhase; }
    public WeatherCondition getWeather() { return weather; }
    public int getStep() { return totalStep; }

    public boolean isNight() { return timePhase == TimePhase.NIGHT; }
    public boolean isDawn() { return timePhase == TimePhase.DAWN; }
    public boolean isDay() { return timePhase == TimePhase.DAY; }
    public boolean isDusk() { return timePhase == TimePhase.DUSK; }
    public boolean isDaylight() { return timePhase != TimePhase.NIGHT; }  // not night

    public boolean isSunny() { return weather == WeatherCondition.SUNNY; }
    public boolean isRainy() { return weather == WeatherCondition.RAINY; }
    public boolean isFoggy() { return weather == WeatherCondition.FOGGY; }
    public boolean isStormy() { return weather == WeatherCondition.STORMY; }

    // Rain boosts growth, storms stunt it.
    public double plantGrowthFactor()
    {
        if(weather == WeatherCondition.RAINY) return 1.7;
        if(weather == WeatherCondition.FOGGY) return 0.85;
        if(weather == WeatherCondition.STORMY) return 0.3;
        return 1.0;
    }

    // Storms make hunting impossible, fog and rain also reduce it.
    public double huntingSuccessFactor()
    {
        if(weather == WeatherCondition.STORMY) return 0.0;
        if(weather == WeatherCondition.FOGGY) return 0.30;
        if(weather == WeatherCondition.RAINY) return 0.65;
        return 1.0;
    }

    public boolean allowsMovement()
    {
        return weather != WeatherCondition.STORMY;
    }

    // No breeding during storms.
    public double breedingFactor()
    {
        if(weather == WeatherCondition.STORMY) return 0.0;
        else if(weather == WeatherCondition.RAINY) return 0.8;
        else if(weather == WeatherCondition.FOGGY) return 0.9;
        return 1.0;
    }


    public void advance()
    {
        totalStep++;
        stepInDay = totalStep % STEPS_PER_DAY;
        timePhase = phaseFor(stepInDay);

        if(stepInDay == 0 || stepInDay == 20) {
            weather = drawWeather(
                new WeatherCondition[]{ WeatherCondition.RAINY, WeatherCondition.FOGGY, WeatherCondition.STORMY },
                NIGHT_WEATHER_PROB);
        } else if(stepInDay == 5) {
            weather = drawWeather(WeatherCondition.values(), DAY_WEATHER_PROB);
        }
    }


    public String toDisplayString()
    {
        String timeIcon;
        if(timePhase == TimePhase.NIGHT) timeIcon = "Night";
        else if(timePhase == TimePhase.DAWN) timeIcon = "Dawn";
        else if(timePhase == TimePhase.DAY) timeIcon = "Day";
        else timeIcon = "Dusk";

        String weatherIcon;
        if(weather == WeatherCondition.SUNNY) weatherIcon = "Sunny";
        else if(weather == WeatherCondition.RAINY) weatherIcon = "Rainy";
        else if(weather == WeatherCondition.FOGGY) weatherIcon = "Foggy";
        else weatherIcon = "Storm";

        return timeIcon + "  " + weatherIcon;
    }


    private WeatherCondition drawWeather(WeatherCondition[] conditions, double[] probs)
    {
        double roll = rand.nextDouble();
        double cumulative = 0.0;
        for(int i = 0; i < conditions.length; i++) {
            cumulative += probs[i];
            if(roll < cumulative) return conditions[i];
        }
        return conditions[conditions.length - 1];
    }

    private static TimePhase phaseFor(int step)
    {
        if(step < 5 || step >= 20) return TimePhase.NIGHT;
        if(step < 7) return TimePhase.DAWN;
        if(step < 18) return TimePhase.DAY;
        return TimePhase.DUSK;
    }
}
