package Pojo;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.lang.reflect.Method;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BaseTest {

    protected static ExtentReports extent;
    protected static ExtentTest test;
    protected WebDriver driver;

    @BeforeSuite
    public void setupSuite() {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("target/extent-report/index.html");
        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
    }

    @BeforeMethod
    public void setup(Method method) {
        test = extent.createTest(method.getName());
        // Initialize your WebDriver here (chrome/firefox)
        // driver = new ChromeDriver(); for example
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                test.fail(result.getThrowable());

                // Take screenshot
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String path = "target/extent-report/screenshots/" + result.getName() + ".png";
                Files.createDirectories(Paths.get("target/extent-report/screenshots/"));
                Files.copy(src.toPath(), Paths.get(path));
                test.addScreenCaptureFromPath(path);
            } else if (result.getStatus() == ITestResult.SUCCESS) {
                test.pass("Test passed");
            } else if (result.getStatus() == ITestResult.SKIP) {
                test.skip("Test skipped");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (driver != null) {
            driver.quit();
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        extent.flush();
    }
}
