
/**
 * Common elements of foxes and rabbits.
 *
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public abstract class Animal
{
    /** The two possible sexes for any animal. */
    public enum Gender { MALE, FEMALE }

    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's position.
    private Location location;
    // The animal's gender, assigned randomly at birth.
    private final Gender gender;
    // Shared instance state for subclasses
    protected int age;
    protected int foodLevel;

    /**
     * Return the maximum age for this animal species.
     */
    abstract public int getMaxAge();

    /**
     * Whether this animal uses hunger mechanics (foodLevel).
     * Subclasses may override; default is false.
     */
    public boolean usesHunger()
    {
        return false;
    }

    /**
     * Constructor for objects of class Animal.
     * @param location The animal's location.
     */
    public Animal(Location location)
    {
        this.alive = true;
        this.location = location;
        this.age = 0;
        this.foodLevel = 0;
        this.gender = rand.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    /**
     * Return this animal's gender.
     */
    public Gender getGender()
    {
        return gender;
    }

    /**
     * Return true if there is an adjacent animal of the same species
     * and opposite gender in the current field.
     */
    /**
     * How many cells out to search for a mate. Default is 2 (5x5 grid minus self).
     * Predators override this for a wider search at lower densities.
     */
    protected int getGenderSearchRadius() { return 2; }

    /**
     * Whether this species requires a nearby opposite-gender individual to breed.
     * Returning false keeps gender as a tracked trait while allowing any individual
     * to breed (appropriate for species that find mates via scent / territory, not
     * direct proximity). Defaults to false; override to true to re-enable the gate.
     */
    protected boolean requiresMate() { return false; }

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
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     * @param isNight If its night or not
     * @param weather Current weather
     */
    abstract public void act(Field currentField, Field nextFieldState, boolean isNight, Weather weather);
    
    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    public boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the animal is no longer alive.
     */
    protected void setDead()
    {
        alive = false;
        location = null;
    }
    
    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    public Location getLocation()
    {
        return location;
    }
    
    /**
     * Set the animal's location.
     * @param location The new location.
     */
    protected void setLocation(Location location)
    {
        this.location = location;
    }

    /**
     * Increase the age. This could result in the animal's death.
     */
    protected void incrementAge()
    {
        age++;
        if(age > getMaxAge()) {
            setDead();
        }
    }

    /**
     * Make this animal more hungry if it uses hunger. This could result in death.
     */
    protected void incrementHunger()
    {
        if(!usesHunger()) {
            return;
        }
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    // Random generator for breeding/food decisions
    private static final java.util.Random rand = Randomizer.getRandom();

    /**
     * Return a list of Classes this animal will consider prey.
     * Subclasses should override to specify prey types.
     */
    protected java.util.List<Class<?>> getPreyClasses()
    {
        return java.util.Collections.emptyList();
    }

    /**
     * Return the food value gained by eating an object of the given class.
     * Subclasses that eat should override.
     */
    protected int getFoodValue(Class<?> preyClass)
    {
        return 0;
    }

    /**
     * Determine whether this animal (or plant) can be eaten by the
     * given predator. Subclasses override to provide species-specific
     * rules. Default is not edible.
     */
    public boolean isEdibleBy(Animal predator)
    {
        return false;
    }

    /**
     * The foodLevel below which this animal will actively seek food.
     * Subclasses can override to prevent unnecessary eating when full.
     * Default 0 means always seek food (appropriate for pure predators).
     */
    protected int getHungerThreshold()
    {
        return 0;
    }

    /**
     * Whether this predator is willing to eat prey of the given class right now.
     * Allows conditional hunting — e.g. only eat secondary prey when hungry.
     * Default: always willing to eat anything that isEdibleBy returns true for.
     */
    protected boolean canEat(Class<?> preyClass) { return true; }

    /**
     * Look for prey adjacent to the current location. If found, kill it,
     * set foodLevel appropriately and return its location. Otherwise return null.
     */
    protected Location findFood(Field field)
    {
        // Only eat if hungry enough.
        if(foodLevel > getHungerThreshold()) {
            return null;
        }
        java.util.List<Location> adjacent = field.getAdjacentLocations(getLocation());
        java.util.Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;
        while(foodLocation == null && it.hasNext()) {
            Location loc = it.next();
            // First check for an animal at the location.
            Animal animal = field.getAnimalAt(loc);
            if(animal != null && animal.isAlive()) {
                if(animal.isEdibleBy(this) && canEat(animal.getClass())) {
                    animal.setDead();
                    foodLevel = getFoodValue(animal.getClass());
                    foodLocation = loc;
                    break;
                }
            }
            // Next check for a plant in the plant layer.
            Plant plant = field.getPlantAt(loc);
            if(plant != null && plant.isAlive() && plant.isEdibleBy(this)) {
                plant.setDead();
                field.clearPlant(loc);
                foodLevel = getFoodValue(plant.getClass());
                foodLocation = loc;
                break;
            }
        }
        return foodLocation;
    }

    /**
     * Subclasses implement getters for breeding characteristics below.
     */
    protected abstract int getBreedingAge();
    protected abstract double getBreedingProbability();
    protected abstract int getMaxLitterSize();

    /**
     * Can this animal breed (based on age)?
     */
    protected boolean canBreed()
    {
        return age >= getBreedingAge();
    }

    /**
     * Calculate number of births this step.
     */
    protected int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= getBreedingProbability()) {
            births = rand.nextInt(getMaxLitterSize()) + 1;
        }
        return births;
    }

    /**
     * Create a new young instance at the given location. Subclasses must implement.
     */
    protected abstract Animal createYoung(Location location);

    /**
     * Give birth into available free locations in the next field state.
     * Requires an adjacent opposite-gender partner of the same species.
     */
    protected void giveBirth(Field currentField, Field nextFieldState, java.util.List<Location> freeLocations)
    {
        if(requiresMate() && !hasOppositeGenderNeighbour(currentField)) {
            return;
        }
        int births = breed();
        if(births > 0) {
            for (int b = 0; b < births && !freeLocations.isEmpty(); b++) {
                Location loc = freeLocations.remove(0);
                Animal young = createYoung(loc);
                nextFieldState.placeAnimal(young, loc);
            }
        }
    }

    @Override
    public String toString() {
        String s = getClass().getSimpleName() + "{" +
                "age=" + age +
                ", alive=" + isAlive() +
                ", location=" + getLocation();
        if(usesHunger()) {
            s += ", foodLevel=" + foodLevel;
        }
        s += '}';
        return s;
    }
}
