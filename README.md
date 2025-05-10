# 🔧 LZ77 Compression Benchmarking Suite

A Java-based toolkit for compressing and decompressing files using the **LZ77 (Lempel-Ziv 77)** algorithm. This project includes tools for encoding, decoding, measuring compression metrics, and benchmarking performance on the **Silesia Corpus**.

---

## 📦 Features

- **LZ77 Compression & Decompression**
- **Pluggable Window & Look-Ahead Sizes**
- **Compression Metrics Calculation**
- **Corpus-wide Benchmarking (Silesia Corpus)**
- **CSV Summary Output**
- **Auto-generated Markdown Report**
- **File Integrity Verification**

---

## 📁 Project Structure

```
src/
├── core/
│ ├── LZ77Codec.java
│ ├── LZ77Encoder.java
│ └── LZ77Decoder.java
├── io/
│ ├── FileEncoder.java
│ └── FileDecoder.java
├── metrics/
│ └── CompressionMetrics.java
└── LZ77SilesiaTest.java
```


---

## 🚀 Getting Started

### ✅ Prerequisites

- **Java 11 or higher**
- **Maven (optional, if you want to manage dependencies)**

---

### 📥 Installation

#### 1. Clone the repository

```bash
git clone https://github.com/yourusername/lz77-benchmark.git
cd lz77-benchmark\
```

#### 2. Compile the project

```bash
javac -d out $(find ./src -name "*.java")
```

#### 3. Run the benchmark (with default Silesia Corpus path)

```bash
java -cp out LZ77SilesiaTest ./silesia
```

    ⚠️ Make sure the Silesia Compression Corpus is downloaded and extracted to the ./silesia folder.

### ⚙️ Usage

```bash
java -cp out LZ77SilesiaTest [path-to-silesia-corpus]
```

- **Runs multiple compression tests with varying parameters.**

- **Generates:**
    - **Compressed and decompressed files**
    - **CSV summary of compression metrics**
    - **Markdown report in the output folder**

### 📊 Output Example

```
benchmark-20250510-140501/ 
├── summary.csv
├── report.md
├── w1024_la16/
│   ├── dickens.lz77
│   ├── dickens.decoded
│   └── ...
└── ...
```

### 📈 Metrics Tracked

- **Original and Compressed File Sizes**
- **Compression Ratio**
- **Average Code Length (bits/symbol)**
- **Encoding Time (ms)**
- **Decoding Time (ms)**

### ✅ License

**MIT License — see LICENSE for details.**

### ✍️ Author

**oshico & nunosilva24**

### 📬 Feedback or Issues?

**Open an issue or submit a pull request!**
