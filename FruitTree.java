import java.util.List;
import java.util.Random;

/**
 * A fruit-bearing tree that grows in the rainforest canopy.
 * Only HowlerMonkeys can eat its fruit. Once mature it drops seeds
 * into adjacent free cells to spread.
 */
public class FruitTree extends Plant
{
    private static final int MATURITY_AGE = 8;
    private static final int MAX_AGE = 100;
    private static final double SEED_PROBABILITY = 0.28;
    private static final int MAX_SEEDS = 5;
    private static final Random rand = Randomizer.getRandom();

    public FruitTree(boolean randomAge, Location location)
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
                            && rand.nextDouble() <= SEED_PROBABILITY
                            && rand.nextInt(MAX_SEEDS + 1) > 0) {
                        nextFieldState.placePlant(new FruitTree(false, loc), loc);
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
        return predator instanceof HowlerMonkey && isEdible();
    }
}
