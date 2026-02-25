/**
 * Abstract base class shared by Animal and Plant.
 * Holds the state and behaviour common to every entity in the simulation:
 * alive/dead status, grid location, age, and the edibility contract.
 * Pulling this up eliminates eight members that were duplicated between
 * the two independent hierarchies.
 */
public abstract class SimulationEntity
{
    private boolean  alive    = true;
    private Location location;
    private int      age      = 0;

    protected SimulationEntity(Location location)
    {
        this.location = location;
    }

    // ── Life-state ────────────────────────────────────────────────────────────

    public boolean isAlive()
    {
        return alive;
    }

    public void setDead()
    {
        alive    = false;
        location = null;
    }

    // ── Location ──────────────────────────────────────────────────────────────

    public Location getLocation()
    {
        return location;
    }

    protected void setLocation(Location location)
    {
        this.location = location;
    }

    // ── Age ──────────────────────────────────────────────────────────────────

    /** Maximum age the entity can reach before dying of old age. */
    public abstract int getMaxAge();

    /** Current age in simulation steps. */
    protected int getAge()
    {
        return age;
    }

    /**
     * Set the initial age (called from subclass constructors when randomAge=true).
     * Clamped to [0, getMaxAge()].
     */
    protected void setAge(int a)
    {
        age = Math.max(0, Math.min(a, getMaxAge()));
    }

    /** Advance age by one step; kill the entity when its max age is exceeded. */
    protected void incrementAge()
    {
        age++;
        if(age > getMaxAge()) {
            setDead();
        }
    }

    // ── Edibility ─────────────────────────────────────────────────────────────
    // Note: prey recognition is handled on the predator side via
    // Animal.getFoodValue(Class<?>). isEdibleBy() is not used and is removed
    // to eliminate the OOP-02 circular dependency (base class referencing a subtype).
}
