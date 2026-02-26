/**
 * A harpy eagle — an apex predator that hunts Howler Monkeys primarily, and
 * Capybaras opportunistically when hungry.
 */
public class HarpyEagle extends ApexPredator
{
    private static final double BREEDING_PROBABILITY = 0.05;
    private static final int PREY_FOOD_VALUE = 9;

    public HarpyEagle(boolean randomAge, Location location)
    {
        super(location);
        initFoodLevel(rand.nextInt(7) + 7);
        if(randomAge) setAge(rand.nextInt(APEX_MAX_AGE + 1));
    }

    /**
     * Eagle prefers HowlerMonkey (always eats if adjacent).
     * Hunts Capybara only when below half-full, i.e. when monkey is scarce.
     */
    @Override
    protected boolean canEat(Class<?> preyClass)
    {
        if(preyClass == Capybara.class) {
            return getFoodLevel() * 2 < PREY_FOOD_VALUE;
        }
        return true;
    }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == HowlerMonkey.class) return PREY_FOOD_VALUE;
        if(preyClass == Capybara.class)     return PREY_FOOD_VALUE;
        return 0;
    }

    @Override protected double getBreedingProbability() { return BREEDING_PROBABILITY; }
    @Override protected Animal createYoung(Location location) { return new HarpyEagle(false, location); }
}
