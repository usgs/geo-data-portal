var SB = function () {
    var _SB_SEARCH_TEXT = '#sbSearch';
    var _SB_FEATURE_BUTTON = '#sbFeatureButton';
    return {
        searchSB: function() {
            var oldVal = document.theForm.query.value;
            var query = $(_SB_SEARCH_TEXT).val();
            CSWClient.setCSWHost('http://my-beta.usgs.gov/geoportal/csw');
            CSWClient.setSBConstraint("wfs");
            CSWClient.setStoredCSWServer(CSWClient.getCSWHost());
            document.theForm.query.value = query;
            
            CSWClient.setCSWHost('http://my-beta.usgs.gov/geoportal/csw');
            
            CSWClient.currentSBFeatureSearch = document.theForm.query.value;
            
            CSWClient.getRecords();
            document.theForm.query.value = oldVal;
            $(_SB_FEATURE_BUTTON).trigger('click');
        }
    }
}
