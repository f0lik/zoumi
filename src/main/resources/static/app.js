$(document).ready(function() {
    changePageAndSize();
    changePageAndSizeAndSort();
    changeSortDirection();
});

function changePageAndSize() {
    $('#pageSizeSelect').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + this.value + "&page=1");
    });
}

function changePageAndSizeAndSort() {
    $('#sortAttributeSelect').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + document.getElementById("pageSizeSelect").value +
            "&page="+ findGetParameter("page")
            + "&sortAttribute=" + this.value);
    });
}

function changeSortDirection() {
    $('#sortDirection').change(function(evt) {
        window.location.replace("/articles/?pageSize=" + document.getElementById("pageSizeSelect").value +
            "&page=" +findGetParameter("page") + "&sortAttribute=" + document.getElementById("sortAttributeSelect").value +
            "&sortDirection=" + this.value);
    });
}

function findGetParameter(parameterName) {
    var result = 1,
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