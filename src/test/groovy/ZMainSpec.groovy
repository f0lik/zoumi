import geb.spock.GebSpec
import pages.ZMainPage

class ZMainSpec extends GebSpec {

    def setup() {

        to ZMainPage

    }

    def "page contains h1 with specified text"() {
        given:
        at ZMainPage
        assert $("h1")*.text() == ["Welcome home, boi."]
    }
}
