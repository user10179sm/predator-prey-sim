/**
 * A jaguar — an apex predator that hunts Capybaras.
 */
public class Jaguar extends ApexPredator
{
    private static final double BREEDING_PROBABILITY = 0.10;
    private static final int PREY_FOOD_VALUE = 14;

    public Jaguar(boolean randomAge, Location location)
    {
        super(location);
        initFoodLevel(rand.nextInt(7) + 7);
        if(randomAge) setAge(rand.nextInt(APEX_MAX_AGE + 1));
    }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == Capybara.class) return PREY_FOOD_VALUE;
        return 0;
    }

    @Override protected double getBreedingProbability() { return BREEDING_PROBABILITY; }
    @Override protected Animal createYoung(Location location) { return new Jaguar(false, location); }
}
