# Report Alignment Notes (for Google Docs version)

The repository contains only the exported PDF (`Assignment 3 - Predator Prey Simulation Report - Google Docs.pdf`), so these changes need to be applied in the Google Doc and re-exported.

## Factual fixes (must update)

1. Time-of-day states wording is incorrect in the report text.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:57`) says: `There are 4 day states: dusk day, dawn, night.`
   - Code uses four phases: `NIGHT`, `DAWN`, `DAY`, `DUSK` in `SimulationContext.java:10`.
   - Replace with: `There are 4 time-of-day states: night, dawn, day, and dusk.`

2. Fruit Tree wording says "spores" and "at night" in two places.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:58-59`, `:74-75`) refers to fruit trees dispersing spores at night.
   - Code shows Fruit Trees disperse **seeds** and only during **daylight** in `FruitTree.java:3`, `FruitTree.java:24`, `FruitTree.java:25`.
   - Replace with: `Fruit trees disperse seeds only during daylight.`

3. Weather condition name mismatch: report says `windy`, code uses `stormy`.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:77`) lists `sunny, rainy, foggy, windy`.
   - Code defines `SUNNY, RAINY, FOGGY, STORMY` in `SimulationContext.java:12`.
   - Replace `windy` with `stormy`.

4. Weather-method wording includes a placeholder that looks unfinished.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:79-81`) says `via ...SuccessFactor()`.
   - Use the real method names from `SimulationContext.java`: `plantGrowthFactor()` (`:45`), `huntingSuccessFactor()` (`:53`), `allowsMovement()` (`:61`), and `breedingFactor()` (`:66`).
   - Replace with: `Weather influences plant growth, hunting success, movement, and breeding through plantGrowthFactor(), huntingSuccessFactor(), allowsMovement(), and breedingFactor().`

5. Plant maturity claim now matches code (after fix), but mention it clearly.
   - Code now requires plants to be mature before herbivores can eat them in `Animal.java:164-166`, and plant maturity is defined by `Plant.isEdible()` in `Plant.java:23-24`.
   - Your report line (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:71-73`) is fine in intent; keep it, but remove the later incorrect fruit-tree sentence (see item 2).

## Accuracy improvements (recommended)

1. Harpy Eagle "always hunts if adjacent" is stronger than the implementation.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:46`) says `always hunts if adjacent`.
   - The code treats Howler Monkey as primary prey, but adjacency order is shuffled, so this wording is too absolute (`HarpyEagle.java:17-28`, `Animal.java:148-170`).
   - Safer replacement: `Primary prey is the Howler Monkey. When sufficiently hungry, Harpy Eagles can also hunt Capybaras.`

2. Processing-order bug description is slightly overstated.
   - Report text (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:98-100`) says animals added during `populate()` always act first forever.
   - The real issue is sequential processing order within each step, not specifically the initial population order only.
   - Safer replacement: `Animals are processed sequentially from the ArrayList order each step, which can bias movement, hunting, and breeding outcomes within that step.`

## "Doesn't look AI" wording cleanup (recommended)

These lines are not "wrong", but they read overly polished/generic:

1. `During development, significant effort was put into designing the codebase according to core OOP principles...` (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:8-10`)
   - Replace with concrete implementation facts instead of abstract quality claims.

2. Repeated `successfully implemented` phrasing in the extension section (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:70-75`)
   - Use direct wording like `We implemented...`, `Plants grow/spread...`, `Fruit trees only spread during daylight...`.

3. `The image below depicts the food web we implemented:` (`Assignment 3 - Predator Prey Simulation Report - Google Docs.txt:22`)
   - Fine if the image is present. Remove if the PDF export does not actually show the image.

## Suggested replacement snippets

### Core task 3 (time of day)
`There are 4 time-of-day states: night, dawn, day, and dusk. Different behaviours are triggered by time of day; for example, animals are inactive at night, and fruit trees disperse seeds only during daylight.`

### Extension task 5 (plants)
`We added plants as separate species on a separate plant layer, so animals can occupy the same grid cell as a plant. Herbivores can eat plants, and plants must reach maturity before they can be eaten or spread. Plant spreading is affected by weather through plantGrowthFactor(), and fruit trees spread seeds only during daylight.`

### Extension task 6 (weather)
`We added four weather conditions: sunny, rainy, foggy, and stormy. Weather is redrawn at night and dawn, and it affects plant growth, hunting success, movement, and breeding through plantGrowthFactor(), huntingSuccessFactor(), allowsMovement(), and breedingFactor(). During storms, animals shelter (no movement), predators cannot hunt, and breeding is disabled.`
