package info.sciman;

public class Main {

  public static void main(String[] args) {

    String code = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";

    // Create a brainfuck session and point it to sysout/sysin
    BfSession bf = new BfSession.BfBuilder(code)
                      .in(System.in)
                      .out(System.out)
                      .build();

    BfSession bfOptimized = new BfSession.BfBuilder(code)
            .in(System.in)
            .out(System.out)
            .optimize()
            .build();

    // Hello, world!
    bf.run();
    System.out.println("Iterations: "+bf.getNumIterations());
    System.out.println("---");
    bfOptimized.run();
    System.out.println("Iterations: "+bfOptimized.getNumIterations());
  }
}
