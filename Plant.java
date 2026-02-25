import java.util.List;

/**
 * Abstract base for all plants in the simulation.
 * Extends SimulationEntity for shared alive/location/age state.
 *
 * The spreadOffspring() template method captures the common spreading
 * logic so Fern and FruitTree don't duplicate it.
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
     * Template method — called from subclass act() after incrementAge().
     * Ages this plant, then if mature (and daylight conditions are met) tries
     * to spread offspring into free adjacent cells.
     *
     * @param spreadProb        Per-cell probability of placing an offspring.
     * @param maxOffspring      Upper bound on offspring per step.
     * @param requiresDaylight  If true, spreading only occurs at daylight.
     */
    protected void spreadOffspring(Field currentField, Field nextFieldState,
                                   SimulationContext ctx,
                                   double spreadProb, int maxOffspring,
                                   boolean requiresDaylight)
    {
        if(!isAlive()) return;
        if(requiresDaylight && !ctx.isDaylight()) return;
        if(!isEdible()) return;

        // R-08: use a placement counter to correctly cap offspring at maxOffspring.
        // The old code used rand.nextInt(maxOffspring+1) > 0 as a stochastic gate
        // (succeeds 5/6 of the time for MAX_SPORES=5) which obscured the true
        // spread probability and had no genuine max-count semantics.
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
     * Subclasses implement to return the correct concrete type.
     */
    protected abstract Plant createOffspring(Location location);
}
