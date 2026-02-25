/**
 * A fern plant that grows on the rainforest floor. Once mature it can spread
 * spores into adjacent free cells and be grazed by Capybaras.
 */
public class Fern extends Plant
{
    private static final int    MATURITY_AGE      = 10;
    private static final int    MAX_AGE           = 80;
    private static final double SPORE_PROBABILITY = 0.30;
    private static final int    MAX_SPORES        = 5;

    public Fern(boolean randomAge, Location location)
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
            spreadOffspring(currentField, nextFieldState, ctx,
                            SPORE_PROBABILITY, MAX_SPORES, false);
        }
    }

    @Override public int getMaxAge() { return MAX_AGE; }

    @Override
    protected Plant createOffspring(Location location) { return new Fern(false, location); }
}
