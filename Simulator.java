import java.util.*;

/**
 * A simple predator-prey simulator, based on a rectangular field containing 
 * rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.1
 */

// create a main class so that I can

public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // Rainforest species creation probabilities.
    private static final double FERN_CREATION_PROBABILITY = 0.30;
    private static final double FRUIT_TREE_CREATION_PROBABILITY = 0.25;
    private static final double CAPYBARA_CREATION_PROBABILITY = 0.09;
    private static final double HOWLER_MONKEY_CREATION_PROBABILITY = 0.10;
    private static final double JAGUAR_CREATION_PROBABILITY = 0.015;
    private static final double HARPY_EAGLE_CREATION_PROBABILITY = 0.018;
    // Clock constants
    private static final int HOURS_PER_DAY = 24;
    private static final int START_HOUR = 6; // start the day at 6am
    private static final int STEPS_PER_HOUR = 10;

    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation (null in headless mode).
    private final SimulatorView view;

    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH, false);
    }

    /**
     * Construct a simulation in headless mode (no GUI).
     */
    public Simulator(boolean headless)
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH, headless);
    }

    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     * @param headless If true, no GUI is created.
     */
    public Simulator(int depth, int width, boolean headless)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }

        field = new Field(depth, width);
        view = headless ? null : new SimulatorView(depth, width);

        reset();
    }

    /**
     * Keep old two-arg constructor working.
     */
    public Simulator(int depth, int width)
    {
        this(depth, width, false);
    }
    
    /**
     * Run the simulation from its current state for a reasonably long 
     * period (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(700);
    }
    
    /**
     * Run the simulation for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        reportStats();
        for(int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep();
            if(view != null) delay(50);
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each fox and rabbit.
     */
    public void simulateOneStep()
    {
        step++;
        // Use a separate Field to store the starting state of
        // the next step.
        Field nextFieldState = new Field(field.getDepth(), field.getWidth());

        List<Animal> animals = field.getAnimals();
        for (Animal anAnimal : animals) {
            anAnimal.act(field, nextFieldState, isNight());
        }

        List<Plant> livePlants = field.getPlants();
        for (Plant aPlant : livePlants) {
            aPlant.act(field, nextFieldState);
        }
        
        // Replace the old state with the new one.
        field = nextFieldState;

        reportStats();
        if(view != null) view.showStatus(step, getTimeLabel(), field);
    }

    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        populate();
        if(view != null) view.showStatus(step, getTimeLabel(), field);
    }
    
    /**
     * Randomly populate the field with foxes and rabbits.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);
                // Plants occupy the plant layer independently of animals.
                double plantRoll = rand.nextDouble();
                if(plantRoll <= FERN_CREATION_PROBABILITY) {
                    field.placePlant(new Fern(true, location), location);
                } else if(plantRoll <= FERN_CREATION_PROBABILITY + FRUIT_TREE_CREATION_PROBABILITY) {
                    field.placePlant(new FruitTree(true, location), location);
                }
                // Animals occupy the animal layer.
                double animalRoll = rand.nextDouble();
                if(animalRoll <= CAPYBARA_CREATION_PROBABILITY) {
                    field.placeAnimal(new Capybara(true, location), location);
                }
                else if(animalRoll <= CAPYBARA_CREATION_PROBABILITY + HOWLER_MONKEY_CREATION_PROBABILITY) {
                    field.placeAnimal(new HowlerMonkey(true, location), location);
                }
                else if(animalRoll <= CAPYBARA_CREATION_PROBABILITY + HOWLER_MONKEY_CREATION_PROBABILITY
                        + JAGUAR_CREATION_PROBABILITY) {
                    field.placeAnimal(new Jaguar(true, location), location);
                }
                else if(animalRoll <= CAPYBARA_CREATION_PROBABILITY + HOWLER_MONKEY_CREATION_PROBABILITY
                        + JAGUAR_CREATION_PROBABILITY + HARPY_EAGLE_CREATION_PROBABILITY) {
                    field.placeAnimal(new HarpyEagle(true, location), location);
                }
            }
        }
    }

    /**
     * Report on the number of each type of animal in the field.
     */
    public void reportStats()
    {
        //System.out.print("Step: " + step + " ");
        field.fieldStats();
    }

    /**
     * Gets the current hour of the day
     * @return currentHour
     */
    private int getHourOfDay()
    {
        int hoursPassed = step / STEPS_PER_HOUR;
        int currentHour = (START_HOUR + hoursPassed) % HOURS_PER_DAY;
        return currentHour;
    }


    /**
     * Determine whether it is night or not.
     * Night time hours are between 9 pm and 5am
     * @return boolean
     */
    private boolean isNight()
    {
        int hour = getHourOfDay();
        return hour < 6 || hour >= 21;
    }

    /**
     * Get the current day number
     * @return currentDay
     */
    private int getDayNumber()
    {
        int hoursPassed = step / STEPS_PER_HOUR;
        int totalHours = START_HOUR + hoursPassed;
        return totalHours / HOURS_PER_DAY;
    }

    /**
     * Get label for current time
     * @return currentTimeLabel
     */
    private String getTimeLabel()
    {
        return "Day " + getDayNumber() + " " + String.format("%02d:00", getHourOfDay());
    }
    
    /**
     * Pause for a given time.
     * @param milliseconds The time to pause for, in milliseconds
     */
    private void delay(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {
            // ignore
        }
    }
}
