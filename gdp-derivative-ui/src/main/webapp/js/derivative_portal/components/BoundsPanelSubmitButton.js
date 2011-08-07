Ext.ns("GDP");

GDP.BoundsPanelSubmitButton = Ext.extend(Ext.Button, {
    layerController : undefined,
    textBoxes : undefined,
    validator : undefined,
    setTextBoxes : function(args) {
        this.textBoxes.westBox = args.westBox;
        this.textBoxes.southBox = args.southBox;
        this.textBoxes.eastBox = args.eastBox;
        this.textBoxes.northBox = args.northBox;
    },
    constructor : function(config) {
        LOG.debug('BoundsPanelSubmitButton:constructor: Constructing self.');
        
        if (!config) config = {};
        this.layerController = config.layerController;
        this.textBoxes = config.textBoxes || {};
        this.validator = config.validator || true;
        config = Ext.apply({
            id : 'boundspanel-submit-button',
            text : 'Submit Bounds'
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('BoundsPanelSubmitButton:constructor: Construction complete.');
    },
    onClick : function(button, eventObj) {
        LOG.debug('BoundsPanelSubmitButton:click: Handling Request.');
        var north = this.textBoxes.northBox.getValue();
        var south = this.textBoxes.eastBox.getValue();
        var east = this.textBoxes.eastBox.getValue()
        var west = this.textBoxes.westBox.getValue()
        
        LOG.debug('West: ' + west);
        LOG.debug('South: ' + south);
        LOG.debug('East: ' + east);
        LOG.debug('North: ' + north);
        if (this.validator({
            northBox : this.textBoxes.northBox,
            southBox : this.textBoxes.southBox, 
            eastBox : this.textBoxes.eastBox,
            westBox : this.textBoxes.westBox
        })) {
            LOG.debug('BoundsPanelSubmitButton:click:validator returned true')
        } else {
            LOG.debug('BoundsPanelSubmitButton:click:validator returned false')
        }
        return;
        
        // Do bounding box validation
        if (!west) {
            this.textBoxes.westBox
        }
        
        // Create XML to send to WPS backing process
        var xmlData;
        
        // Send the AJAX request and do success/fail handling. (We probably want to pass the 'success' function in?)
        Ext.Ajax.request({
            url : 'proxy/' ,
            method: 'POST',
            xmlData : xmlData,
            success: function ( result, request ) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:success: ' + result);
            },
            failure: function ( result, request) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:failure: ' + result);
            }
        });
 
    }
});