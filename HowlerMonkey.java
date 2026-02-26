/**
 * A howler monkey — a herbivore that forages in the canopy on fruit trees.
 * Primary prey for Harpy Eagles.
 */
public class HowlerMonkey extends Animal
{
    private static final int BREEDING_AGE = 4;
    private static final int MAX_AGE = 80;
    private static final double BREEDING_PROBABILITY = 0.14;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int FRUIT_FOOD_VALUE = 14;

    public HowlerMonkey(boolean randomAge, Location location)
    {
        super(location);
        initFoodLevel(rand.nextInt(FRUIT_FOOD_VALUE) + 4);
        if(randomAge) setAge(rand.nextInt(MAX_AGE + 1));
    }

    @Override protected int getGenderSearchRadius() { return 10; }
    @Override protected int getHungerThreshold() { return FRUIT_FOOD_VALUE / 2; }
    @Override public int getMaxAge() { return MAX_AGE; }
    @Override protected int getBreedingAge() { return BREEDING_AGE; }
    @Override protected double getBreedingProbability() { return BREEDING_PROBABILITY; }
    @Override protected int getMaxLitterSize() { return MAX_LITTER_SIZE; }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == FruitTree.class) return FRUIT_FOOD_VALUE;
        return 0;
    }

    @Override
    protected Animal createYoung(Location location) { return new HowlerMonkey(false, location); }
}
