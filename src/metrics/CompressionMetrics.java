package metrics;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Calculates and formats compression statistics.
 */
public class CompressionMetrics {

    public Map<String, Double> calculateMetrics(String originalFilePath, String compressedFilePath) throws IOException {
        Map<String, Double> metrics = new HashMap<>();
        long originalSize = Files.size(Paths.get(originalFilePath));
        long compressedSize = Files.size(Paths.get(compressedFilePath));

        double compressionRatio = (double) originalSize / compressedSize;
        double avgCodeLength = (compressedSize * 8.0) / originalSize;

        metrics.put("originalSize", (double) originalSize);
        metrics.put("compressedSize", (double) compressedSize);
        metrics.put("compressionRatio", compressionRatio);
        metrics.put("averageCodeLength", avgCodeLength);

        return metrics;
    }

    public String formatResults(Map<String, Double> metrics, double encodingTime, double decodingTime) {
        StringBuilder sb = new StringBuilder();

        sb.append("Original size: ").append(String.format("%d", metrics.get("originalSize").longValue())).append(" bytes\n");
        sb.append("Compressed size: ").append(String.format("%d", metrics.get("compressedSize").longValue())).append(" bytes\n");
        sb.append("Compression ratio: ").append(String.format("%.4f", metrics.get("compressionRatio"))).append(" (higher is better)\n");
        sb.append("Average code length: ").append(String.format("%.4f", metrics.get("averageCodeLength"))).append(" bits/symbol\n");
        sb.append("Encoding time: ").append(String.format("%.2f", encodingTime)).append(" ms\n");
        sb.append("Decoding time: ").append(String.format("%.2f", decodingTime)).append(" ms\n");

        return sb.toString();
    }

    public String toCsvLine(String fileName, Map<String, Double> metrics, double encodingTime, double decodingTime) {
        return String.format("%s,%d,%d,%.4f,%.4f,%.2f,%.2f",
                fileName,
                metrics.get("originalSize").longValue(),
                metrics.get("compressedSize").longValue(),
                metrics.get("compressionRatio"),
                metrics.get("averageCodeLength"),
                encodingTime,
                decodingTime);
    }
}
