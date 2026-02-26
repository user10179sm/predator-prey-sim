import java.util.List;
import java.util.Set;

/**
 * Abstract base for all animals in the simulation.
 * Extends SimulationEntity for shared alive/location/age state.
 */
public abstract class Animal extends SimulationEntity
{
    /** The two possible sexes for any animal. */
    public enum Gender { MALE, FEMALE }

    // Current hunger level
    private int foodLevel = 0;

    // Gender assigned once at birth.
    private final Gender gender;

    private static final double INFECTION_SPREAD_CHANCE = 0.20;
    private static final double INFECTION_DEATH_CHANCE = 0.003;
    private static final int INFECTION_DURATION = 50;
    private static final int IMMUNITY_DURATION = 30;

    // Disease state
    private boolean infected = false;
    private int infectionAge = 0;
    private int immuneAge = 0;

    /** Shared random source available to all subclasses. */
    protected static final java.util.Random rand = Randomizer.getRandom();

    protected Animal(Location location)
    {
        super(location);
        this.gender = rand.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    /** Set the starting foodLevel (called from subclass constructors). */
    protected void initFoodLevel(int level) { foodLevel = level; }

    /** Search radius (cells) for mate detection. Default: 2. */
    protected int getGenderSearchRadius() { return 2; }

    protected boolean isActiveNow(SimulationContext ctx)
    {
        return ctx.isDaylight() && ctx.allowsMovement();
    }

    public Gender getGender() { return gender; }

    protected boolean hasOppositeGenderNeighbour(Field currentField)
    {
        for(Location loc : currentField.getLocationsWithinRadius(getLocation(), getGenderSearchRadius())) {
            Animal neighbour = currentField.getAnimalAt(loc);
            if(neighbour != null
                    && neighbour.isAlive()
                    && neighbour.getClass() == this.getClass()
                    && neighbour.getGender() != this.gender) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Execute one simulation step for this animal.
     */
    public final void act(Field currentField, Field nextFieldState, SimulationContext ctx)
    {
        incrementAge();
        if(!isAlive()) return;

        progressDisease();
        if(!isAlive()) return;

        trySpreadDisease(currentField);

        if(!isActiveNow(ctx)) {
            Location stay = getLocation();
            if(nextFieldState.getAnimalAt(stay) == null) {
                nextFieldState.placeAnimal(this, stay);
            } else {
                List<Location> nearby = nextFieldState.getFreeAdjacentLocations(stay);
                if(!nearby.isEmpty()) {
                    Location fallback = nearby.remove(0);
                    setLocation(fallback);
                    nextFieldState.placeAnimal(this, fallback);
                } else {
                    setDead();
                }
            }
            return;
        }

        incrementHunger();
        if(!isAlive()) return;

        List<Location> birthSlots = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!birthSlots.isEmpty()) {
            giveBirth(currentField, nextFieldState, birthSlots, ctx);
        }

        Location nextLocation = findFood(currentField, ctx);
        List<Location> moveSlots = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(nextLocation != null && nextFieldState.getAnimalAt(nextLocation) != null) {
            nextLocation = moveSlots.isEmpty() ? null : moveSlots.remove(0);
        }
        if(nextLocation == null && !moveSlots.isEmpty()) {
            nextLocation = moveSlots.remove(0);
        }
        if(nextLocation != null) {
            setLocation(nextLocation);
            nextFieldState.placeAnimal(this, nextLocation);
        } else {
            setDead();
        }
    }

    protected void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) setDead();
    }

    protected int getFoodValue(Class<?> preyClass) { return 0; }

    public boolean hasFoodSource(Set<Class<?>> aliveSpecies)
    {
        for(Class<?> c : aliveSpecies) {
            if(getFoodValue(c) > 0) return true;
        }
        return false;
    }

    /** Current food level. */
    protected int getFoodLevel() { return foodLevel; }

    protected int getHungerThreshold() { return 0; }

    protected boolean isAlwaysHungry() { return false; }

    protected boolean canEat(Class<?> preyClass) { return true; }

    protected Location findFood(Field field, SimulationContext ctx)
    {
        if(!isAlwaysHungry() && foodLevel > getHungerThreshold()) return null;

        for(Location loc : field.getAdjacentLocations(getLocation())) {

            // Animal prey
            Animal animal = field.getAnimalAt(loc);
            if(animal != null && animal.isAlive()
                    && getFoodValue(animal.getClass()) > 0
                    && canEat(animal.getClass())) {
                if(ctx != null && rand.nextDouble() > ctx.huntingSuccessFactor()) continue;
                if(animal.isInfected()) infect();
                animal.setDead();
                foodLevel = getFoodValue(animal.getClass());
                return loc;
            }

            // Plant prey
            Plant plant = field.getPlantAt(loc);
            if(plant != null && plant.isAlive()
                    && plant.isEdible()
                    && getFoodValue(plant.getClass()) > 0) {
                plant.setDead();
                field.clearPlant(loc);
                foodLevel = getFoodValue(plant.getClass());
                return loc;
            }
        }
        return null;
    }

    protected abstract int getBreedingAge();
    protected abstract double getBreedingProbability();
    protected abstract int getMaxLitterSize();
    protected abstract Animal createYoung(Location location);

    protected boolean canBreed() { return getAge() >= getBreedingAge(); }

    private int breed(SimulationContext ctx)
    {
        double bp = getBreedingProbability()
                * ctx.breedingFactor()
                * (infected ? 0.4 : 1.0);
        return (canBreed() && rand.nextDouble() < bp)
                ? rand.nextInt(getMaxLitterSize()) + 1 : 0;
    }

    protected void giveBirth(Field currentField, Field nextFieldState,
                             List<Location> freeLocations, SimulationContext ctx)
    {
        if(!hasOppositeGenderNeighbour(currentField)) return;
        int births = breed(ctx);
        for(int b = 0; b < births && !freeLocations.isEmpty(); b++) {
            Location loc = freeLocations.remove(0);
            nextFieldState.placeAnimal(createYoung(loc), loc);
        }
    }

    /** Whether this animal is currently infected. */
    public boolean isInfected() { return infected; }

    /** Whether this animal is currently immune. */
    public boolean isImmune() { return immuneAge > 0; }

    /**
     * Attempt to infect this animal.  Has no effect if already infected or immune.
     */
    public void infect()
    {
        if(!infected && !isImmune()) {
            infected = true;
            infectionAge = 0;
        }
    }

    protected void progressDisease()
    {
        if(infected) {
            infectionAge++;
            if(rand.nextDouble() < INFECTION_DEATH_CHANCE) {
                setDead();
                return;
            }
            if(infectionAge >= INFECTION_DURATION) {
                infected = false;
                infectionAge = 0;
                immuneAge = IMMUNITY_DURATION;
            }
        } else if(immuneAge > 0) {
            immuneAge--;
        }
    }

    protected void trySpreadDisease(Field field)
    {
        if(!infected) return;
        for(Location loc : field.getAdjacentLocations(getLocation())) {
            Animal neighbour = field.getAnimalAt(loc);
            if(neighbour != null && neighbour.isAlive()
                    && rand.nextDouble() < INFECTION_SPREAD_CHANCE) {
                neighbour.infect();
            }
        }
    }


    @Override
    public String toString()
    {
        String s = getClass().getSimpleName() + "{"
                + "age=" + getAge()
                + ", alive=" + isAlive()
                + ", location=" + getLocation();
        if(infected)        s += ", INFECTED";
        else if(isImmune()) s += ", IMMUNE";
        s += ", foodLevel=" + foodLevel;
        s += '}';
        return s;
    }
}
