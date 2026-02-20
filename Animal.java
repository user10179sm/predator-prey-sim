

/**
 * Common elements of foxes and rabbits.
 *
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public abstract class Animal
{
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's position.
    private Location location;
    private boolean isMale; // There are two states that the sex could be, male or female.

    /**
     * Constructor for objects of class Animal.
     * @param location The animal's location.
     */
    public Animal(Location location)
    {
        this.alive = true;
        this.location = location;
        this.isMale = Randomizer.getRandom().nextBoolean(); // Randomly decide whether the animal is male or female
    }
    
    /**
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    abstract public void act(Field currentField, Field nextFieldState);
    
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
     * Return if the animal is a male or not
     * @return boolean True if the animal is male. boolean False if the animal is female.
     */
    public boolean isMale()
    {
        return isMale;
    }

    /**
     * Return if the animal is female or not
     * This works because if the animal is not a male then it is a female (by sex)
     * If isMale is False then !isMale will evaluate to true
     * This means that isFemale() = True under isMale = False
     * @return boolean True if the animal is female. boolean False if the animal is male.
     */
    public boolean isFemale()
    {
        return !isMale;
    }
    
    /**
     * Set the animal's location.
     * @param location The new location.
     */
    protected void setLocation(Location location)
    {
        this.location = location;
    }
}
