import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import org.testng.Assert;
import org.testng.annotations.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.logging.*;

public class QuizTest {

    WebDriver driver;
    WebDriverWait wait;
    Logger logger;

    @BeforeTest
    public void setup() throws Exception {

        // ---------- Logger Setup ----------
        logger = Logger.getLogger("QuizAutomationLogger");
        logger.setUseParentHandlers(false);

        new File("logs").mkdirs();
        FileHandler fh = new FileHandler("logs/quiz-execution.log");
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);

        logger.info("==== TEST EXECUTION STARTED ====");

        // ---------- WebDriver Setup ----------
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
    }

    @Test
    public void automateQuizApplication() throws Exception {

        openLandingPage();     // Category page
        selectCategory();
        selectDifficulty();
        answerAllQuestions();
        verifyResultPage();
    }

    /* ===================== STEP 1: LANDING PAGE ===================== */
    void openLandingPage() throws Exception {

        logger.info("Opening Landing Page (Category Selection)");
        driver.get("http://localhost/frugal/index.html");

        wait.until(ExpectedConditions.titleContains("Category"));

        logger.info("Page Loaded | URL: " + driver.getCurrentUrl());
        logger.info("Page Title: " + driver.getTitle());

        takeScreenshot("01_Landing_Category");
    }

    /* ===================== STEP 2: CATEGORY ===================== */
    void selectCategory() throws Exception {

        logger.info("Selecting Category");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".category-card"))).click();

        wait.until(ExpectedConditions.urlContains("difficulty"));

        logger.info("Category Selected | URL: " + driver.getCurrentUrl());
        takeScreenshot("02_Category_Selected");
    }

    /* ===================== STEP 3: DIFFICULTY ===================== */
    void selectDifficulty() throws Exception {

        logger.info("Selecting Difficulty");

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".difficulty-card"))).click();

        wait.until(ExpectedConditions.urlContains("quiz"));

        logger.info("Difficulty Selected | Quiz Started");
        takeScreenshot("03_Difficulty_Selected");
    }

    /* ===================== STEP 4: ANSWER QUESTIONS ===================== */
    void answerAllQuestions() throws Exception {

        for (int i = 1; i <= 10; i++) {
            answerSingleQuestion(i);
        }
    }

    /* ===================== SINGLE QUESTION HANDLER ===================== */
    void answerSingleQuestion(int index) throws Exception {

        // Fetch question text freshly
        WebElement questionEl = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("question")));

        String questionText = questionEl.getText();
        logger.info("Question " + index + ": " + questionText);

        // Fetch options freshly every time (NO STALE)
        List<WebElement> options = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("input[name='option']")));

        // Select first option
        options.get(0).click();
        logger.info("Selected first option");

        takeScreenshot("Q" + index);

        // Click NEXT (fresh element)
        WebElement nextBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.className("next-btn")));
        nextBtn.click();

        // Wait until question changes OR result page loads
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("result"),
                ExpectedConditions.not(
                        ExpectedConditions.textToBe(By.id("question"), questionText))
        ));
    }

    /* ===================== STEP 5: RESULT PAGE ===================== */
    void verifyResultPage() throws Exception {

        logger.info("Verifying Result Page");

        wait.until(ExpectedConditions.urlContains("result"));

        WebElement score = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("score")));

        Assert.assertTrue(score.isDisplayed(), "Score not visible");

        logger.info("Result Page Displayed | Score Verified");
        takeScreenshot("Final_Result");
    }

    /* ===================== SCREENSHOT METHOD ===================== */
    void takeScreenshot(String name) throws Exception {

        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File("screenshots/" + name + ".png"));
        logger.info("Screenshot captured: " + name);
    }

    @AfterTest
    public void tearDown() {

        logger.info("==== TEST EXECUTION FINISHED ====");
        driver.quit();
    }
}
