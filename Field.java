import java.util.*;

/**
 * Represent a rectangular grid of field positions.
 * Each position is able to store a single animal/object.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class Field
{
    // A random number generator for providing random locations.
    private static final Random rand = Randomizer.getRandom();
    
    // The dimensions of the field.
    private final int depth, width;
    // Animals mapped by location.
    private final Map<Location, Animal> field = new HashMap<>();
    // Plants mapped by location (separate layer).
    private final Map<Location, Plant> plants = new HashMap<>();
    // The animals.
    private final List<Animal> animals = new ArrayList<>();
    // The plants (kept separate from animals).
    private final List<Plant> plantList = new ArrayList<>();

    /**
     * Represent a field of the given dimensions.
     * @param depth The depth of the field.
     * @param width The width of the field.
     */
    public Field(int depth, int width)
    {
        this.depth = depth;
        this.width = width;
    }

    /**
     * Place an animal at the given location.
     * If there is already an animal at the location it will
     * be lost.
     * @param anAnimal The animal to be placed.
     * @param location Where to place the animal.
     */
    public void placeAnimal(Animal anAnimal, Location location)
    {
        assert location != null;
        Animal other = field.get(location);
        if(other != null) {
            animals.remove(other);
        }
        field.put(location, anAnimal);
        animals.add(anAnimal);
    }

    /**
     * Place a plant at the given location. Plants are kept in a separate
     * layer from animals. If there is already a plant at the location it will
     * be replaced.
     */
    public void placePlant(Plant aPlant, Location location)
    {
        assert location != null;
        Plant other = plants.get(location);
        if(other != null) {
            plantList.remove(other);
        }
        if(aPlant == null) {
            plants.remove(location);
        }
        else {
            plants.put(location, aPlant);
            plantList.add(aPlant);
        }
    }

    /**
     * Remove any plant at the given location, keeping both internal
     * structures (HashMap and list) in sync.
     */
    public void clearPlant(Location location)
    {
        Plant removed = plants.remove(location);
        if(removed != null) {
            plantList.remove(removed);
        }
    }
    
    /**
     * Return the animal at the given location, if any.
     * @param location Where in the field.
     * @return The animal at the given location, or null if there is none.
     */
    public Animal getAnimalAt(Location location)
    {
        return field.get(location);
    }

    /**
     * Return the plant at the given location, if any.
     */
    public Plant getPlantAt(Location location)
    {
        return plants.get(location);
    }

    /**
     * Get a shuffled list of the free adjacent locations.
     * @param location Get locations adjacent to this.
     * @return A list of free adjacent locations.
     */
    public List<Location> getFreeAdjacentLocations(Location location)
    {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = getAdjacentLocations(location);
        for(Location next : adjacent) {
            Animal anAnimal = field.get(next);
            if(anAnimal == null) {
                free.add(next);
            }
            else if(!anAnimal.isAlive()) {
                free.add(next);
            }
        }
        return free;
    }

    /**
     * Return a shuffled list of locations adjacent to the given one.
     * The list will not include the location itself.
     * All locations will lie within the grid.
     * @param location The location from which to generate adjacencies.
     * @return A list of locations adjacent to that given.
     */
    public List<Location> getAdjacentLocations(Location location)
    {
        // The list of locations to be returned.
        List<Location> locations = new ArrayList<>();
        if(location != null) {
            int row = location.row();
            int col = location.col();
            for(int roffset = -1; roffset <= 1; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -1; coffset <= 1; coffset++) {
                        int nextCol = col + coffset;
                        // Exclude invalid locations and the original location.
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            
            // Shuffle the list. Several other methods rely on the list
            // being in a random order.
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /**
     * Return a shuffled list of all locations within the given radius
     * of the specified location, excluding the location itself.
     */
    public List<Location> getLocationsWithinRadius(Location location, int radius)
    {
        List<Location> locations = new ArrayList<>();
        if(location != null) {
            int row = location.row();
            int col = location.col();
            for(int roffset = -radius; roffset <= radius; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -radius; coffset <= radius; coffset++) {
                        int nextCol = col + coffset;
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /**
     * Print out the number of foxes and rabbits in the field.
     */
    public void fieldStats()
    {
        // Generic counts for all species present in the field.
        Map<Class<?>, Integer> counts = new HashMap<>();
        for(Animal anAnimal : field.values()) {
            if(anAnimal != null && anAnimal.isAlive()) {
                counts.merge(anAnimal.getClass(), 1, Integer::sum);
            }
        }
        for(Plant p : plants.values()) {
            if(p != null && p.isAlive()) {
                counts.merge(p.getClass(), 1, Integer::sum);
            }
        }
        // Format output similar to previous implementation.
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Class<?>, Integer> e : counts.entrySet()) {
            String name = e.getKey().getSimpleName();
            sb.append(name).append(": ").append(e.getValue()).append(' ');
        }
        System.out.println(sb.toString().trim());
    }

    /**
     * Empty the field.
     */
    public void clear()
    {
        field.clear();
        plants.clear();
        animals.clear();
        plantList.clear();
    }

    /**
     * Return whether there is at least one rabbit and one fox in the field.
     * @return true if there is at least one rabbit and one fox in the field.
     */
    public boolean isViable()
    {
        // Viable if at least two different species are alive.
        Set<Class<?>> aliveSpecies = new HashSet<>();
        for(Animal a : animals) {
            if(a != null && a.isAlive()) {
                aliveSpecies.add(a.getClass());
                if(aliveSpecies.size() >= 2) {
                    return true;
                }
            }
        }
        for(Plant p : plantList) {
            if(p != null && p.isAlive()) {
                aliveSpecies.add(p.getClass());
                if(aliveSpecies.size() >= 2) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get the list of animals.
     */
    public List<Animal> getAnimals()
    {
        return animals;
    }

    /**
     * Get the list of plants.
     */
    public List<Plant> getPlants()
    {
        return plantList;
    }

    /**
     * Return the depth of the field.
     * @return The depth of the field.
     */
    public int getDepth()
    {
        return depth;
    }
    
    /**
     * Return the width of the field.
     * @return The width of the field.
     */
    public int getWidth()
    {
        return width;
    }
}
