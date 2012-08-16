var SB = function () {
    var _SB_SEARCH_TEXT = '#sbSearch';
    var _SB_FEATURE_BUTTON = '#sbFeatureButton';
    return {
        searchSB: function() {
            var oldVal = document.theForm.query.value;
            var oldCSWServer = CSWClient.getCSWHost();
            var query = $(_SB_SEARCH_TEXT).val();
            
            document.theForm.query.value = query;
            
            CSWClient.setCSWHost('http://my-beta.usgs.gov/geoportal/csw');
            
            CSWClient.currentSBFeatureSearch = document.theForm.query.value;
            
            CSWClient.getRecordsFromScienceBase();
            
            $(_SB_FEATURE_BUTTON).trigger('click');
            
            CSWClient.setCSWHost(oldCSWServer);
            document.theForm.query.value = oldVal;
        }
    }
}