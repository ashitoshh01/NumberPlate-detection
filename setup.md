# TrafficEnforce: Number Plate Detection Setup Guide

This application is a JavaFX-based Traffic Enforcement System that uses Python for AI-powered License Plate Recognition.

## 🛠 Prerequisites

Ensure you have the following installed on your system:

### 1. Java Development Kit (JDK)
- Recommended: **JDK 17 or later**.
- You must have the [JavaFX SDK](https://openjfx.io/) installed if your JDK does not include it.

### 2. Python 3
- Recommended: **Python 3.10+**.
- Ensure `python3-venv` is installed:
  ```bash
  sudo apt update
  sudo apt install python3-venv python3-pip
  ```

### 3. Tesseract OCR
The AI extraction script relies on the Tesseract OCR engine.
- **Linux**:
  ```bash
  sudo apt install tesseract-ocr
  ```
- **Windows**: Download and install the binary from [here](https://github.com/UB-Mannheim/tesseract/wiki).
- **macOS**: `brew install tesseract`

---

## 🚀 Installation & Setup

### Step 1: Configure Python Environment
Navigate to the `Extract_AI` directory and set up the virtual environment:

1. **Open a terminal** and go to the project root.
2. **Create and activate the virtual environment**:
   ```bash
   cd Extract_AI
   python3 -m venv venv
   source venv/bin/activate
   ```
3. **Install dependencies**:
   ```bash
   pip install opencv-python pytesseract
   ```
4. **Deactivate** when done:
   ```bash
   deactivate
   ```

### Step 2: Configure Java Database Path (Optional but Recommended)
The application currently looks for a database folder at a hardcoded path. Open `src/FXMini/ViolationDataService.java` and ensure the `DATA_DIRECTORY_PATH` points to the `fastDatabase` folder inside this project.

### Step 3: Compiling the Java Application
If you are using an IDE (like VS Code or Eclipse), simply open the project and ensure JavaFX libraries are added to your classpath.

To compile manually:
```bash
# From the project root
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls -d bin src/FXMini/*.java
```

---

## 🏃 How to Run

### Option A: Using an IDE (Recommended)
1. Import the project into your IDE.
2. Add JavaFX to the Library/Classpath.
3. Add the following VM arguments if required:
   ```
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```
4. Run `FXMini.index`.

### Option B: Using Command Line
```bash
# From the project root
java -cp "bin:resources" --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml FXMini.index
```

---

## 📂 Project Structure
- `src/FXMini`: Java source code (Management UI).
- `Extract_AI`: Python logic for OCR and Plate Detection.
- `fastDatabase`: Local data storage for violations.
- `resources`: CSS styles and UI assets.

## ⚠️ Troubleshooting
- **Python Error**: Ensure the path in `DetectViolationView.java` (line 104) matches your virtual environment path.
- **Tesseract Error**: If Tesseract is not in your system PATH, you may need to explicitly define its path in `Extract_AI/extractor.py` using `pytesseract.pytesseract.tesseract_cmd = r'/usr/bin/tesseract'`.
