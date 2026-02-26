/**
 * Shared base for Jaguar and HarpyEagle since they have
 * a lot of the same constants and behaviour.
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

    @Override public int getMaxAge() { return APEX_MAX_AGE; }
    @Override protected int getBreedingAge() { return APEX_BREEDING_AGE; }
    @Override protected int getMaxLitterSize() { return APEX_MAX_LITTER_SIZE; }
    @Override protected int getGenderSearchRadius() { return 20; }
    @Override protected boolean isAlwaysHungry() { return true; }
}
