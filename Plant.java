import java.util.List;

/**
 * Abstract base for all plants in the simulation.
 * Extends SimulationEntity for shared alive/location/age state.
 */
public abstract class Plant extends SimulationEntity
{
    /** Shared random source for all plant subclasses. */
    protected static final java.util.Random rand = Randomizer.getRandom();

    private final int maturityAge;

    protected Plant(Location location, int maturityAge)
    {
        super(location);
        this.maturityAge = maturityAge;
    }

    /** Each step the plant gets to act (age, spread seeds, etc.). */
    public abstract void act(Field currentField, Field nextFieldState, SimulationContext ctx);

    /** Whether this plant has reached maturity and can be eaten/spread. */
    public boolean isEdible() { return getAge() >= maturityAge; }

    /**
     * Tries to spread offspring into free adjacent cells.
     * @param spreadProb        Per-cell probability of placing an offspring.
     * @param maxOffspring      Maximum offspring per step.
     * @param requiresDaylight  If true, spreading only occurs during daylight.
     */
    protected void spreadOffspring(Field currentField, Field nextFieldState,
                                   SimulationContext ctx,
                                   double spreadProb, int maxOffspring,
                                   boolean requiresDaylight)
    {
        if(!isAlive()) return;
        if(requiresDaylight && !ctx.isDaylight()) return;
        if(!isEdible()) return;

        int placed = 0;
        for(Location loc : nextFieldState.getFreeAdjacentLocations(getLocation())) {
            if(placed >= maxOffspring) break;
            if(currentField.getPlantAt(loc) == null
                    && nextFieldState.getPlantAt(loc) == null
                    && rand.nextDouble() <= spreadProb * ctx.plantGrowthFactor()) {
                nextFieldState.placePlant(createOffspring(loc), loc);
                placed++;
            }
        }
    }

    /**
     * Create a new (young) plant of the same species at the given location.
     */
    protected abstract Plant createOffspring(Location location);
}
