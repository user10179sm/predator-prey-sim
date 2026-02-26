/**
 * Intermediate abstract class shared by Jaguar and HarpyEagle.
 * Consolidates the constants and behaviour common to both apex predators.
 */
public abstract class ApexPredator extends Animal
{
    protected static final int APEX_MAX_AGE = 150;
    protected static final int APEX_BREEDING_AGE = 7;
    protected static final int APEX_MAX_LITTER_SIZE = 1;

    protected ApexPredator(Location location)
    {
        super(location);
    }

    @Override public int getMaxAge()               { return APEX_MAX_AGE; }
    @Override protected int getBreedingAge()       { return APEX_BREEDING_AGE; }
    @Override protected int getMaxLitterSize()     { return APEX_MAX_LITTER_SIZE; }
    @Override protected int getGenderSearchRadius(){ return 20; }
    @Override protected boolean isAlwaysHungry()   { return true; }
}
