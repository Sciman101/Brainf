# Brainf
 A simple Java Brainfuck interpreter

## Building
Use `./gradlew build` or the provided [`Brainf [build]` IntelliJ run configuration](.run/Brainf%20%5Bbuild%5D.run.xml) which should have been automatically imported into your IDE.

## Running
Use `./gradlew run` or the provided [`Brainf [run]` IntelliJ run configuration](.run/Brainf%20%5Brun%5D.run.xml) which should have been automatically imported into your IDE.

## Usage
To load your code, use `BFSession.load()`. To run it, call `BFSession.run()`. An example is provided [here](src/main/java/info/sciman/Main.java).

## Contributing
This project adheres to the [Google Java Style](https://google.github.io/styleguide/javaguide.html). This is accomplished with the [`googleJavaFormat()`](https://github.com/diffplug/spotless/tree/main/plugin-gradle#google-java-format) formatter of the [Spotless](https://github.com/diffplug/spotless) Gradle plugin.

You can check that your code adheres to the Google Java Style by running `./gradlew spotlessJavaCheck` or using the included [`Brainf [spotlessJavaCheck]` run configuration](.run/Brainf%20%5BspotlessJavaCheck%5D.run.xml). You can also just build the project which will run that same task during the build process.

To reformat your code, you can use the `spotlessJavaApply` task or the [included run configuration](.run/Brainf%20%5BspotlessJavaApply%5D.run.xml). However, it is easier to use the [google-java-format IntelliJ plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) to do this task on the fly with a simple keystroke (CTRL + Alt + L by default).