var ScienceBase = function () {
    var _SB_SEARCH_TEXT = '#sbSearch';
    var _SB_FEATURE_BUTTON = '#sbFeatureButton';
    var _SB_ENDPOINTS = {};
    var _USE_SB = false;
    return {
        endpoints : _SB_ENDPOINTS,
        useSB : _USE_SB,
        init : function() {
            this.endpoints = incomingEndpoints;
            if (!$.isEmptyObject(incomingEndpoints)) {
                _USE_SB = true;
            }
        },
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
