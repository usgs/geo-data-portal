var SB = function () {
    var _SB_SEARCH_TEXT = '#sbSearch'
    return {
        searchSB: function() {
            var query = $(_SB_SEARCH_TEXT).val();
            CSWClient.setCSWHost('http://my-beta.usgs.gov/geoportal/csw');
            document.theForm.query.value = query;
            CSWClient.getRecords();
        }
    }
}