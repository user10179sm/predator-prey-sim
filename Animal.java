import java.util.List;

/**
 * Abstract base for all animals in the simulation.
 * Extends SimulationEntity for the shared alive/location/age state.
 *
 * act() is a concrete final method here — subclasses customise behaviour
 * through hook methods only, not by reimplementing the full turn logic.
 */
public abstract class Animal extends SimulationEntity
{
    /** The two possible sexes for any animal. */
    public enum Gender { MALE, FEMALE }

    // Current hunger level; only meaningful when usesHunger() returns true.
    private int foodLevel = 0;

    // Gender assigned once at birth.
    private final Gender gender;

    // ─── Disease system constants ──────────────────────────────────────────
    private static final double INFECTION_SPREAD_CHANCE = 0.20;
    private static final double INFECTION_DEATH_CHANCE  = 0.003;
    private static final int    INFECTION_DURATION       = 50;
    private static final int    IMMUNITY_DURATION        = 30;

    // Per-instance disease state
    private boolean infected     = false;
    private int     infectionAge = 0;
    private int     immuneAge    = 0;

    @Override public abstract int getMaxAge();

    /**
     * Whether this species tracks hunger. Default: false.
     */
    public boolean usesHunger() { return false; }

    /** Shared random source available to all subclasses. */
    protected static final java.util.Random rand = Randomizer.getRandom();

    protected Animal(Location location)
    {
        super(location);
        this.gender = rand.nextBoolean() ? Gender.MALE : Gender.FEMALE;
    }

    /** Set the starting foodLevel (called from subclass constructors). */
    protected void initFoodLevel(int level) { foodLevel = level; }



    /** Whether a nearby opposite-gender partner is required to breed. Default: false. */
    protected boolean requiresMate() { return false; }

    /** Search radius (cells) for mate detection. Default: 2. */
    protected int getGenderSearchRadius() { return 2; }

    /**
     * Whether this animal is active this step.
     * Default: active during daylight when movement is allowed (not a storm).
     */
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
     * The full turn sequence is defined here; subclasses customise via hook
     * methods only — they do NOT override act().
     */
    public final void act(Field currentField, Field nextFieldState, SimulationContext ctx)
    {
        incrementAge();
        // B-08: guard BEFORE progressDisease so a just-aged-out animal is not
        // subjected to further disease progression.
        if(!isAlive()) return;

        progressDisease();
        if(!isAlive()) return;

        trySpreadDisease(currentField);

        if(!isActiveNow(ctx)) {
            // B-01: Sleeping animal — always try to retain current cell.
            // If an active animal already moved into that cell in nextFieldState,
            // fall back to any adjacent free cell rather than silently vanishing.
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
                    setDead(); // genuinely crowded out — death recorded correctly
                }
            }
            return;
        }

        incrementHunger();
        if(!isAlive()) return;

        // B-04: Use a separate slot list for births so offspring cannot consume
        // all free cells and accidentally kill the parent.
        List<Location> birthSlots = nextFieldState.getFreeAdjacentLocations(getLocation());
        if(!birthSlots.isEmpty()) {
            giveBirth(currentField, nextFieldState, birthSlots, ctx);
        }

        // After birth, re-query free cells so movement reflects newly filled slots.
        Location nextLocation = findFood(currentField, ctx);
        List<Location> moveSlots = nextFieldState.getFreeAdjacentLocations(getLocation());
        // If the prey's cell is already taken in nextFieldState, use a free cell instead.
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
        if(!usesHunger()) return;
        foodLevel--;
        if(foodLevel <= 0) setDead();
    }

    /**
     * Food value gained by eating something of the given class.
     * Return > 0 for recognised prey; 0 for non-prey.
     * Predator/prey relationships are expressed here on the predator side.
     */
    protected int getFoodValue(Class<?> preyClass) { return 0; }

    /** Current food level (read-only access for subclasses, e.g. conditional canEat). */
    protected int getFoodLevel() { return foodLevel; }

    /** foodLevel above which this animal stops hunting. Default: 0 (always hunt). */
    protected int getHungerThreshold() { return 0; }

    /**
     * Override to return true for apex predators that should hunt on every step
     * regardless of foodLevel.  Separates the "always hunt" semantics from the
     * numeric threshold so getHungerThreshold() retains its documented meaning.
     */
    protected boolean isAlwaysHungry() { return false; }

    /**
     * Whether this predator will eat prey of the given class right now.
     * Allows conditional hunting (e.g. secondary prey only when hungry).
     */
    protected boolean canEat(Class<?> preyClass) { return true; }

    /**
     * Scan adjacent cells for food.
     * Uses getFoodValue() > 0 to recognise prey — predator/prey knowledge
     * stays on the predator side rather than the prey side.
     */
    protected Location findFood(Field field, SimulationContext ctx)
    {
        // OOP-03: isAlwaysHungry() separates "hunt every step" from a numeric threshold.
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
            if(plant != null && plant.isAlive() && getFoodValue(plant.getClass()) > 0) {
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
        // ctx is never null in practice (always passed from act()), but guard is harmless.
        double bp = getBreedingProbability()
                * ctx.breedingFactor()
                * (infected ? 0.4 : 1.0);
        // B-07: use strict < so bp == 0.0 (e.g. during storms) never permits breeding.
        return (canBreed() && rand.nextDouble() < bp)
                ? rand.nextInt(getMaxLitterSize()) + 1 : 0;
    }

    protected void giveBirth(Field currentField, Field nextFieldState,
                             List<Location> freeLocations, SimulationContext ctx)
    {
        if(requiresMate() && !hasOppositeGenderNeighbour(currentField)) return;
        int births = breed(ctx);
        for(int b = 0; b < births && !freeLocations.isEmpty(); b++) {
            Location loc = freeLocations.remove(0);
            nextFieldState.placeAnimal(createYoung(loc), loc);
        }
    }

    // ─── Disease methods ──────────────────────────────────────────────────────

    /** Whether this animal is currently infected. */
    public boolean isInfected() { return infected; }

    /** Whether this animal is currently immune (post-infection recovery period). */
    public boolean isImmune()   { return immuneAge > 0; }

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

    /**
     * Advance the disease state by one step.
     * Infected animals may die randomly, and eventually recover (becoming immune).
     * Must be called once per act() cycle.
     */
    protected void progressDisease()
    {
        if(infected) {
            infectionAge++;
            // Small per-step chance of disease-induced death.
            if(rand.nextDouble() < INFECTION_DEATH_CHANCE) {
                setDead();
                return;
            }
            // After the infection duration, recover with immunity.
            if(infectionAge >= INFECTION_DURATION) {
                infected     = false;
                infectionAge = 0;
                immuneAge    = IMMUNITY_DURATION;
            }
        } else if(immuneAge > 0) {
            immuneAge--;
        }
    }

    /**
     * Spread the disease to adjacent animals.  Only infected animals spread.
     * Proximity disease spread (as opposed to spread through eating).
     */
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

    // ─── toString ────────────────────────────────────────────────────────────

    @Override
    public String toString()
    {
        String s = getClass().getSimpleName() + "{"
                + "age=" + getAge()
                + ", alive=" + isAlive()
                + ", location=" + getLocation();
        if(usesHunger())    s += ", foodLevel=" + foodLevel;
        if(infected)        s += ", INFECTED";
        else if(isImmune()) s += ", IMMUNE";
        s += '}';
        return s;
    }
}
