package info.sciman;

public class Main {

    public static void main(String[] args) {
        // Create a brainfuck session and point it to sysout/sysin
        BFSession bf = new BFSession(System.out,System.in);

        bf.load("+++++[>+++++++>++<<-]>.>.][");
        bf.run();
    }
}
