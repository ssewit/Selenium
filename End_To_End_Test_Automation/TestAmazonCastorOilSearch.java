import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TestAmazonCastor is an automated testing script for Amazon shopping workflow.
 * Updated with comprehensive implicit and explicit waits.
 */
public class TestAmazonCastorOilSearch {
    // Logger for capturing runtime information and errors
    private static final Logger logger = LogManager.getLogger(TestAmazonCastorOilSearch.class);

    // Configuration constants for file paths, URLs, and element identifiers
    private static final String DRIVER_PATH = "C:\\Users\\Blessed\\IdeaProjects\\SimpleAutomation\\drivers\\geckodriver.exe";
    private static final String SCREENSHOT_PATH = "D:\\QA\\screenshot";
    private static final String URL = "http://www.amazon.com/";
    private static final String TEST_DATA_FILE = "D:\\QA\\castor.xlsx";
    private static final String AMAZON_SEARCH_TEXTBOX_ID = "twotabsearchtextbox";
    private static final String AMAZON_SEARCH_BUTTON_ID = "nav-search-submit-button";

    // Wait times
    private static final Duration GLOBAL_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration STEP_WAIT = Duration.ofSeconds(15);

    // Date formatter for creating unique screenshot and log filenames
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd");

    // WebDriver and test data variables
    private WebDriver driver;
    private WebDriverWait wait;
    private String item;
    private List<String> expectedPrices;

    public TestAmazonCastorOilSearch() {
        // Initialize logging configuration
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.INFO);
    }

    @BeforeTest
    void setup() throws Exception {
        logger.info("Setting up the test...");

        // Set up Firefox WebDriver with implicit waits
        System.setProperty("webdriver.gecko.driver", DRIVER_PATH);
        driver = new FirefoxDriver();

        // Configure implicit and explicit waits
        driver.manage().timeouts().implicitlyWait(GLOBAL_TIMEOUT);
        wait = new WebDriverWait(driver, GLOBAL_TIMEOUT);

        // Maximize browser window
        driver.manage().window().maximize();

        try {
            // Navigate to Amazon and wait for page to load
            driver.get(URL);
            wait.until(ExpectedConditions.and(
                    ExpectedConditions.titleContains("Amazon.com"),
                    ExpectedConditions.presenceOfElementLocated(By.id(AMAZON_SEARCH_TEXTBOX_ID))
            ));

            // Read test data from Excel
            setExcelFile();
            logger.info("Test setup completed.");
        } catch (Exception e) {
            logger.error("Error during test setup", e);
            cleanupDriver();
            throw e;
        }
    }

    @Test(priority = 1)
    void searchItemFromExcel() throws Exception {
        // Get current date and time for screenshot naming
        String formattedDateTime = LocalDateTime.now().format(FORMATTER);

        // Execute test steps with 15-second waits
        executeTestStep("Search Item", (fd) -> searchItem(fd));
        executeTestStep("Select Item", (fd) -> selectItem(fd));
        executeTestStep("Add to Cart", (fd) -> addToCart(fd));
        executeTestStep("Proceed to Checkout", (fd) -> proceedToCheckout(fd));
    }

    private void searchItem(String formattedDateTime) throws Exception {
        // Wait for search textbox to be present and interactable
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id(AMAZON_SEARCH_TEXTBOX_ID)));
        searchBox.clear();
        searchBox.sendKeys(item);

        // Wait for and click search button
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(AMAZON_SEARCH_BUTTON_ID)));
        searchButton.click();

        // Wait for search results to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.a-link-normal.s-line-clamp-3")));

        // Scroll down to bypass top advertisements
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 100);");

        // Capture search results screenshot
        captureScreenshot("searchItem_" + formattedDateTime);

        // Wait for price element to be present
        WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span.a-price-whole")));
        String actualPrice = priceElement.getText();
        assertPriceMatch(actualPrice);
    }

    private void selectItem(String formattedDateTime) throws Exception {
        // Wait for and click first search result
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.a-link-normal.s-line-clamp-3")));
        firstResult.click();
        captureScreenshot("selectItem_" + formattedDateTime);

        // Wait for quantity dropdown and select value
        WebElement quantityDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("quantity")));
        new Select(quantityDropdown).selectByValue("1");
    }

    private void addToCart(String formattedDateTime) throws Exception {
        // Wait for and click 'Add to Cart' button
        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-button")));
        addToCartButton.click();

        // Capture screenshot of cart addition
        captureScreenshot("addToCart_" + formattedDateTime);
    }

    private void proceedToCheckout(String formattedDateTime) throws Exception {
        try {
            // Wait and click on the proceed to checkout button
            WebElement checkoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//input[@name='proceedToRetailCheckout']")
            ));
            checkoutButton.click();

            // Capture screenshot of checkout page
            captureScreenshot("proceedToCheckout_" + formattedDateTime);
            logger.info("Proceeded to checkout successfully.");
        } catch (Exception e) {
            // Handle and log any errors during checkout
            handleError("Proceed to Checkout", e, "proceedToCheckout_error_" + formattedDateTime);
        }
    }

    @AfterTest
    void tearDown() {
        logger.info("Tearing down the test...");
        cleanupDriver();
        logger.info("Test teardown completed.");
    }

    private void setExcelFile() throws Exception {
        try (FileInputStream file = new FileInputStream(TEST_DATA_FILE)) {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet("Sheet1");

            // Read item name and expected prices from first row
            item = readCell(sheet.getRow(1).getCell(0));
            expectedPrices = new ArrayList<>();
            for (int col = 1; col <= 3; col++) {
                expectedPrices.add(readCell(sheet.getRow(1).getCell(col)));
            }
        } catch (Exception e) {
            logger.error("Error reading Excel file", e);
            throw e;
        }
    }

    private String readCell(org.apache.poi.ss.usermodel.Cell cell) {
        return cell.getCellType() == CellType.NUMERIC
                ? String.valueOf((int) cell.getNumericCellValue())
                : cell.getStringCellValue();
    }

    private void captureScreenshot(String filename) throws Exception {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(srcFile, new File(SCREENSHOT_PATH + "//" + filename + ".png"));
    }

    private void assertPriceMatch(String actualPrice) {
        SoftAssert softAssert = new SoftAssert();
        boolean priceMatched = expectedPrices.stream().anyMatch(expectedPrice -> {
            try {
                softAssert.assertTrue(actualPrice.equals(expectedPrice));
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
        Assert.assertTrue(priceMatched, "No matching price found for the item");
    }

    /**
     * Modified executeTestStep method to include 15-second wait between steps
     */
    private void executeTestStep(String stepName, TestStep step) throws Exception {
        try {
            String formattedDateTime = LocalDateTime.now().format(FORMATTER);
            logger.info("Executing step: " + stepName);
            step.execute(formattedDateTime);
            logger.info("Completed step: " + stepName);

            // Add explicit wait between test steps
            Thread.sleep(STEP_WAIT.toMillis());
        } catch (Exception e) {
            logger.error("Error during step: " + stepName, e);
            throw e;
        }
    }

    @FunctionalInterface
    interface TestStep {
        void execute(String formattedDateTime) throws Exception;
    }

    private void cleanupDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void handleError(String step, Exception e, String screenshotName) throws Exception {
        logger.error("Error during " + step, e);
        captureScreenshot(screenshotName);
        throw e;
    }
}