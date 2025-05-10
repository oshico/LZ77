import core.LZ77Codec;
import core.LZ77Encoder;
import core.LZ77Decoder;
import io.FileEncoder;
import io.FileDecoder;
import metrics.CompressionMetrics;
import model.LZ77Token;
import util.TraceUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for running the LZ77 compression tool in different modes:
 * - Custom input tracing
 * - Corpus compression benchmarking
 * - File compression/decompression
 * - Educational testing with poetry
 */
public class LZ77Main {
    private static final int DEFAULT_WINDOW_SIZE = 4096;
    private static final int DEFAULT_LOOKAHEAD_SIZE = 40;

    /**
     * Entry point for the LZ77 compression tool.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("=== LZ77 Compression Algorithm Educational Tool ===");
        System.out.println("This program demonstrates the LZ77 algorithm for lossless compression");
        System.out.println("Created as an educational tool for understanding how LZ77 works");
        System.out.println("------------------------------------------------------");

        do {
            System.out.println("\nSelect operation mode:");
            System.out.println("1. Test with custom input (trace mode)");
            System.out.println("2. Run Silesia Corpus compression test");
            System.out.println("3. Run Fernando Pessoa poem test");
            System.out.println("4. Compress a single file");
            System.out.println("5. Decompress a file");
            System.out.println("0. Exit");
            System.out.print("\nYour choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    runCustomInputTest(scanner);
                    break;
                case 2:
                    runSilesiaCorpusTest(scanner);
                    break;
                case 3:
                    runPessoaPoemTest();
                    break;
                case 4:
                    compressSingleFile(scanner);
                    break;
                case 5:
                    decompressSingleFile(scanner);
                    break;
                case 0:
                    System.out.println("Exiting program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 0);

        scanner.close();
    }

    /**
     * Runs a tracing demonstration using custom user-provided input.
     *
     * @param scanner A Scanner object for user input.
     */
    private static void runCustomInputTest(Scanner scanner) {
        System.out.println("\n=== Custom Input Test (Trace Mode) ===");
        System.out.print("Enter text to compress: ");
        String input = scanner.nextLine();

        System.out.print("Enter window size (default " + DEFAULT_WINDOW_SIZE + "): ");
        String windowSizeStr = scanner.nextLine();
        int windowSize = windowSizeStr.isEmpty() ? DEFAULT_WINDOW_SIZE : Integer.parseInt(windowSizeStr);

        System.out.print("Enter look-ahead buffer size (default " + DEFAULT_LOOKAHEAD_SIZE + "): ");
        String lookAheadSizeStr = scanner.nextLine();
        int lookAheadSize = lookAheadSizeStr.isEmpty() ? DEFAULT_LOOKAHEAD_SIZE : Integer.parseInt(lookAheadSizeStr);

        System.out.println("\nRunning LZ77 encoding with tracing...");
        List<LZ77Token> tokens = TraceUtil.traceEncoding(input, windowSize, lookAheadSize);

        System.out.println("\nNow running LZ77 decoding with tracing...");
        TraceUtil.traceDecoding(tokens);

        // Verify integrity
        LZ77Codec codec = new LZ77Codec(windowSize, lookAheadSize);
        boolean isIntact = codec.verifyIntegrity(input);
        System.out.println("\nVerification: " + (isIntact ? "PASSED - Input and output match" : "FAILED - Data corruption detected"));
    }

    /**
     * Runs compression tests on all files in the Silesia Corpus directory.
     *
     * @param scanner A Scanner object for reading user inputs.
     */
    private static void runSilesiaCorpusTest(Scanner scanner) {
        System.out.println("\n=== Silesia Corpus Compression Test ===");
        System.out.print("Enter path to Silesia Corpus directory: ");
        String corpusPath = scanner.nextLine();

        File corpusDir = new File(corpusPath);
        if (!corpusDir.exists() || !corpusDir.isDirectory()) {
            System.out.println("Error: Invalid directory path");
            return;
        }

        System.out.print("Enter window size (default " + DEFAULT_WINDOW_SIZE + "): ");
        String windowSizeStr = scanner.nextLine();
        int windowSize = windowSizeStr.isEmpty() ? DEFAULT_WINDOW_SIZE : Integer.parseInt(windowSizeStr);

        System.out.print("Enter look-ahead buffer size (default " + DEFAULT_LOOKAHEAD_SIZE + "): ");
        String lookAheadSizeStr = scanner.nextLine();
        int lookAheadSize = lookAheadSizeStr.isEmpty() ? DEFAULT_LOOKAHEAD_SIZE : Integer.parseInt(lookAheadSizeStr);

        String outputPath = corpusPath + "/results";
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        File csvFile = new File(outputPath + "/results.csv");
        try {
            if (!csvFile.exists()) {
                csvFile.createNewFile();
                Files.write(Paths.get(csvFile.getPath()),
                        "Filename,Original Size (bytes),Compressed Size (bytes),Compression Ratio,Avg Code Length (bits/symbol),Encoding Time (ms),Decoding Time (ms)\n".getBytes());
            }
        } catch (IOException e) {
            System.out.println("Error creating results file: " + e.getMessage());
            return;
        }

        LZ77Codec codec = new LZ77Codec(windowSize, lookAheadSize);
        FileEncoder encoder = new FileEncoder(new LZ77Encoder(windowSize, lookAheadSize));
        FileDecoder decoder = new FileDecoder(new LZ77Decoder());
        CompressionMetrics metrics = new CompressionMetrics();

        System.out.println("\nProcessing Silesia Corpus files...");
        System.out.println("Window Size: " + windowSize + ", Look-ahead Size: " + lookAheadSize);
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-20s %-15s %-15s %-15s %-15s %-15s %-15s\n",
                "Filename", "Original", "Compressed", "Ratio", "Bits/Symbol", "Encode (ms)", "Decode (ms)");
        System.out.println("------------------------------------------------------------");

        File[] files = corpusDir.listFiles(f -> f.isFile() && !f.getName().startsWith("."));
        if (files == null || files.length == 0) {
            System.out.println("No files found in the directory");
            return;
        }

        for (File file : files) {
            try {
                String inputPath = file.getPath();
                String compressedPath = outputPath + "/" + file.getName() + ".lz77";
                String decompressedPath = outputPath + "/" + file.getName() + ".decoded";

                // Encode
                double encodingTime = encoder.encodeFile(inputPath, compressedPath);

                // Decode
                double decodingTime = decoder.decodeFile(compressedPath, decompressedPath) / 1_000_000;

                // Calculate metrics
                Map<String, Double> metricsMap = metrics.calculateMetrics(inputPath, compressedPath);

                // Print results
                System.out.printf("%-20s %-15d %-15d %-15.4f %-15.4f %-15.2f %-15.2f\n",
                        file.getName(),
                        metricsMap.get("originalSize").longValue(),
                        metricsMap.get("compressedSize").longValue(),
                        metricsMap.get("compressionRatio"),
                        metricsMap.get("averageCodeLength"),
                        encodingTime,
                        decodingTime);

                // Append to CSV
                String csvLine = metrics.toCsvLine(file.getName(), metricsMap, encodingTime, decodingTime);
                Files.write(Paths.get(csvFile.getPath()),
                        (csvLine + "\n").getBytes(),
                        java.nio.file.StandardOpenOption.APPEND);

            } catch (IOException e) {
                System.out.println("Error processing file " + file.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("Compression test completed. Results saved to " + csvFile.getPath());
    }

    /**
     * Runs a small test using a Fernando Pessoa poem to demonstrate compression.
     * Useful for visualizing compression on natural language input.
     */
    private static void runPessoaPoemTest() {
        System.out.println("\n=== Fernando Pessoa Poem Test ===");

        // The poem by Fernando Pessoa
        String poem = "Não sei quantas almas tenho. Cada momento mudei. Continuamente me estranho. "
                + "Nunca me vi nem achei. De tanto ser, só tenho alma. Quem tem alma não tem calma. "
                + "Quem vê é só o que vê, Quem sente não é quem é, Atento ao que sou e vejo, "
                + "Torno-me eles e não eu. Cada meu sonho ou desejo, É do que nasce e não meu.";

        System.out.println("Poem (Fernando Pessoa - 'Não sei quantas almas tenho'):");
        System.out.println("\"" + poem + "\"");
        System.out.println("\nLength: " + poem.length() + " characters");

        // Use smaller window and look-ahead sizes for educational visualization
        int windowSize = 30;
        int lookAheadSize = 15;

        System.out.println("\nRunning LZ77 encoding with window size = " + windowSize
                + " and look-ahead size = " + lookAheadSize);

        List<LZ77Token> tokens = TraceUtil.traceEncoding(poem, windowSize, lookAheadSize);

        System.out.println("\nNow running LZ77 decoding to verify results:");
        TraceUtil.traceDecoding(tokens);

        // Calculate compression stats
        int originalSize = poem.length() * 2; // Assuming 2 bytes per character (UTF-16)
        int tokenSize = tokens.size() * (4 + 4 + 2); // 4 bytes for distance, 4 for length, 2 for char
        double compressionRatio = (double) originalSize / tokenSize;
        double bitsPerSymbol = (tokenSize * 8.0) / poem.length();

        System.out.println("\n=== Compression Statistics ===");
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + tokenSize + " bytes");
        System.out.println("Compression ratio: " + String.format("%.4f", compressionRatio));
        System.out.println("Average code length: " + String.format("%.4f", bitsPerSymbol) + " bits/symbol");
        System.out.println("Number of tokens: " + tokens.size());
    }

    /**
     * Prompts the user to compress a single file using LZ77.
     *
     * @param scanner Scanner object to read user input.
     */
    private static void compressSingleFile(Scanner scanner) {
        System.out.println("\n=== Compress Single File ===");
        System.out.print("Enter path to input file: ");
        String inputPath = scanner.nextLine();

        System.out.print("Enter path for output file: ");
        String outputPath = scanner.nextLine();

        System.out.print("Enter window size (default " + DEFAULT_WINDOW_SIZE + "): ");
        String windowSizeStr = scanner.nextLine();
        int windowSize = windowSizeStr.isEmpty() ? DEFAULT_WINDOW_SIZE : Integer.parseInt(windowSizeStr);

        System.out.print("Enter look-ahead buffer size (default " + DEFAULT_LOOKAHEAD_SIZE + "): ");
        String lookAheadSizeStr = scanner.nextLine();
        int lookAheadSize = lookAheadSizeStr.isEmpty() ? DEFAULT_LOOKAHEAD_SIZE : Integer.parseInt(lookAheadSizeStr);

        try {
            LZ77Codec codec = new LZ77Codec(windowSize, lookAheadSize);
            FileEncoder encoder = new FileEncoder(new LZ77Encoder(windowSize, lookAheadSize));

            System.out.println("\nCompressing file...");
            long encodingTime = encoder.encodeFile(inputPath, outputPath);

            // Calculate metrics
            CompressionMetrics metrics = new CompressionMetrics();
            Map<String, Double> metricsMap = metrics.calculateMetrics(inputPath, outputPath);

            // Print results
            System.out.println("\n" + metrics.formatResults(metricsMap, encodingTime, 0));
            System.out.println("File successfully compressed to: " + outputPath);

        } catch (IOException e) {
            System.out.println("Error compressing file: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to decompress a previously compressed LZ77 file.
     *
     * @param scanner Scanner object to read user input.
     */
    private static void decompressSingleFile(Scanner scanner) {
        System.out.println("\n=== Decompress Single File ===");
        System.out.print("Enter path to compressed file: ");
        String inputPath = scanner.nextLine();

        System.out.print("Enter path for decompressed output file: ");
        String outputPath = scanner.nextLine();

        try {
            LZ77Codec codec = new LZ77Codec(DEFAULT_WINDOW_SIZE, DEFAULT_LOOKAHEAD_SIZE);
            FileDecoder decoder = new FileDecoder(new LZ77Decoder());

            System.out.println("\nDecompressing file...");
            long decodingTime = decoder.decodeFile(inputPath, outputPath);

            System.out.println("Decompression time: " + (decodingTime / 1_000_000) + " ms");
            System.out.println("File successfully decompressed to: " + outputPath);

        } catch (IOException e) {
            System.out.println("Error decompressing file: " + e.getMessage());
        }
    }
}