/**
 * A base class for plants in the simulation. Plants are stationary
 * objects that age and can become edible after a maturity age.
 * Plants are kept in a separate layer from animals in the Field.
 */
public abstract class Plant
{
    private boolean alive;
    private Location location;
    protected int age;
    protected final int maturityAge;

    public Plant(Location location, int maturityAge)
    {
        this.alive = true;
        this.location = location;
        this.age = 0;
        this.maturityAge = maturityAge;
    }

    /** Each step the plant gets to act (age, spread seeds, etc.). */
    public abstract void act(Field currentField, Field nextFieldState);

    /** Maximum age before this plant dies. */
    public abstract int getMaxAge();

    public boolean isAlive()
    {
        return alive;
    }

    public void setDead()
    {
        alive = false;
        location = null;
    }

    public Location getLocation()
    {
        return location;
    }

    protected void setLocation(Location location)
    {
        this.location = location;
    }

    /** Age by one step; die if max age exceeded. */
    protected void incrementAge()
    {
        age++;
        if(age > getMaxAge()) {
            setDead();
        }
    }

    /** Whether this plant has reached maturity and can be eaten/spread. */
    public boolean isEdible()
    {
        return age >= maturityAge;
    }

    /**
     * Whether this plant can be eaten by the given predator.
     * Subclasses override to specify which animals can eat them.
     */
    public boolean isEdibleBy(Animal predator)
    {
        return false;
    }
}
