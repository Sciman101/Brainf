package info.sciman;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/** Class to contain an instance of a running brainfuck program */
public class BFSession {

  // Error messages
  private static final String ERROR_UNMATCHED =
      "Error parsing input BF: Unbalanced bracket at col &d";
  private static final String ERROR_CANNOT_RUN = "Unable to run, unmatched brackets in source code";

  // Default length of program tape
  private static final int TAPE_LENGTH = 30000;

  // Streams for reading/writing data
  private PrintStream out;
  private InputStream in;
  private Scanner scanner;

  // Jump table
  private HashMap<Integer, Integer> jumpTable = new HashMap<>();

  // 'Tape' used to hold memory
  private byte[] tape;
  // Pointer
  private int ptr;
  // Code pointer
  private int cptr;

  // Actual code
  private String code;
  private int codeLen;
  private boolean error; // True if there was an error building the jump table

  /** Default constructor */
  public BFSession(PrintStream out, InputStream in) {
    tape = new byte[TAPE_LENGTH];
    this.out = out;
    this.in = in;

    // Create bufferedreader
    scanner = new Scanner(this.in);
    scanner.useDelimiter("");

    ptr = 0;
    cptr = 0;
  }

  // Getters
  public int getPointerPos() {
    return ptr;
  }

  public int getCodePos() {
    return cptr;
  }

  public String getCode() {
    return code;
  }

  public boolean checkBracketError() { // Returns true if there was an error parsing brackets
    return error;
  }

  public byte getTapeValue(int pos) {
    if (pos > 0 && pos < TAPE_LENGTH) {
      return tape[pos];
    } else {
      throw new IndexOutOfBoundsException(
          pos + " is out of the bounds of the BF tape [0," + TAPE_LENGTH + ")");
    }
  }

  /**
   * Load BF code into the session
   *
   * @param code the code to load
   */
  public void load(String code) {
    this.code = code;
    this.codeLen = code.length();
    error = false;

    // Compute jump table
    jumpTable.clear();
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
            jumpTable.put(i, j);
            jumpTable.put(j, i);
            // System.out.println("Jump established between " + i + " and " + j);
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
    for (Map.Entry<Integer, Integer> pair : jumpTable.entrySet()) {
      int a = pair.getKey();
      int b = pair.getValue();
      // Check closing brackets
      if (code.charAt(a) == ']') {
        if (code.charAt(b) != '[') {
          // Uh oh
          System.out.println(String.format(ERROR_UNMATCHED, a));
          error = true;
          return;
        }
      }
    }

    reset();
  }

  /** Reset the pointer and tape */
  public void reset() {
    ptr = 0;
    cptr = 0;
    // Clear tape
    for (int i = 0; i < tape.length; i++) {
      tape[i] = 0;
    }
  }

  /**
   * Returns true if the pointer has not advanced beyond the end of the available code
   *
   * @return
   */
  public boolean available() {
    return !error && cptr < codeLen && codeLen > 0;
  }

  /**
   * Run a single iteration and return the value currently at the pointer
   *
   * @return
   */
  public int advance() {

    if (!available()) {
      return -1;
    }

    char instruction = code.charAt(cptr);
    switch (instruction) {
      case '>':
        // Move pointer right
        if (++ptr >= tape.length) {
          ptr = 0;
        }
        break;
      case '<':
        // Move pointer left
        if (--ptr < 0) {
          ptr = tape.length - 1;
        }
        break;
      case '+':
        // Increment tape value
        tape[ptr]++;
        break;
      case '-':
        // Decrement tape value
        tape[ptr]--;
        break;
      case '.':
        // Print character
        out.print((char) Byte.toUnsignedInt(tape[ptr]));
        break;
      case ',':
        // Input character
        while (!scanner.hasNext())
          ;
        byte inp = (byte) scanner.next().charAt(0);
        tape[ptr] = inp;
        break;

      case '[':
        // Jump to next ']' if pointer value is 0
        if (tape[ptr] == 0) {
          // Jump
          cptr = jumpTable.get(cptr);
        }
        break;
      case ']':
        // Jump to previous '[' if pointer is nonzero
        if (tape[ptr] != 0) {
          // Jump
          cptr = jumpTable.get(cptr);
        }
        break;
    }
    // Increment pointer
    cptr++;

    return tape[ptr];
  }

  /** Run the code through to completion */
  public void run() {
    if (!error) {
      reset();
      while (available()) {
        advance();
      }
    } else {
      System.out.println(ERROR_CANNOT_RUN);
    }
  }
}
