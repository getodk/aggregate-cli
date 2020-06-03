package org.opendatakit.cli;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import de.vandermeer.asciitable.AsciiTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import org.opendatakit.aggregate.cli.operations.Exceptions;

public class Console {
  private final HelpFormatter helpFormatter;
  private final InputStream inStream;
  private final PrintStream outStream;
  private final PrintStream errorStream;
  private boolean verboseMode = false;
  private boolean alwaysYesMode = false;

  public Console(HelpFormatter helpFormatter, InputStream inStream, PrintStream outStream, PrintStream errorStream) {
    this.helpFormatter = helpFormatter;
    this.inStream = inStream;
    this.outStream = outStream;
    this.errorStream = errorStream;
  }

  public static Console std(HelpFormatter helpFormatter) {
    return new Console(helpFormatter, System.in, System.out, System.err);
  }

  public void out() {
    outStream.println();
  }

  public void out(String text) {
    outStream.println(text);
  }

  public void error() {
    errorStream.println();
  }

  public void error(String text) {
    errorStream.println(text);
  }

  public void exit() {
    System.exit(0);
  }

  public void exit(int status) {
    out("You can ask for support at https://forum.getodk.org/c/support");
    System.exit(status);
  }

  public void table(List<List<String>> rows, String... headers) {
    AsciiTable table = new AsciiTable();
    table.addRule();
    table.addRow(Arrays.asList(headers));
    table.addRule();
    rows.forEach(release -> {
      table.addRow(release);
      table.addRule();
    });
    outStream.println(table.render());
  }

  public void execute(String command) {
    execute(command, false);
  }

  public void execute(String command, boolean ignoreErrors) {
    if (verboseMode) {
      outStream.println(command);
    }

    Process process = getProcess(command);
    ProcessWatcher processWatcher = ProcessWatcher.attach(process)
        .onOut(outStream::println)
        .onError(errorStream::println)
        .verbose(verboseMode)
        .build();
    newSingleThreadExecutor().submit(processWatcher);
    int exitCode = waitFor(process);
    if (exitCode != 0 && !ignoreErrors)
      throw new RuntimeException("Command '" + command + "' exited with exit code " + exitCode);
  }

  private static int waitFor(Process process) {
    try {
      return process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static Process getProcess(String command) {
    try {
      return Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void setVerboseMode(boolean enabled) {
    if (enabled) {
      outStream.println("[Console] Enabling verbose mode. Showing all commands");
      outStream.println();
    }
    verboseMode = enabled;
  }

  public void setAlwaysYesMode(boolean enabled) {
    if (enabled && verboseMode) {
      outStream.println("[Console] Assuming 'yes' in all confirm prompts");
      outStream.println();
    }
    alwaysYesMode = enabled;
  }

  public boolean confirm(String message) {
    outStream.print(String.format("%s (YES/no): ", message));
    if (alwaysYesMode) {
      outStream.println("yes");
      return true;
    }
    String input = new Scanner(inStream).nextLine().trim();
    if (!input.isEmpty() && !input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("no")) {
      errorStream.println("You need to answer 'yes' or 'no'.");
      return confirm(message);
    } else
      return !input.equalsIgnoreCase("no");
  }

  public void block(String name, Runnable block) {
    out("- " + name);
    block.run();
    out("  done");
    out();
  }

  public void printHelp(Set<Operation> requiredOperations, Set<Operation> operations) {
    out(helpFormatter.renderHelp(requiredOperations, operations));
  }

  public void requireSuperuser() {
    StringBuilder out = new StringBuilder();
    Process process = getProcess("whoami");
    ProcessWatcher processWatcher = ProcessWatcher.attach(process)
        .onOut(out::append)
        .build();
    newSingleThreadExecutor().submit(processWatcher);
    waitFor(process);
    if (!out.toString().equals("root"))
      throw new Exceptions.OperationException("Superuser privileges required. Try running it with sudo.");
  }

  private static class ProcessWatcher implements Runnable {
    private final Process process;
    private final Consumer<String> outConsumer;
    private final Consumer<String> errorConsumer;
    private final boolean verboseMode;

    public ProcessWatcher(Process process, Consumer<String> outConsumer, Consumer<String> errorConsumer, boolean verboseMode) {
      this.process = process;
      this.outConsumer = outConsumer;
      this.errorConsumer = errorConsumer;
      this.verboseMode = verboseMode;
    }

    static ProcessWatcherBuilder attach(Process process) {
      return new ProcessWatcherBuilder(process);
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(line -> {
        if (verboseMode)
          outConsumer.accept(line);
      });
      new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(line -> {
        if (verboseMode)
          errorConsumer.accept(line);
      });
    }
  }

  private static class ProcessWatcherBuilder {
    private final Process process;
    private Consumer<String> outConsumer = s -> {};
    private Consumer<String> errorConsumer = s -> {};
    private boolean verboseMode = false;

    public ProcessWatcherBuilder(Process process) {
      this.process = process;
    }

    public ProcessWatcherBuilder onOut(Consumer<String> consumer) {
      outConsumer = consumer;
      return this;
    }

    public ProcessWatcherBuilder onError(Consumer<String> consumer) {
      errorConsumer = consumer;
      return this;
    }

    public ProcessWatcherBuilder verbose(boolean enabled) {
      this.verboseMode = enabled;
      return this;
    }

    public ProcessWatcher build() {
      return new ProcessWatcher(process, outConsumer, errorConsumer, verboseMode);
    }
  }
}
