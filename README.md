# Brainf
 A simple Java Brainfuck interpreter

## Building
Use `./gradlew build` or the provided [`Brainf [build]` IntelliJ run configuration](.run/Brainf%20%5Bbuild%5D.run.xml) which should have been automatically imported into your IDE.

## Running
Use `./gradlew run` or the provided [`Brainf [run]` IntelliJ run configuration](.run/Brainf%20%5Brun%5D.run.xml) which should have been automatically imported into your IDE.

## Usage
To load your code, use `BFSession.load()`. To run it, call `BFSession.run()`. An example is provided [here](src/main/java/info/sciman/Main.java).