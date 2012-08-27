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
            if (!$.isEmptyObject(this.endpoints)) {
                this.useSB = true;
            }
            
            // By this point, the ScienceBase object has initialized and 
            // may have incoming parameters. Use those to set our params 
            // here.
            $.each(ScienceBase.endpoints, function(key, value) {
                if (key === 'feature_wms') {
                    Constant.endpoint.wms = value;
                }
                
                if (key === 'feature_wfs') {
                    Constant.endpoint.wfs = value;
                }
                
                if (key === 'redirect_url') {
                    Constant.endpoint['redirect_url'] = value;
                }
                
                if (key === 'coverage_wcs' && value) {
                    Constant.ui.default_dataset_url = value
                } else if (key === 'coverage_opendap' && value) {
                    Constant.ui.default_dataset_url = value
                }
            })
        },
        searchSB: function() {
            var oldVal = document.theForm.query.value;
            var query = $(_SB_SEARCH_TEXT).val();
            var sbEndpoint = Constant.endpoint['sciencebase-csw'];
            
            
            document.theForm.query.value = query;
            
            CSWClient.currentSBFeatureSearch = document.theForm.query.value;
            
            CSWClient.setCSWHost(sbEndpoint);
            CSWClient.setContext('wfs');
            CSWClient.getRecords();
            
            document.theForm.query.value = oldVal;
            
            $(_SB_FEATURE_BUTTON).trigger('click');
        }
    }
}
