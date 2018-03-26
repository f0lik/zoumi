package integration.pages

import geb.Page

class ZArticlesPage extends Page {
    static url = 'http://localhost:8091/articles'
    static at = { title == "Články | Zoumi.cz" }
    static content = {
        heading { $("h1").text() }
        searchField { $("input[name=searchTextBox]") }
        dropdownSort {$("select#sortAttributeSelect")}
        dropdownSortSelectedText {dropdownSort.find('option', value:dropdownSort.value()).text()}
        dropdownPageSize {$("select#pageSizeSelect")}
        dropdownPageSizeSelectedText {dropdownPageSize.find('option', value:dropdownPageSize.value()).text()}
        dropdownSortDirection {$("select#sortDirection")}
        dropdownSortDirectionSelectedText {dropdownSortDirection.find('option', value:dropdownSortDirection.value()).text()}
    }
}
