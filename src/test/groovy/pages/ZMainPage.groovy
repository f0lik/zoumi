package pages
import geb.Page

class ZMainPage extends Page {
    static url = 'http://localhost:8090/'
    static at = {title == "Home | Zoumi.cz"}
    static content = {
    }
}
