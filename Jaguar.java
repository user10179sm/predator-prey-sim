import java.util.List;
import java.util.Random;

/**
 * A jaguar — an apex predator that hunts Capybaras and Howler Monkeys.
 * Competes directly with the Harpy Eagle for the same prey.
 */
public class Jaguar extends Animal
{
    private static final int BREEDING_AGE = 7;
    private static final int MAX_AGE = 150;
    private static final double BREEDING_PROBABILITY = 0.04;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int PREY_FOOD_VALUE = 14;
    private static final Random rand = Randomizer.getRandom();

    public Jaguar(boolean randomAge, Location location)
    {
        super(location);
        foodLevel = rand.nextInt(7) + 7;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }

    @Override
    public void act(Field currentField, Field nextFieldState)
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
            // Guard: prey's current-field location may already be occupied in nextFieldState.
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
    protected boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }

    @Override
    protected int getHungerThreshold() { return MAX_AGE * 10; } // always hunt

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == Capybara.class) return PREY_FOOD_VALUE;
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
    protected Animal createYoung(Location location) { return new Jaguar(false, location); }
}
