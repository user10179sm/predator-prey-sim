import java.util.List;
import java.util.Random;

/**
 * A howler monkey — a herbivore that forages in the canopy on fruit trees.
 * Prey for Harpy Eagles.
 */
public class HowlerMonkey extends Animal
{
    private static final int BREEDING_AGE = 4;
    private static final int MAX_AGE = 80;
    private static final double BREEDING_PROBABILITY = 0.10;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int FRUIT_FOOD_VALUE = 14;
    private static final Random rand = Randomizer.getRandom();

    public HowlerMonkey(boolean randomAge, Location location)
    {
        super(location);
        foodLevel = rand.nextInt(FRUIT_FOOD_VALUE) + 4;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }

    @Override
    public void act(Field currentField, Field nextFieldState, boolean isNight, Weather weather)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            List<Location> freeLocations =
                nextFieldState.getFreeAdjacentLocations(getLocation());
            if(!freeLocations.isEmpty()) {
                giveBirth(currentField, nextFieldState, freeLocations);
            }
            Location nextLocation = findFood(currentField);
            // Guard: food's current-field location may already be occupied in nextFieldState.
            if(nextLocation != null && nextFieldState.getAnimalAt(nextLocation) != null) {
                nextLocation = freeLocations.isEmpty() ? null : freeLocations.remove(0);
            }
            if(nextLocation == null && !freeLocations.isEmpty()) {
                nextLocation = freeLocations.remove(0);
            }
            if(nextLocation != null) {
                setLocation(nextLocation);
                nextFieldState.placeAnimal(this, nextLocation);
            } else {
                setDead();
            }
        }
    }

    @Override
    protected boolean requiresMate() { return true; }

    @Override
    protected int getGenderSearchRadius() { return 10; }

    @Override
    protected int getHungerThreshold() { return FRUIT_FOOD_VALUE / 2; }

    @Override
    public boolean isEdibleBy(Animal predator)
    {
        return predator instanceof HarpyEagle;
    }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == FruitTree.class) return FRUIT_FOOD_VALUE;
        return 0;
    }

    @Override
    public boolean usesHunger() { return true; }

    @Override
    public int getMaxAge() { return MAX_AGE; }

    @Override
    protected int getBreedingAge() { return BREEDING_AGE; }

    @Override
    protected double getBreedingProbability() { return BREEDING_PROBABILITY; }

    @Override
    protected int getMaxLitterSize() { return MAX_LITTER_SIZE; }

    @Override
    protected Animal createYoung(Location location) { return new HowlerMonkey(false, location); }
}
