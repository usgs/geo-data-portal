Ext.ns("GDP");

GDP.PolygonPOIPanel = Ext.extend(Ext.Panel, {
    layerController : undefined,
    west : undefined,
    south : undefined, 
    east : undefined,
    north : undefined,
    setCoords : function(args) {
        LOG.debug('PolygonPOIPanel:setCoords');
        this.get('coord-text-panel').get('west-box').setValue(args.west.lon);
        this.get('coord-text-panel').get('south-box').setValue(args.south.lat);
        this.get('coord-text-panel').get('east-box').setValue(args.east.lon);
        this.get('coord-text-panel').get('north-box').setValue(args.north.lat);
    },
    getCoords : function() {
        LOG.debug('PolygonPOIPanel:getCoords');
        return {
            west : this.get('coord-text-panel').get('west-box').getValue(),
            south :  this.get('coord-text-panel').get('south-box').getValue(),
            east : this.get('coord-text-panel').get('east-box').getValue(),
            north : this.get('coord-text-panel').get('north-box').getValue()
        }
    },
    constructor : function(config) {
        LOG.debug('PolygonPOIPanel:constructor: Constructing self.');
        
        if (!config) config = {};
        this.layerController = config.layerController;
        this.west = config.west || '';
        this.south = config.south || '';
        this.east = config.east || '';
        this.north = config.north || '';
        
        var coordPanel; 
        var westboundLonTexbox = new Ext.form.TextField({
            id : 'west-box',
            fieldLabel : 'West Lon',
            value : this.west.lon,
            allowBlank : false,
            regEx : ' [-+]?[0-9]*\\.?[0-9]+',
            regexText : 'Must be float'
        });
        var northboundLatTextbox = new Ext.form.TextField({
            id : 'north-box',
            fieldLabel : 'North Lat',
            value : this.north.lat,
            allowBlank : false,
            regEx : ' [-+]?[0-9]*\\.?[0-9]+',
            regexText : 'Must be float'
            
        });
        var southboundLatTextbox = new Ext.form.TextField({
            id : 'south-box',
            fieldLabel : 'South Lat',
            value : this.south.lat,
            allowBlank : false,
            regEx : ' [-+]?[0-9]*\\.?[0-9]+',
            regexText : 'Must be float'
        });
        var eastboundLonTextBox = new Ext.form.TextField({
            id : 'east-box',
            fieldLabel : 'East Lon',
            value : this.east.lon,
            allowBlank : false,
            regEx : ' [-+]?[0-9]*\\.?[0-9]+',
            regexText : 'Must be float'
        });
        var coordDescPanel = new Ext.Panel({
            id : 'coord-desc-panel',
            region : 'north',
            border : false,
            html : 'Draw a bounding box on the map or enter coordinates here.'
        });
        
        coordPanel = new Ext.Panel({
            id : 'coord-text-panel',
            region : 'center',
            layout : 'form',
            title : 'Coordinates',
            border : false,
            items : [
                coordDescPanel,
                westboundLonTexbox, 
                southboundLatTextbox, 
                eastboundLonTextBox,
                northboundLatTextbox
            ]
        })
        
        var configItems = [coordPanel];
        var button = config.submitButton;
        
        // This validates the textbox values and returns a boolean.
        // The submit button will use this function when pressed.
        var validator = function(boxes) {
            var westBox = boxes.westBox;
            var southBox = boxes.southBox;
            var eastBox = boxes.eastBox;
            var northBox = boxes.northBox;
            LOG.debug('Validating input bounding boxes.');

            return function() {
                var result = true;
                var errorMsg = '';
                var nullValErr = '\nValidation: All Inputs Must Be Populated';
                var nanError = '\nValidation: All Inputs Must Be Numeric';
                
                // null validation
                Ext.each([northBox, westBox, southBox, eastBox], function(box, index){
                    if (!box.getValue()) {
                        LOG.debug(errorMsg);
                        errorMsg = nullValErr
                        result = false;
                    }
                });
                
                // NaN validation
                Ext.each([northBox, westBox, southBox, eastBox], function(box, index){
                    if (isNaN(box.getValue())) {
                        LOG.debug(errorMsg);
                        errorMsg += nanError
                        box.setValue('');
                        result = false;
                    }
                });
                
                if (parseFloat(southBox.getValue()) > parseFloat(northBox.getValue())) {
                    LOG.debug('Validation: North Box is always less than South Box');
                    errorMsg +='\nValidation: North Box is always less than South Box'
                    northBox.setValue('');
                    southBox.setValue('');
                    result = false;
                }
                if (parseFloat(westBox.getValue()) > parseFloat(eastBox.getValue())) {
                    LOG.debug('Validation: West Box is always less than East Box');
                    errorMsg += '\nValidation: West Box is always less than East Box';
                    westBox.setValue('');
                    eastBox.setValue('');
                    result = false;
                }
                if (!result) NOTIFY.warn(errorMsg);
                return result;
            }();
        }
                
        if (button) {
            button.validator = validator;
            button.setTextBoxes({
                westBox : westboundLonTexbox, 
                southBox : southboundLatTextbox, 
                northBox : northboundLatTextbox, 
                eastBox : eastboundLonTextBox
            })
            configItems.push(button);
        }
        
        config = Ext.apply({
            items : configItems
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('PolygonPOIPanel:constructor: Construction complete.');
    },
      initComponent : function() {
          GDP.PolygonPOIPanel.superclass.initComponent.call(this);
          this.addEvents();
    }
});