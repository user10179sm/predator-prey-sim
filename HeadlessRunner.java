/**
 * Runs the simulation in headless mode (no GUI) for analysis.
 * Output is CSV on stdout: step,fern,capybara,howlermonkey,jaguar,harpyeagle
 */
public class HeadlessRunner {
    public static void main(String[] args) {
        Simulator sim = new Simulator(true);
        sim.simulate(500);
    }
}
