/**
 * Intermediate abstract class shared by Jaguar and HarpyEagle.
 * Consolidates the constants and behaviour hooks that are identical for both
 * apex predators, removing the duplication that existed when each class
 * declared MAX_AGE, BREEDING_AGE, etc. independently.
 */
public abstract class ApexPredator extends Animal
{
    // Both apex predators share the same lifespan and breeding characteristics.
    protected static final int    APEX_MAX_AGE         = 150;
    protected static final int    APEX_BREEDING_AGE    = 7;
    protected static final int    APEX_MAX_LITTER_SIZE = 1;

    protected ApexPredator(Location location)
    {
        super(location);
    }

    // ─── Hook overrides shared by all apex predators ──────────────────────────

    @Override public int     getMaxAge()            { return APEX_MAX_AGE; }
    @Override protected int  getBreedingAge()       { return APEX_BREEDING_AGE; }
    @Override protected int  getMaxLitterSize()     { return APEX_MAX_LITTER_SIZE; }
    @Override public    boolean usesHunger()        { return true; }
    @Override protected boolean requiresMate()      { return true; }
    @Override protected int  getGenderSearchRadius(){ return 20; }

    /**
     * Apex predators hunt on every step regardless of foodLevel.
     * Using isAlwaysHungry() rather than a giant getHungerThreshold() sentinel
     * expresses the intent clearly without abusing the threshold contract.
     */
    @Override protected boolean isAlwaysHungry() { return true; }
}
