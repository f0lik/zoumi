package integration.test

import geb.spock.GebSpec
import integration.pages.ZMainPage
import spock.lang.Stepwise

@Stepwise
class ZMain extends GebSpec {

    def "page contains h1 with specified text"() {
        given: "I'm at home page"
        to ZMainPage

        when: "Nothing"

        then: "Again home page"
        at ZMainPage
        heading == "Welcome home, boi."
    }
}
