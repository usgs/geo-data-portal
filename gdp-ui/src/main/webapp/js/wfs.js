WFS = function() {
    var _getCapabilities;
    
    function _callWFS(data, async, successCallback) {
        var defaultData = {
            'service': 'WFS',
            'version': '1.1.0'
        }

        var wfsData = {};

        // Merge defaultData with data, putting results in wfsData. If there are
        // any conflicts, the property from data will overwrite the one in defaultData.
        $.extend(wfsData, defaultData, data);
        logger.debug('GDP: Calling WFS Service with a '+wfsData.request+' request.');
        $.ajax({
            url: Constant.endpoint.proxy + Constant.endpoint.wfs,
            async: async,
            data: wfsData,
            cache: false,
            success: function(data, textStatus, jqXHR) {
                if ('GetCapabilities' == wfsData.request) {
                    WFS.cachedGetCapabilities = data;
                }
                successCallback(data);
            }
        });
    }
    
    return {
        callWFS: _callWFS,
        cachedGetCapabilities: _getCapabilities
    }
};


