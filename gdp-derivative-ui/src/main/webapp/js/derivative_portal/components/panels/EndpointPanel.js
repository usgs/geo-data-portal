Ext.ns("GDP");

/**
 * This panel is a holder for the endpoint combobox that appears above the basemap
 */
GDP.EndpointPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    endpointApplyButton : undefined,
    endpointCombo : undefined,
    constructor : function(config) {
        LOG.debug('EndpointPanel:constructor: Constructing self.');

        this.controller = config.controller;

        var endpointUrls, endpointStore, endpointApplyButton, endpointContainer, endpointPanel;
        endpointUrls = config.endpointUrls;
        
        endpointStore = new Ext.data.ArrayStore({
            storeId : 'endpointStore',
            idIndex: 0,
            fields: ['url']
        });
        endpointStore.loadData(endpointUrls);
        this.endpointCombo = new Ext.form.ComboBox({
            mode : 'local'
            ,triggerAction: 'all'
            ,flex : 1
            ,store : endpointStore
            ,value : endpointUrls[0][0]
            ,lazyInit : false
            ,displayField : 'url'
            
        });
        endpointApplyButton = new Ext.Button({
            text : 'Go'
        });
        this.endpointApplyButton = endpointApplyButton;
        
        this.endpointCombo.on('select', function(){
            endpointApplyButton.fireEvent('click');
        });
        
        endpointContainer = new Ext.form.CompositeField({
            region : 'center'
            ,fieldLabel : 'Endpoint'
            ,items : [
                this.endpointCombo,
                endpointApplyButton
            ]
        });

        config = Ext.apply({
            id : 'endpoint-panel',
            border : false,
            collapsed : true,
            collapsible : true,
            floatable : false,
            hideCollapseTool : true,
            collapseMode : 'mini',
            split : true,
            height : 25,
            minSize : 25,
            maxSize : 25,
            items : [endpointContainer],
            layout : 'border'
        }, config);
        
        GDP.EndpointPanel.superclass.constructor.call(this, config);
        LOG.debug('EndpointPanel:constructor: Construction complete.');
        
        LOG.debug('EndpointPanel:constructor: Registering listeners.');
        this.controller.on('exception-capstore', function() {
            LOG.debug('EndpointPanel: Observed "exception-capstore"');
            this.expand();
//            this.endpointCombo
        }, this);
        endpointApplyButton.on('click', function() {
            LOG.debug('EVENT: User has clicked on the endpoint apply button');
            var endpoint = this.endpointCombo.getRawValue();
            if (endpoint) {
                LOG.debug('EVENT: Adding ' + endpoint + ' to the capabilities store');
                if (LOADMASK) LOADMASK.show();
                var proxyUrl = '';
                
                proxyUrl = GDP.PROXY_PREFIX + endpoint;
                
                this.controller.selectedDataset(proxyUrl);
                
                this.capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, proxyUrl);
                this.capabilitiesStore.load();
            }
        }, this);
    }
});