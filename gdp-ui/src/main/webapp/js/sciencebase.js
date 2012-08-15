var SB = function () {
    var _SB_SEARCH_TEXT = '#sbSearch';
    var _SB_FEATURE_BUTTON = '#sbFeatureButton';
    return {
        searchSB: function() {
            var old_val = document.theForm.query.value;
            var query = $(_SB_SEARCH_TEXT).val();
            CSWClient.setCSWHost('http://my-beta.usgs.gov/geoportal/csw');
            document.theForm.query.value = query;
            CSWClient.getRecords();
            document.theForm.query.value = old_val;
            $(_SB_FEATURE_BUTTON).trigger('click');
        }
    }
}