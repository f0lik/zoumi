import io.github.bonigarcia.wdm.ChromeDriverManager
import org.openqa.selenium.chrome.ChromeDriver

baseUrl = 'http://localhost:8090/'
ChromeDriverManager.getInstance().setup()
timeout = 15
driver = {
    def driver = new ChromeDriver()
    println "driver"
    driver.manage().window().maximize()
    driver
}