/**
 * A capybara — a large herbivore that grazes on ferns.
 * Primary prey for Jaguars; also hunted by Harpy Eagles when food is scarce.
 */
public class Capybara extends Animal
{
    private static final int BREEDING_AGE = 5;
    private static final int MAX_AGE = 80;
    private static final double BREEDING_PROBABILITY = 0.10;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int FERN_FOOD_VALUE = 12;

    public Capybara(boolean randomAge, Location location)
    {
        super(location);
        initFoodLevel(rand.nextInt(FERN_FOOD_VALUE) + 6);
        if(randomAge) setAge(rand.nextInt(MAX_AGE + 1));
    }

    @Override protected int getGenderSearchRadius() { return 10; }
    @Override protected int getHungerThreshold() { return FERN_FOOD_VALUE / 2; }
    @Override public int getMaxAge() { return MAX_AGE; }
    @Override protected int getBreedingAge() { return BREEDING_AGE; }
    @Override protected double getBreedingProbability() { return BREEDING_PROBABILITY; }
    @Override protected int getMaxLitterSize() { return MAX_LITTER_SIZE; }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == Fern.class) return FERN_FOOD_VALUE;
        return 0;
    }

    @Override
    protected Animal createYoung(Location location) { return new Capybara(false, location); }
}
