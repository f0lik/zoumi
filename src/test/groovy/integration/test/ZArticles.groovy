package integration.test

import geb.spock.GebSpec
import integration.pages.ZArticlesPage

class ZArticles extends GebSpec{

    def 'filtering contains sort by drop down and when changed, it does change value'() {
        given:
        to ZArticlesPage

        when: 'select Počet komentářů from dropdown...'
        dropdownSort = 'Počet komentářů'

        then:
        dropdownSortSelectedText == 'Počet komentářů'
        dropdownSort.value() == 'commentCount'
    }

    def 'filtering contains sorting direction drop down and when changed, it does change value'() {
        given:
        to ZArticlesPage

        when: 'select Vzestupně from dropdown...'
        dropdownSortDirection = 'Vzestupně'

        then:
        dropdownSortDirectionSelectedText == 'Vzestupně'
        dropdownSortDirection.value() == 'ASC'
    }

    def 'filtering contains page size drop down and when changed, it does change value'() {
        given:
        to ZArticlesPage

        when: 'select 10 from dropdown...'
        dropdownPageSize = '10'

        then:
        dropdownPageSizeSelectedText == '10'
        dropdownPageSize.value() == '10'
    }
}
