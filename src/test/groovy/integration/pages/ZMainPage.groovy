package integration.pages

import geb.Page

class ZMainPage extends Page {
    static url = 'http://localhost:8091/'
    static at = { title == "Home | Zoumi.cz" }
    static content = {
        heading { $("h1").text() }
    }
}
