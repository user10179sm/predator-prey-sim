import java.util.Random;

/**
 * Encapsulates the current time of day and weather condition for the simulation.
 * A single instance is advanced once per simulation step and passed to every
 * act() call so that animals and plants can adjust their behaviour accordingly.
 *
 * Time: one full day = 24 simulation steps.
 *   Steps  0-4   → NIGHT  (5 steps)
 *   Steps  5-6   → DAWN   (2 steps)
 *   Steps  7-17  → DAY    (11 steps)
 *   Steps 18-19  → DUSK   (2 steps)
 *   Steps 20-23  → NIGHT  (4 steps, wraps around)
 *
 * Weather: transitions are evaluated once per full day (every 24 steps)
 * using a Markov-chain transition matrix tuned for a tropical rainforest.
 */
public class SimulationContext
{
    // ── Time of day ──────────────────────────────────────────────────────────
    public enum TimePhase { NIGHT, DAWN, DAY, DUSK }

    // ── Weather conditions ───────────────────────────────────────────────────
    public enum WeatherCondition { SUNNY, RAINY, FOGGY, STORMY }

    // Number of simulation steps per in-game day.
    private static final int STEPS_PER_DAY = 24;

    // Hour-boundary mapping: which TimePhase corresponds to each step in [0,23].
    private static final TimePhase[] PHASE_MAP = buildPhaseMap();

    /**
     * Fixed daily probabilities for each weather condition.
     * Order: SUNNY, RAINY, FOGGY, STORMY — must sum to 1.0.
     */
    private static final double[] WEATHER_PROB = { 0.45, 0.30, 0.15, 0.10 };

    private final Random rand = Randomizer.getRandom();

    // Current step within the overall simulation (monotonically increasing).
    private int totalStep = 0;

    // Step within the current day [0, STEPS_PER_DAY).
    private int stepInDay = 0;

    private TimePhase timePhase  = TimePhase.NIGHT;  // matches PHASE_MAP[0]
    private WeatherCondition weather = WeatherCondition.SUNNY;

    // ── Public query methods ─────────────────────────────────────────────────

    public TimePhase       getTimePhase() { return timePhase; }
    public WeatherCondition getWeather()  { return weather;   }
    public int             getStep()      { return totalStep; }

    public boolean isNight()    { return timePhase == TimePhase.NIGHT; }
    public boolean isDawn()     { return timePhase == TimePhase.DAWN;  }
    public boolean isDay()      { return timePhase == TimePhase.DAY;   }
    public boolean isDusk()     { return timePhase == TimePhase.DUSK;  }
    /** True during DAWN, DAY, or DUSK. */
    public boolean isDaylight() { return timePhase != TimePhase.NIGHT; }

    public boolean isSunny()    { return weather == WeatherCondition.SUNNY;   }
    public boolean isRainy()    { return weather == WeatherCondition.RAINY;   }
    public boolean isFoggy()    { return weather == WeatherCondition.FOGGY;   }
    public boolean isStormy()   { return weather == WeatherCondition.STORMY;  }

    /**
     * Multiplier applied to plant spreading probabilities.
     * Rain boosts growth; storms suppress it.
     */
    public double plantGrowthFactor()
    {
        return switch(weather) {
            case SUNNY  -> 1.0;
            case RAINY  -> 1.7;
            case FOGGY  -> 0.85;
            case STORMY -> 0.3;
        };
    }

    /**
     * Multiplier applied to predator hunting success.
     * Fog and rain reduce visibility; storms halt hunting entirely.
     */
    public double huntingSuccessFactor()
    {
        return switch(weather) {
            case SUNNY  -> 1.0;
            case RAINY  -> 0.65;
            case FOGGY  -> 0.30;
            case STORMY -> 0.0;
        };
    }

    /**
     * Whether animals can move and act this step.
     * All animals shelter during a storm.
     */
    public boolean allowsMovement()
    {
        return weather != WeatherCondition.STORMY;
    }

    /**
     * Multiplier applied to breeding probability this step.
     * Storms halt reproduction; disease-suppression is handled separately in Animal.
     */
    public double breedingFactor()
    {
        return switch(weather) {
            case SUNNY  -> 1.0;
            case RAINY  -> 0.8;
            case FOGGY  -> 0.9;
            case STORMY -> 0.0;
        };
    }

    // ── Advance one simulation step ──────────────────────────────────────────

    /**
     * Advance the context by one simulation step.
     * Call this once at the top of simulateOneStep() before any act() calls.
     */
    public void advance()
    {
        totalStep++;
        stepInDay = totalStep % STEPS_PER_DAY;
        timePhase = PHASE_MAP[stepInDay];

        // Transition weather once per in-game day (at step 0 of each new day).
        if(stepInDay == 0) {
            weather = nextWeather();
        }
    }

    // ── Display ──────────────────────────────────────────────────────────────

    /** Short human-readable status string for the UI header. */
    public String toDisplayString()
    {
        String timeIcon = switch(timePhase) {
            case NIGHT -> "Night";
            case DAWN  -> "Dawn";
            case DAY   -> "Day";
            case DUSK  -> "Dusk";
        };
        String weatherIcon = switch(weather) {
            case SUNNY  -> "Sunny";
            case RAINY  -> "Rainy";
            case FOGGY  -> "Foggy";
            case STORMY -> "Storm";
        };
        return timeIcon + "  " + weatherIcon;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private WeatherCondition nextWeather()
    {
        double roll = rand.nextDouble();
        double cumulative = 0.0;
        for(int i = 0; i < WeatherCondition.values().length; i++) {
            cumulative += WEATHER_PROB[i];
            if(roll < cumulative) {
                return WeatherCondition.values()[i];
            }
        }
        return WeatherCondition.values()[WeatherCondition.values().length - 1];
    }

    private static TimePhase[] buildPhaseMap()
    {
        TimePhase[] map = new TimePhase[STEPS_PER_DAY];
        for(int i = 0; i < STEPS_PER_DAY; i++) {
            if(i < 5 || i >= 20) {
                map[i] = TimePhase.NIGHT;
            } else if(i < 7) {
                map[i] = TimePhase.DAWN;
            } else if(i < 18) {
                map[i] = TimePhase.DAY;
            } else {
                map[i] = TimePhase.DUSK;
            }
        }
        return map;
    }
}
