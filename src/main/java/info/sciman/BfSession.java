package info.sciman;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/** Class to contain an instance of a running brainfuck program */
public class BfSession {

  // Error messages
  private static final String ERROR_UNMATCHED =
      "Error parsing input BF: Unbalanced bracket at col &d";
  private static final String ERROR_CANNOT_RUN = "Unable to run, unmatched brackets in source code";
  private static final String ERROR_INDEX = "%d is out of the bounds of the BF tape [0,%d)";
  private static final String ERROR_CANNOT_OPTIMIZE = "There is no code to optimize!";

  // Streams for reading/writing data
  private PrintStream out;
  private InputStream in;
  private Scanner scanner;

  // Jump table
  private int[] offsetTable;

  // 'Tape' used to hold memory
  private byte[] tape;
  // Pointer
  private int ptr;
  // Program counter
  private int pc;

  // Actual code
  private String code;
  private int codeLen;
  private boolean error; // True if there was an error building the offset table

  // Stats
  private int numIterations; // Tracks how many times we step through the program
  private int maxIterations = -1;

  /** Default constructor */
  public BfSession(BfBuilder builder) {
    // Setup tape
    tape = new byte[builder.tapeLengthOverride];

    setInputStream(builder.in);
    setOutputStream(builder.out);
    maxIterations = builder.maxIterations;

    // Reset counters
    ptr = 0;
    pc = 0;
    numIterations = 0;

    // Load BF code
    load(builder.script,builder.optimize);
  }

  // Getters
  public int getProgramCounter() {return pc;}
  public int getPointerPosition() {return ptr;}
  public String getCode() {return code;}
  public int getTapeSize() {return tape.length;}
  public byte getTapeValue(int pos) { return tape[pos]; }
  public int getNumIterations() { return numIterations; }

  /**
   * Set the input stream for the session
   * @param in
   */
  public void setInputStream(InputStream in) {
    this.in = in;
    // Create scanner
    scanner = new Scanner(this.in);
    // We do this so the scanner only reads 1-character tokens in at once
    scanner.useDelimiter("");
  }

  /**
   * Set the output stream for the session
   * @param out
   */
  public void setOutputStream(PrintStream out) {
    this.out = out;
  }


  /**
   * Load BF code into the session
   *
   * @param code the code to load
   */
  public void load(String code, boolean optimize) {
    // Set code string
    this.code = code;

    // Optimize code string and create offset table
    if (optimize) {
      this.optimize();
    }else{
      // Create new offset table
      this.codeLen = code.length();
      offsetTable = new int[codeLen];
    }


    // Clear error
    error = false;

    // Compute jump table
    precalculateJumps();

    // Reset everything
    reset();
  }

  /**
   * Optimize the code loaded into the session
   */
  private void optimize() {
    // Make sure we have code
    if (this.code.length() <= 0) {
      throw new RuntimeException(ERROR_CANNOT_OPTIMIZE);
    }

    // Replace redundant statements
    code = code.replace("+-","")
            .replace("-+","")
            .replace("<>","")
            .replace("><","");

    // Replace recurring statements
    StringBuilder newCode = new StringBuilder();
    ArrayList<Integer> offsets = new ArrayList<>();
    int i = 0, j;
    // Loop over code and find
    while (i<code.length()) {

      char c = code.charAt(i);
      // Ignore brackets
      j = i+1;
      if (c != '[' && c != ']') {
        // Find matching characters
        while (j < code.length() && code.charAt(j) == c) {
          j++;
        }
      }

      // Add values
      newCode.append(c);
      offsets.add(j-i);

      // Increment counter
      i = j;
    }

    // New code!
    code = newCode.toString();
    codeLen = code.length();

    offsetTable = new int[codeLen];
    for (i=0;i<codeLen;i++) {
      offsetTable[i] = offsets.get(i);
    }
  }

  /**
   * Calculate the jumps for each opening and closing bracket
   * and put that info into the offset table
   */
  private void precalculateJumps() {
    // Loop over code
    for (int i = 0; i < codeLen; i++) {
      char c = code.charAt(i);
      if (c == '[') {
        // Find corresponding closing bracket
        int bc = 0; // Bracket counter
        for (int j = i; j < codeLen; j++) {
          char c2 = code.charAt(j);
          if (c2 == '[') {
            bc++;
          } else if (c2 == ']') {
            bc--;
          }
          if (bc == 0) {
            // Add table values and break
            offsetTable[i] = j;
            offsetTable[j] = i;
            break;
          }
        }
        if (bc != 0) {
          // Uh oh
          System.out.println(String.format(ERROR_UNMATCHED, i));
          error = true;
          return;
        }
      }
    }
    // Verify jump table
    for (int i=0;i<codeLen;i++) {
      int a = offsetTable[i];
      // Check closing brackets
      if (code.charAt(i) == ']') {
        if (code.charAt(a) != '[') {
          // Uh oh
          System.out.println(String.format(ERROR_UNMATCHED, a));
          error = true;
          return;
        }
      }
    }
  }

  /** Reset the pointer and tape. This does NOT modify the loaded code */
  public void reset() {
    ptr = 0;
    pc = 0;
    numIterations = 0;
    // Clear tape
    for (int i = 0; i < tape.length; i++) {
      tape[i] = 0;
    }
  }

  /**
   * Returns true if the pointer has not advanced beyond the end of the available code, and there is no error
   * preventing execution
   *
   * @return
   */
  public boolean available() {
    return !error && pc < codeLen && codeLen > 0;
  }

  /**
   * Run a single iteration and return the value currently at the pointer
   *
   * @return
   */
  public int step() {

    if (!available()) {
      return -1;
    }

    char instruction = code.charAt(pc);
    int offset = offsetTable[pc];
    int offsetOr1 = Math.max(offset, 1);

    switch (instruction) {
      case '>':
        // Move pointer right
        ptr = (ptr + offsetOr1) % tape.length;
        while (ptr >= tape.length) ptr -= tape.length;
        break;
      case '<':
        // Move pointer left
        ptr = (ptr - offsetOr1);
        while (ptr < 0) ptr += tape.length;
        break;
      case '+':
        // Increment tape value
        tape[ptr] += offsetOr1;
        break;
      case '-':
        // Decrement tape value
        tape[ptr] -= offsetOr1;
        break;
      case '.':
        // Print character
        if (out != null) {
          for (int i=0;i<offsetOr1;i++) {
            out.print((char) Byte.toUnsignedInt(tape[ptr]));
          }
        }
        break;
      case ',':
        // Input character
        if (in != null) {
          for (int i=0;i<offsetOr1;i++) {
            while (!scanner.hasNext())
              ;
            byte inp = (byte) scanner.next().charAt(0);
            tape[ptr] = inp;
          }
        }
        break;

      case '[':
        // Jump to next ']' if pointer value is 0
        if (tape[ptr] == 0) {
          // Jump
          pc = offset;
        }
        break;
      case ']':
        // Jump to previous '[' if pointer is nonzero
        if (tape[ptr] != 0) {
          // Jump
          pc = offset;
        }
        break;
    }
    // Increment pointer
    pc++;
    numIterations++;

    // Return current value
    return tape[ptr];
  }

  /** Run the code through to completion */
  public void run() {
    if (!error) {
      reset();
      while (available()) {
        step();
        // Break out
        if (maxIterations > 0 && numIterations > maxIterations) {
          break;
        }
      }
    } else {
      System.out.println(ERROR_CANNOT_RUN);
    }
  }

  /***
   * Used to create a new brainfuck session
   */
  public static class BfBuilder {

    // Default length of program tape
    private static final int DEFAULT_TAPE_LENGTH = 30000;

    private final String script;
    // IO streams
    private InputStream in;
    private PrintStream out;
    // Overrides
    private int tapeLengthOverride = DEFAULT_TAPE_LENGTH;
    private boolean optimize = false;
    private int maxIterations = -1;

    /**
     * Create a new session builder, populated with a BF script
     * This will also clean the loaded code and remove all non-BF code
     * @param code The brainfuck script to load
     */
    public BfBuilder(String code) {
      script = code.replaceAll("[^.,\\[\\]><+-]","");
    }

    /**
     * Set the input stream for this session
     * @param in
     * @return
     */
    public BfBuilder in(InputStream in) {
      this.in = in;
      return this;
    }

    /**
     * Set the output stream for this session
     * @param out
     * @return
     */
    public BfBuilder out(PrintStream out) {
      this.out = out;
      return this;
    }

    /**
     * Set the length of the tape for this session, overriding the default
     * of 30,000 cells.
     * @param length
     * @return
     */
    public BfBuilder tapeLength(int length) {
      if (length > 0) {
        this.tapeLengthOverride = length;
      }else{
        throw new IndexOutOfBoundsException("Invalid tape length " + length + "!");
      }
      return this;
    }

    /**
     * Tells the session builder to optimize the code being passed into the session
     * @return
     */
    public BfBuilder optimize() {
      this.optimize = true;
      return this;
    }

    /**
     * Tells the session builder to set the max number of iterations a program can go through before terminating
     * @param maxIterations
     * @return
     */
    public BfBuilder maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return this;
    }

    /**
     * Construct the brainfuck session
     * @return
     */
    public BfSession build() {
      BfSession session = new BfSession(this);
      // Return the completed session
      return session;
    }
  }

}
