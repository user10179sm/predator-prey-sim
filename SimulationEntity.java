/**
 * Abstract base class shared by Animal and Plant.
 * Holds the state and behaviour common to every entity in the simulation:
 * alive/dead status, grid location, and age.
 */
public abstract class SimulationEntity
{
    private boolean alive = true;
    private Location location;
    private int age = 0;

    protected SimulationEntity(Location location)
    {
        this.location = location;
    }

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

    /** Maximum age the entity can reach before dying of old age. */
    public abstract int getMaxAge();

    protected int getAge()
    {
        return age;
    }

    /**
     * Set the initial age (called from subclass constructors when randomAge=true).
     */
    protected void setAge(int a)
    {
        age = Math.max(0, Math.min(a, getMaxAge()));
    }

    protected void incrementAge()
    {
        age++;
        if(age > getMaxAge()) {
            setDead();
        }
    }
}
