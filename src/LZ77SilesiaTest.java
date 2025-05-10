import core.LZ77Codec;
import core.LZ77Encoder;
import core.LZ77Decoder;
import io.FileEncoder;
import io.FileDecoder;
import metrics.CompressionMetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is designed to run automated tests on the Silesia Corpus
 * for benchmarking the LZ77 compression algorithm with different parameters.
 * <p>
 * It evaluates multiple window and look-ahead buffer sizes, collects compression
 * metrics, verifies file integrity, and generates both a summary CSV and a detailed report.
 */
public class LZ77SilesiaTest {

    // Default parameters
    private static final int[] WINDOW_SIZES = {1024, 4096, 8192, 16384};
    private static final int[] LOOKAHEAD_SIZES = {16, 32, 64, 128};

    /**
     * Entry point for running the benchmark tool.
     * Takes the path to the Silesia Corpus directory as an optional command-line argument.
     *
     * @param args Optional command-line arguments (first argument: path to corpus).
     */
    public static void main(String[] args) {
        System.out.println("=== LZ77 Silesia Corpus Benchmark Tool ===");
        System.out.println("This program runs comprehensive tests on the Silesia Corpus");
        System.out.println("with different window and look-ahead buffer sizes.");

        // Get corpus directory from command line or use default
        String corpusPath = args.length > 0 ? args[0] : "./silesia";

        File corpusDir = new File(corpusPath);
        if (!corpusDir.exists() || !corpusDir.isDirectory()) {
            System.out.println("Error: Invalid Silesia Corpus directory path: " + corpusPath);
            System.out.println("Usage: java LZ77SilesiaTest [path-to-silesia-corpus]");
            return;
        }

        // Create timestamp for results directory
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String resultsDir = "./benchmark-" + timestamp;
        File resultsDirFile = new File(resultsDir);
        if (!resultsDirFile.mkdir()) {
            System.err.println("Warning: Could not create directory: " + resultsDir);
        }

        // Initialize summary CSV file
        Path summaryPath = Paths.get(resultsDir + "/summary.csv");
        try {
            Files.writeString(summaryPath,
                    "Filename,Window Size,Look-ahead Size,Original Size (bytes),Compressed Size (bytes),"
                            + "Compression Ratio,Avg Code Length (bits/symbol),Encoding Time (ms),Decoding Time (ms)\n");
        } catch (IOException e) {
            System.err.println("Error creating summary file: " + e.getMessage());
            return;
        }

        // List Silesia Corpus files
        File[] files = corpusDir.listFiles(f -> f.isFile() && !f.getName().startsWith("."));
        if (files == null || files.length == 0) {
            System.out.println("No files found in the Silesia Corpus directory");
            return;
        }

        System.out.println("\nFound " + files.length + " files in the Silesia Corpus");
        System.out.println("Results will be saved to: " + resultsDir);
        System.out.println("\nRunning tests with multiple window and look-ahead buffer sizes...");

        // Test each combination of window size and look-ahead buffer size
        for (int windowSize : WINDOW_SIZES) {
            for (int lookAheadSize : LOOKAHEAD_SIZES) {
                System.out.println("\n=== Testing with Window Size: " + windowSize +
                        ", Look-ahead Size: " + lookAheadSize + " ===");

                String configDir = resultsDir + "/w" + windowSize + "_la" + lookAheadSize;
                File configDirFile = new File(configDir);
                if (!configDirFile.mkdir()) {
                    System.err.println("Warning: Could not create directory: " + configDir);
                }

                System.out.println("------------------------------------------------------------");
                System.out.printf("%-20s %-15s %-15s %-15s %-15s %-15s %-15s\n",
                        "Filename", "Original", "Compressed", "Ratio", "Bits/Symbol", "Encode (ms)", "Decode (ms)");
                System.out.println("------------------------------------------------------------");

                for (File file : files) {
                    try {
                        testFile(file, windowSize, lookAheadSize, configDir, summaryPath.toString());
                    } catch (Exception e) {
                        System.err.println("Error processing file " + file.getName() + ": " + e.getMessage());
                        System.err.println("Stack trace: " + e);
                    }
                }
            }
        }

        System.out.println("\n=== Benchmark completed ===");
        System.out.println("Results saved to: " + resultsDir);
        System.out.println("Summary file: " + summaryPath);

        // Generate report if needed
        generateReport(resultsDir);
    }

    /**
     * Tests a single file using LZ77 compression with the given parameters.
     * Records metrics, verifies output correctness, and appends results to the summary CSV.
     *
     * @param file         The file to be compressed.
     * @param windowSize   Size of the sliding window.
     * @param lookAheadSize Size of the look-ahead buffer.
     * @param outputDir    Directory to store compressed and decompressed files.
     * @param summaryFile  Path to the summary CSV file.
     * @throws IOException If an I/O error occurs during compression or writing results.
     */
    private static void testFile(File file, int windowSize, int lookAheadSize,
                                 String outputDir, String summaryFile) throws IOException {
        String inputPath = file.getPath();
        String compressedPath = outputDir + "/" + file.getName() + ".lz77";
        String decompressedPath = outputDir + "/" + file.getName() + ".decoded";

        // Initialize codec and IO components
        FileEncoder encoder = new FileEncoder(new LZ77Encoder(windowSize, lookAheadSize));
        FileDecoder decoder = new FileDecoder(new LZ77Decoder());
        CompressionMetrics metrics = new CompressionMetrics();

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

        // Verify file integrity
        verifyFileIntegrity(inputPath, decompressedPath);

        // Append to summary CSV
        String csvLine = String.format("%s,%d,%d,%d,%d,%.4f,%.4f,%.2f,%.2f",
                file.getName(),
                windowSize,
                lookAheadSize,
                metricsMap.get("originalSize").longValue(),
                metricsMap.get("compressedSize").longValue(),
                metricsMap.get("compressionRatio"),
                metricsMap.get("averageCodeLength"),
                encodingTime,
                decodingTime);

        Files.writeString(
                Paths.get(summaryFile),
                csvLine + System.lineSeparator(),
                StandardOpenOption.APPEND
        );
    }

    /**
     * Verifies the integrity of the decompressed file by comparing its content
     * byte-for-byte with the original file.
     *
     * @param originalPath   Path to the original input file.
     * @param decodedPath    Path to the decoded (decompressed) output file.
     */
    private static void verifyFileIntegrity(String originalPath, String decodedPath) {
        try {
            byte[] originalBytes = Files.readAllBytes(Paths.get(originalPath));
            byte[] decodedBytes = Files.readAllBytes(Paths.get(decodedPath));

            if (originalBytes.length != decodedBytes.length) {
                System.out.println("  ⚠️ WARNING: File size mismatch - Original: " +
                        originalBytes.length + ", Decoded: " + decodedBytes.length);
                return;
            }

            for (int i = 0; i < originalBytes.length; i++) {
                if (originalBytes[i] != decodedBytes[i]) {
                    System.out.println("  ⚠️ WARNING: File content mismatch at position " + i);
                    return;
                }
            }

            // If we get here, files are identical
        } catch (IOException e) {
            System.out.println("  ⚠️ ERROR verifying file integrity: " + e.getMessage());
        }
    }

    /**
     * Generates a basic Markdown report summarizing the test setup and placeholder
     * sections for further analysis of compression performance.
     *
     * @param resultsDir The directory where the report will be saved.
     */
    private static void generateReport(String resultsDir) {
        try {
            String reportPath = resultsDir + "/report.md";
            StringBuilder report = new StringBuilder();

            report.append("# LZ77 Compression Algorithm Performance Report\n\n");
            report.append("## Test Configuration\n\n");
            report.append("- Algorithm: LZ77 (Lempel-Ziv 77)\n");
            report.append("- Window Sizes: ").append(arrayToString(WINDOW_SIZES)).append("\n");
            report.append("- Look-ahead Buffer Sizes: ").append(arrayToString(LOOKAHEAD_SIZES)).append("\n");
            report.append("- Test Corpus: Silesia Corpus\n\n");

            report.append("## Summary of Results\n\n");
            report.append("The best compression performance was achieved with the following parameters:\n\n");
            // Here you would analyze the summary.csv to find the best parameters

            report.append("## Analysis\n\n");
            report.append("### Effect of Window Size\n\n");
            // Analysis of how window size affects compression ratio and performance

            report.append("### Effect of Look-ahead Buffer Size\n\n");
            // Analysis of how look-ahead buffer size affects compression ratio and performance

            report.append("### File Type Analysis\n\n");
            // Analysis of how different file types in the corpus react to LZ77 compression

            report.append("## Conclusion\n\n");
            // Conclusion about the effectiveness of LZ77 for different types of data

            Files.writeString(Paths.get(reportPath), report.toString());
            System.out.println("\nDetailed report generated: " + reportPath);

        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Utility method to convert an array of integers to a comma-separated string.
     *
     * @param array The array to convert.
     * @return A string representation of the array.
     */
    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}