/**
 * A fruit-bearing tree that grows in the rainforest canopy.
 * Only HowlerMonkeys can eat its fruit (handled by Howler's getFoodValue).
 * Seeds disperse only during daylight.
 */
public class FruitTree extends Plant
{
    private static final int    MATURITY_AGE      = 8;
    private static final int    MAX_AGE           = 150;
    private static final double SEED_PROBABILITY  = 0.25;
    private static final int    MAX_SEEDS         = 5;

    public FruitTree(boolean randomAge, Location location)
    {
        super(location, MATURITY_AGE);
        if(randomAge) setAge(rand.nextInt(MAX_AGE + 1));
    }

    @Override
    public void act(Field currentField, Field nextFieldState, SimulationContext ctx)
    {
        incrementAge();
        if(isAlive()) {
            nextFieldState.placePlant(this, getLocation());
            // requiresDaylight = true: seeds release only in sunlight.
            spreadOffspring(currentField, nextFieldState, ctx,
                            SEED_PROBABILITY, MAX_SEEDS, true);
        }
    }

    @Override public int getMaxAge() { return MAX_AGE; }

    @Override
    protected Plant createOffspring(Location location) { return new FruitTree(false, location); }
}
