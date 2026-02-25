import java.util.List;
import java.util.Random;

/**
 * A fern plant that grows on the rainforest floor. Once mature it can spread
 * spores into adjacent free cells and be grazed by herbivores.
 */
public class Fern extends Plant
{
    private static final int MATURITY_AGE = 10;
    private static final int MAX_AGE = 80;
    private static final double SPORE_PROBABILITY = 0.35;
    private static final int MAX_SPORES = 5;
    private static final Random rand = Randomizer.getRandom();

    public Fern(boolean randomAge, Location location)
    {
        super(location, MATURITY_AGE);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }

    @Override
    public void act(Field currentField, Field nextFieldState)
    {
        incrementAge();
        if(isAlive()) {
            nextFieldState.placePlant(this, getLocation());
            if(isEdible()) {
                List<Location> free = nextFieldState.getFreeAdjacentLocations(getLocation());
                for(Location loc : free) {
                    if(nextFieldState.getPlantAt(loc) == null
                            && rand.nextDouble() <= SPORE_PROBABILITY
                            && rand.nextInt(MAX_SPORES + 1) > 0) {
                        nextFieldState.placePlant(new Fern(false, loc), loc);
                    }
                }
            }
        }
    }

    @Override
    public int getMaxAge()
    {
        return MAX_AGE;
    }

    @Override
    public boolean isEdibleBy(Animal predator)
    {
        return predator instanceof Capybara && isEdible();
    }
}
