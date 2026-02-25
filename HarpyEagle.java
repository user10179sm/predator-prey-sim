import java.util.List;
import java.util.Random;

/**
 * A harpy eagle — an apex predator that hunts Capybaras and Howler Monkeys.
 * Competes directly with the Jaguar for the same prey.
 */
public class HarpyEagle extends Animal
{
    private static final int BREEDING_AGE = 7;
    private static final int MAX_AGE = 150;
    private static final double BREEDING_PROBABILITY = 0.04;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int PREY_FOOD_VALUE = 14;
    private static final Random rand = Randomizer.getRandom();

    public HarpyEagle(boolean randomAge, Location location)
    {
        super(location);
        foodLevel = rand.nextInt(7) + 7;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }

    @Override
    public void act(Field currentField, Field nextFieldState, boolean isNight)
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

    /**
     * Eagle hunts HowlerMonkey as primary prey (always).
     * It only hunts Capybara when nearly starving (food < PFV/3),
     * creating emergency competition with Jaguar (Task 1).
     */
    @Override
    protected boolean canEat(Class<?> preyClass)
    {
        if(preyClass == Capybara.class) {
            return foodLevel < PREY_FOOD_VALUE / 3;  // emergency: only when nearly starving
        }
        return true;  // primary prey (HowlerMonkey): always willing to eat
    }

    @Override
    protected int getFoodValue(Class<?> preyClass)
    {
        if(preyClass == HowlerMonkey.class) return PREY_FOOD_VALUE;
        if(preyClass == Capybara.class) return PREY_FOOD_VALUE;  // competes with jaguar
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
    protected Animal createYoung(Location location) { return new HarpyEagle(false, location); }
}
