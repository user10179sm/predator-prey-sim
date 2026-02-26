import java.awt.*;
import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A graphical view of the simulation grid.
 * The view displays a colored rectangle for each location 
 * representing its contents. It uses a default background color.
 * Colors for each type of species can be defined using the
 * setColor method.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class SimulatorView extends JFrame
{
    // Colors used for empty locations.
    private static final Color EMPTY_COLOR = Color.white;

    // Color used for objects that have no defined color.
    private static final Color UNKNOWN_COLOR = Color.gray;

    private static final String STEP_PREFIX = "Step: ";
    private static final String POPULATION_PREFIX = "Population: ";
    private final JLabel stepLabel;
    private final JLabel population;
    private final FieldView fieldView;
    
    // A map for storing colors for participants in the simulation
    private final Map<Class<?>, Color> colors;
    // A statistics object computing and storing simulation information
    private final FieldStats stats;

    /**
     * Create a view of the given width and height.
     * @param height The simulation's height.
     * @param width  The simulation's width.
     */
    public SimulatorView(int height, int width)
    {
        stats = new FieldStats();
        colors = new LinkedHashMap<>();
        setColor(Capybara.class, new Color(139, 90, 43)); // brown
        setColor(HowlerMonkey.class, new Color(180, 130, 60)); // tan
        setColor(Jaguar.class, new Color(210, 100, 0)); // dark orange
        setColor(HarpyEagle.class, new Color(50, 50, 160)); // dark blue
        setColor(Fern.class, new Color(80, 200, 50)); // green
        setColor(FruitTree.class, new Color(0, 130, 90)); // dark teal

        setTitle("Rainforest Predator-Prey Simulation");
        stepLabel = new JLabel(STEP_PREFIX, JLabel.CENTER);
        population = new JLabel(POPULATION_PREFIX, JLabel.CENTER);
        
        setLocation(100, 50);
        
        fieldView = new FieldView(height, width);

        Container contents = getContentPane();
        contents.add(stepLabel, BorderLayout.NORTH);
        contents.add(fieldView, BorderLayout.CENTER);
        contents.add(new LegendPanel(), BorderLayout.EAST);
        contents.add(population, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }
    
    /**
     * Define a color to be used for a given class of animal.
     * @param animalClass The animal's Class object.
     * @param color The color to be used for the given class.
     */
    public void setColor(Class<?> animalClass, Color color)
    {
        colors.put(animalClass, color);
    }

    /**
     * @return The color to be used for a given class of animal.
     */
    private Color getColor(Class<?> animalClass)
    {
        Color col = colors.get(animalClass);
        if(col == null) {
            // no color defined for this class
            return UNKNOWN_COLOR;
        }
        else {
            return col;
        }
    }

    /**
     * Show the current status of the field.
     * @param step Which iteration step it is.
     * @param field The field whose status is to be displayed.
     * @param ctx  The current simulation context (time/weather).
     */
    public void showStatus(int step, Field field, SimulationContext ctx)
    {
        if(!isVisible()) {
            setVisible(true);
        }
            
        stepLabel.setText(STEP_PREFIX + step + "   |   " + ctx.toDisplayString());
        stats.reset();
        
        fieldView.preparePaint();

        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location loc = new Location(row, col);
                Plant plant = field.getPlantAt(loc);
                Animal animal = field.getAnimalAt(loc);
                // Draw empty background.
                fieldView.drawMark(col, row, EMPTY_COLOR);
                if(animal != null) {
                    stats.incrementCount(animal.getClass());
                    fieldView.drawMark(col, row, getColor(animal.getClass()));
                    // Draw a small overlay dot for disease state.
                    if(animal.isInfected()) {
                        fieldView.drawOverlayMark(col, row, Color.RED);
                    } else if(animal.isImmune()) {
                        fieldView.drawOverlayMark(col, row, Color.YELLOW);
                    }
                }
                // Draw plant marker on top so it remains visible even when
                // an animal occupies the cell.
                if(plant != null) {
                    stats.incrementCount(plant.getClass());
                    fieldView.drawPlantMark(col, row, getColor(plant.getClass()));
                }
            }
        }
        stats.countFinished();

        population.setText(POPULATION_PREFIX + stats.getPopulationDetails(field));
        fieldView.repaint();
    }

    /**
     * Determine whether the simulation should continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        return stats.isViable(field);
    }

    /**
     * A panel that renders a colour key for all species and status indicators.
     */
    private class LegendPanel extends JPanel
    {
        private static final int SWATCH = 16;   // colour square size
        private static final int PAD    = 8;    // padding
        private static final int ROW_H  = 24;   // height per row

        private final String[][] entries = {
            { "Capybara",     "",              "animal" },
            { "HowlerMonkey", "Howler Monkey", "animal" },
            { "Jaguar",       "",              "animal" },
            { "HarpyEagle",   "Harpy Eagle",   "animal" },
            { "Fern",         "",              "plant"  },
            { "FruitTree",    "Fruit Tree",    "plant"  },
        };

        public LegendPanel()
        {
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            setBackground(Color.WHITE);
        }

        @Override
        public Dimension getPreferredSize()
        {
            int rows = entries.length + 1 /* separator */ + 3 /* status dots */;
            return new Dimension(160, PAD + rows * ROW_H + PAD * 2);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
            FontMetrics fm = g.getFontMetrics();

            int x = PAD;
            int y = PAD;

            // Header
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            fm = g.getFontMetrics();
            g.setColor(Color.BLACK);
            g.drawString("Key", x, y + fm.getAscent());
            y += ROW_H;
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 11f));
            fm = g.getFontMetrics();

            // Species entries
            for(String[] entry : entries) {
                String className   = entry[0];
                String displayName = entry[1].isEmpty() ? className : entry[1];
                Color  col         = lookupByName(className);
                boolean isPlant    = entry[2].equals("plant");

                if(isPlant) {
                    // Plants: grey cell with coloured oval in top-right corner
                    g.setColor(new Color(200, 200, 200));
                    g.fillRect(x, y - SWATCH + 3, SWATCH, SWATCH);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x, y - SWATCH + 3, SWATCH, SWATCH);
                    int dot = Math.max(2, (SWATCH - 2) / 2);
                    g.setColor(col);
                    g.fillOval(x + SWATCH - dot - 1, y - SWATCH + 4, dot, dot);
                } else {
                    // Animals: solid colour swatch
                    g.setColor(col);
                    g.fillRect(x, y - SWATCH + 3, SWATCH, SWATCH);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect(x, y - SWATCH + 3, SWATCH, SWATCH);
                }
                g.setColor(Color.BLACK);
                g.drawString(displayName, x + SWATCH + 6, y);
                y += ROW_H;
            }

            // Separator line
            y += 4;
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x, y, getWidth() - PAD, y);
            y += ROW_H - 4;

            // Status indicators
            drawIndicator(g, x, y, new Color(80, 200, 50),  true,  "Plant present"); y += ROW_H;
            drawIndicator(g, x, y, Color.RED,              false, "Infected");       y += ROW_H;
            drawIndicator(g, x, y, Color.YELLOW.darker(),  false, "Immune");
        }

        /** Draw a mock cell with the corner oval + a label. */
        private void drawIndicator(Graphics g, int x, int y,
                                   Color color, boolean topRight, String label)
        {
            // Grey mock cell
            g.setColor(new Color(200, 200, 200));
            g.fillRect(x, y - SWATCH + 3, SWATCH, SWATCH);
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(x, y - SWATCH + 3, SWATCH, SWATCH);

            int dot = Math.max(2, (SWATCH - 2) / 2);
            int ox = x + SWATCH - dot - 1;
            int oy = topRight ? (y - SWATCH + 4) : (y - dot);
            g.setColor(color);
            g.fillOval(ox, oy, dot, dot);

            g.setColor(Color.BLACK);
            g.drawString(label, x + SWATCH + 6, y);
        }

        private Color lookupByName(String name)
        {
            for(Map.Entry<Class<?>, Color> e : colors.entrySet()) {
                if(e.getKey().getSimpleName().equals(name)) {
                    return e.getValue();
                }
            }
            return UNKNOWN_COLOR;
        }
    }

    /**
     * Provide a graphical view of a rectangular field. This is 
     * a nested class (a class defined inside a class) which
     * defines a custom component for the user interface. This
     * component displays the field.
     * This is rather advanced GUI stuff - you can ignore this 
     * for your project if you like.
     */
    private class FieldView extends JPanel
    {
        private static final int GRID_VIEW_SCALING_FACTOR = 8;

        private final int gridWidth, gridHeight;
        private int xScale, yScale;
        Dimension size;
        private Graphics g;
        private Image fieldImage;

        /**
         * Create a new FieldView component.
         */
        public FieldView(int height, int width)
        {
            gridHeight = height;
            gridWidth = width;
            size = new Dimension(0, 0);
        }

        /**
         * Tell the GUI manager how big we would like to be.
         */
        public Dimension getPreferredSize()
        {
            return new Dimension(gridWidth * GRID_VIEW_SCALING_FACTOR,
                                 gridHeight * GRID_VIEW_SCALING_FACTOR);
        }

        /**
         * Prepare for a new round of painting. Since the component
         * may be resized, compute the scaling factor again.
         */
        public void preparePaint()
        {
            if(! size.equals(getSize())) {  // if the size has changed...
                size = getSize();
                fieldImage = fieldView.createImage(size.width, size.height);
                g = fieldImage.getGraphics();

                xScale = size.width / gridWidth;
                if(xScale < 1) {
                    xScale = GRID_VIEW_SCALING_FACTOR;
                }
                yScale = size.height / gridHeight;
                if(yScale < 1) {
                    yScale = GRID_VIEW_SCALING_FACTOR;
                }
            }
        }
        
        /**
         * Paint on grid location on this field in a given color.
         */
        public void drawMark(int x, int y, Color color)
        {
            g.setColor(color);
            g.fillRect(x * xScale, y * yScale, xScale-1, yScale-1);
        }

        /**
         * Draw a small plant marker in the top-right corner of the cell.
         */
        public void drawPlantMark(int x, int y, Color color)
        {
            g.setColor(color);
            int dot = Math.max(2, (Math.min(xScale, yScale) - 2) / 2);
            int px = x * xScale + xScale - dot - 1;
            int py = y * yScale + 1;
            g.fillOval(px, py, dot, dot);
        }

        /**
         * Draw a small overlay oval in the bottom-right corner of a cell.
         * Used to indicate disease status without obscuring the animal's colour.
         * Red = infected, Yellow = immune.
         */
        public void drawOverlayMark(int x, int y, Color color)
        {
            g.setColor(color);
            int dot = Math.max(2, (Math.min(xScale, yScale) - 2) / 2);
            int px = x * xScale + xScale - dot - 1;
            int py = y * yScale + yScale - dot - 1;
            g.fillOval(px, py, dot, dot);
        }

        /**
         * The field view component needs to be redisplayed. Copy the
         * internal image to screen.
         */
        public void paintComponent(Graphics g)
        {
            if(fieldImage != null) {
                Dimension currentSize = getSize();
                if(size.equals(currentSize)) {
                    g.drawImage(fieldImage, 0, 0, null);
                }
                else {
                    // Rescale the previous image.
                    g.drawImage(fieldImage, 0, 0, currentSize.width, currentSize.height, null);
                }
            }
        }
    }
}
