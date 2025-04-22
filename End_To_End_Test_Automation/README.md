# Amazon Castor Oil Search Test Automation Script

This document provides a guide to understanding and running the `TestAmazonCastorOilSearch.java` script. This script automates the process of searching for "castor oil" on Amazon, selecting the first result, adding it to the cart, and attempting to proceed to checkout. It also reads the search item and expected prices from an Excel file.

## Table of Contents

1.  **Overview**
2.  **Prerequisites**
3.  **Setup**
    * Driver Configuration
    * Test Data File
    * Screenshot Directory
4.  **Execution**
5.  **Code Walkthrough**
    * Imports
    * Class Declaration and Logger
    * Configuration Constants
    * Wait Times and Date Formatter
    * WebDriver and Test Data Variables
    * Constructor
    * `@BeforeTest` - `setup()`
    * `@Test` - `searchItemFromExcel()`
    * Private Methods:
        * `searchItem()`
        * `selectItem()`
        * `addToCart()`
        * `proceedToCheckout()`
        * `@AfterTest` - `tearDown()`
        * `setExcelFile()`
        * `readCell()`
        * `captureScreenshot()`
        * `assertPriceMatch()`
        * `executeTestStep()`
        * `cleanupDriver()`
        * `handleError()`
        * `TestStep` Interface
6.  **Logging**
7.  **Screenshots**
8.  **Error Handling**
9.  **TestNG Annotations**
10. **Improvements and Further Development**

## 1. Overview

The `TestAmazonCastorOilSearch` script is a Java-based automation test designed to simulate a user searching for "castor oil" on the Amazon website. It utilizes Selenium WebDriver to interact with the web browser and TestNG for test execution and reporting. The script also incorporates Apache POI to read test data (the item to search and expected prices) from an Excel file and Apache Commons IO for file operations like copying screenshots. Log4j is used for logging test execution details and potential errors.

## 2. Prerequisites

Before running the script, ensure you have the following installed and configured:

* **Java Development Kit (JDK):** Make sure you have a compatible JDK installed on your system.
* **Maven or Gradle:** This project likely uses Maven or Gradle for dependency management. Ensure you have one of these build tools installed.
* **Firefox Browser:** The script is configured to use the Firefox browser. Please ensure it is installed on your machine.
* **GeckoDriver:** GeckoDriver is a proxy for interacting with Gecko-based browsers (like Firefox). You need to download the appropriate GeckoDriver executable for your operating system and specify its path in the `DRIVER_PATH` constant within the script.
* **Apache POI Library:** This library is used for reading data from the Excel file. Ensure it is included as a dependency in your project's build file (`pom.xml` for Maven, `build.gradle` for Gradle).
* **Selenium WebDriver Library:** This library is essential for browser automation. Ensure it is included as a dependency in your project's build file.
* **TestNG Library:** This framework is used for running the test. Ensure it is included as a dependency.
* **Apache Commons IO Library:** This library is used for file utilities, specifically for taking screenshots. Ensure it is included as a dependency.
* **Apache Log4j 2 Library:** This library is used for logging. Ensure it is included as a dependency.
* **Test Data Excel File (`castor.xlsx`):** You need an Excel file named `castor.xlsx` located at `D:\\QA\\`. This file should contain a sheet named "Sheet1". The item to search should be in the first cell (column 0) of the second row (row index 1), and the expected prices should be in the subsequent three cells (columns 1, 2, and 3) of the same row.

## 3. Setup

### Driver Configuration

1.  **Download GeckoDriver:** Go to the official Mozilla GeckoDriver releases page ([https://github.com/mozilla/geckodriver/releases](https://github.com/mozilla/geckodriver/releases)) and download the appropriate version for your operating system.
2.  **Extract GeckoDriver:** Extract the downloaded executable file (e.g., `geckodriver.exe`).
3.  **Update `DRIVER_PATH`:** Modify the `DRIVER_PATH` constant in the `TestAmazonCastorOilSearch.java` file to the absolute path of the extracted GeckoDriver executable on your system.

    ```java
    private static final String DRIVER_PATH = "C:\\path\\to\\your\\geckodriver.exe";
    ```

### Test Data File

1.  **Create `castor.xlsx`:** Create an Excel file named `castor.xlsx` at the location specified by the `TEST_DATA_FILE` constant (`D:\\QA\\`).
2.  **Add Data to "Sheet1":** Open the Excel file and navigate to the sheet named "Sheet1".
3.  **Enter Test Item and Expected Prices:** In the second row (row index 1):
    * In the first cell (column 0), enter the item to search for (e.g., `castor oil`).
    * In the subsequent three cells (columns 1, 2, and 3), enter the expected prices you want to assert against (e.g., `10`, `12`, `15`).

    | Column A      | Column B | Column C | Column D |
    | ------------- | -------- | -------- | -------- |
    | (Header - Optional) | Price 1 | Price 2 | Price 3 |
    | castor oil    | 10       | 12       | 15       |

### Screenshot Directory

1.  **Create `screenshot` Directory:** Ensure that a directory named `screenshot` exists at the location specified by the `SCREENSHOT_PATH` constant (`D:\\QA\\`). The script will save screenshots of the test execution steps in this directory.

    ```java
    private static final String SCREENSHOT_PATH = "D:\\QA\\screenshot";
    ```

## 4. Execution

To run the test script:

1.  **Navigate to Project Directory:** Open your terminal or command prompt and navigate to the root directory of your Java project containing the `TestAmazonCastorOilSearch.java` file and your project's build configuration file (e.g., `pom.xml` or `build.gradle`).
2.  **Execute TestNG:**
    * **Using Maven:** If you are using Maven, run the following command:
        ```bash
        mvn test
        ```
    * **Using Gradle:** If you are using Gradle, run the following command:
        ```bash
        gradle test
        ```
    * **Using an IDE (IntelliJ, Eclipse, etc.):** You can also run the TestNG test directly from your Integrated Development Environment (IDE) by right-clicking on the `@Test` method or the class and selecting "Run As TestNG Test".

The script will then:

* Initialize the Firefox browser.
* Navigate to `http://www.amazon.com/`.
* Read the search item from the `castor.xlsx` file.
* Search for the item on Amazon.
* Select the first search result.
* Add the item to the cart.
* Attempt to proceed to checkout.
* Capture screenshots of key steps.
* Log the execution flow and any errors.
* Assert that the price of the first search result matches one of the expected prices from the Excel file.
* Close the browser after the test is complete.

## 5. Code Walkthrough

### Imports

The script imports various classes from different libraries to facilitate its functionality:

```
java
import org.apache.commons.io.FileUtils; // For file utilities (e.g., copying screenshots)
import org.apache.logging.log4j.Level;   // For setting logging levels
import org.apache.logging.log4j.LogManager; // For getting the logger instance
import org.apache.logging.log4j.Logger;   // For logging messages
import org.apache.logging.log4j.core.config.Configurator; // For programmatic logger configuration
import org.apache.logging.log4j.core.config.DefaultConfiguration; // For default logging configuration
import org.apache.poi.ss.usermodel.CellType; // For identifying cell data types in Excel
import org.apache.poi.xssf.usermodel.XSSFSheet; // For working with Excel sheets
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // For working with Excel workbooks
import org.openqa.selenium.*; // Core Selenium WebDriver classes
import org.openqa.selenium.firefox.FirefoxDriver; // For using the Firefox browser
import org.openqa.selenium.support.ui.ExpectedConditions; // For defining explicit wait conditions
import org.openqa.selenium.support.ui.Select; // For working with dropdown elements
import org.openqa.selenium.support.ui.WebDriverWait; // For implementing explicit waits
import org.openqa.selenium.JavascriptExecutor; // For executing JavaScript in the browser
import org.testng.Assert; // For hard assertions in TestNG
import org.testng.annotations.AfterTest; // TestNG annotation for methods to run after the test
import org.testng.annotations.BeforeTest; // TestNG annotation for methods to run before the test
import org.testng.annotations.Test; // TestNG annotation for test methods
import org.testng.asserts.SoftAssert; // For soft assertions in TestNG
import java.io.File; // For working with files
import java.io.FileInputStream; // For reading files
import java.time.Duration; // For specifying time durations
import java.time.LocalDateTime; // For getting the current date and time
import java.time.format.DateTimeFormatter; // For formatting date and time
import java.util.ArrayList; // For creating dynamic lists
import java.util.List; // For working with lists
```


## 6. Screenshots collected

<img width="944" alt="inputsearchItem_2024_12_12" src="https://github.com/user-attachments/assets/840ffabf-2b32-4f7f-a86f-5e323e448241" />


<img width="949" alt="searchItem_2024_12_12" src="https://github.com/user-attachments/assets/f5fd56b8-5576-40c1-8947-bcd985b8d5e8" />


![selectItem_2024_12_12](https://github.com/user-attachments/assets/166556ea-0568-4780-ba5f-21d980917f1a)


<img width="929" alt="clickaddToCart_2024_12_12" src="https://github.com/user-attachments/assets/3661459f-41f8-4cef-b9d9-06243ebb8231" />


![addToCart_2024_12_12](https://github.com/user-attachments/assets/c4ab4c3c-046b-4c84-9e37-78481533dcfc)


<img width="946" alt="clickproceedToCheckout_2024_12_12" src="https://github.com/user-attachments/assets/b21f6365-30af-4899-9d87-36909a5e5752" />


![proceedToCheckout_2024_12_12](https://github.com/user-attachments/assets/15843253-19c0-47a6-b866-0aac9709b2b5)


<img width="748" alt="RunResult" src="https://github.com/user-attachments/assets/319e695e-d33c-48b1-baf6-ed398285b5e7" />







