$(document).ready(function() {
    changePageAndSize();
    changePageAndSizeAndSort();
    changeSortDirection();
    clickSeach();
});

function changePageAndSize() {
    $('#pageSizeSelect').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + this.value + "&page=1");
    });
}

function clickSeach() {
    $('#searchButton').click(function(evt) {
        window.location.replace("/articles/?search=" + document.getElementById("searchTextBox").value);
    });
}

function changePageAndSizeAndSort() {
    $('#sortAttributeSelect').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + document.getElementById("pageSizeSelect").value +
            "&page="+ findGetParameter("page", 1)
            + "&sortAttribute=" + this.value);
    });
}

function changeSortDirection() {
    $('#sortDirection').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + document.getElementById("pageSizeSelect").value +
            "&page=" + findGetParameter("page", 1) + "&sortAttribute=" + document.getElementById("sortAttributeSelect").value +
            "&sortDirection=" + this.value);
    });
}

function findGetParameter(parameterName, defaultValue) {
    var result = defaultValue,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}