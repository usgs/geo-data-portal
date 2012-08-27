var ScienceBase = function () {
    var _SB_SEARCH_TEXT = '#sbSearch';
    var _SB_FEATURE_BUTTON = '#sbFeatureButton';
    var _SB_ENDPOINTS = {};
    var _USE_SB = false;
    var _ITEM_ID;
    return {
        endpoints : _SB_ENDPOINTS,
        useSB : _USE_SB,
        itemId : _ITEM_ID,
        init : function() {
            this.endpoints = incomingParams;
            if (!$.isEmptyObject(this.endpoints)) {
                this.useSB = true;
            }
            
            this.itemId = incomingParams['item_id'] || '';
            
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
                    Constant.ui.default_wms_url = ScienceBase.endpoints['coverage_wms'];
                } else if (key === 'coverage_opendap' && value) {
                    Constant.ui.default_dataset_url = value
                    Constant.ui.default_wms_url = ScienceBase.endpoints['coverage_wms'];
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
