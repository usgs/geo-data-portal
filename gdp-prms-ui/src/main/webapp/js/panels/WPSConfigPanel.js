Ext.ns("PRMS");

PRMS.WPSConfigPanel = Ext.extend(Ext.form.FormPanel, {
    constructor : function(config) {
        if (!config) config = {};

        config = Ext.apply({
            id: 'panel-wpsconfig',
            items: [],
            buttons: []
        }, config);
    PRMS.WPSConfigPanel.superclass.constructor.call(this, config);
    }
});