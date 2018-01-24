import io.github.bonigarcia.wdm.ChromeDriverManager
import org.openqa.selenium.htmlunit.HtmlUnitDriver

baseUrl = 'http://localhost:8090/'
ChromeDriverManager.getInstance().setup()
timeout = 15
driver = {
    def driver = new HtmlUnitDriver()

    driver.manage().window().maximize()
    driver
}