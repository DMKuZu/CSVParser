# CSVParser

A JavaFX desktop application that generates printable PDF vouchers from CSV data. Reads voucher codes from CSV files and combines them with customizable parameters to produce formatted, multi-card PDF documents.

**Use Case**: Ideal for service providers needing to generate bulk voucher documents with dynamic pricing, validity periods, uptime specifications, and bandwidth information.

## Prerequisites

- **Java 15 or higher**
- **Maven 3.6+** (or use the included Maven Wrapper)
- **JavaFX 17.0.6** (managed as a dependency)

## Setup & Build

### Clone and Build
```bash
git clone <repository-url>
cd CSVParser
mvn clean package
```

### Run the Application
```bash
mvn clean javafx:run
```

Or if you have the compiled JAR:
```bash
java -jar target/CSVParser-1.0-SNAPSHOT-shaded.jar
```

## Project Structure

```
CSVParser/
├── src/main/java/com/kuzu/csvparser/
│   ├── Launcher.java              # Application entry point
│   ├── Main.java                  # JavaFX application initialization
│   ├── MainController.java        # UI controller for user interactions
│   ├── CSVProcessor.java          # Core CSV processing and PDF generation
│   └── module-info.java           # Java module descriptor
├── src/main/resources/
│   └── com/kuzu/csvparser/
│       └── GUI-view.fxml          # JavaFX UI layout
├── template/
│   ├── root_template.html         # Main HTML template wrapper
│   ├── voucher_template.html      # Individual voucher card template
│   └── bg_voucher.jpg             # Background image for vouchers
├── pom.xml                        # Maven configuration
└── .mvn/                          # Maven wrapper
```

## How It Works

1. **User launches the application** → JavaFX GUI opens with input fields for voucher parameters
2. **User selects a CSV file** → Contains voucher codes (one per row)
3. **User enters voucher parameters** → Price, service name, uptime, validity, speed
4. **Click "Generate vouchers"** → Application processes:
   - Validates input (numeric price check)
   - Parses CSV file using Apache Commons CSV
   - Substitutes parameters into HTML template
   - Converts HTML to PDF using OpenHtmlToPdf
   - Opens PDF in default viewer
5. **Output file saved** → `[original-filename]_vouchers.pdf`

## Key Components

- **MainController.java**: Handles GUI interactions, file selection, and input validation
- **CSVProcessor.java**: Reads CSV, processes templates, generates PDF, manages template directory detection
- **HTML Templates**: Customizable voucher card design with CSS styling and background images
- **GUI-view.fxml**: JavaFX layout with input fields for: Price, Name, Uptime, Validity, Speed

## Core Dependencies

- **Apache Commons CSV 1.9.0** - CSV file parsing
- **OpenHtmlToPdf 1.0.10** - HTML to PDF conversion
- **JSoup 1.15.4** - HTML parsing and manipulation
- **JavaFX 17.0.6** - Desktop GUI framework
- **JUnit 5.10.2** - Testing framework

## Development Notes

### Template Placeholders
The HTML templates use these placeholders, replaced at runtime:
- `${price}` - Voucher price
- `${code}` - Voucher code (from CSV)
- `${name}` - Service name
- `${uptime}` - Uptime duration
- `${validity}` - Validity period
- `${speed}` - Speed specification

### Module Requirements
Declared in `module-info.java`:
```
javafx.controls, javafx.fxml, commons.csv, openhtmltopdf, jsoup, java.desktop, java.logging
```

### Build Configuration
- **Compiler Target**: Java 15
- **Character Encoding**: UTF-8
- **Maven Plugins**: Shade Plugin (fat JAR), JavaFX Maven Plugin
- **PDF Output Format**: A4 page size with 2mm margins

## License

Specify your license here (e.g., MIT, Apache 2.0)
