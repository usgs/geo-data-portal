Ext.ns("GDP");

GDP.LayerChooser = Ext.extend(Ext.form.FormPanel, {
	controller : undefined,
	constructor : function(config) {
		if (!config) config = {};
		
		this.controller = config.controller || new GDP.LayerController({});
		
		
		
		var baseLayerStore = config.baseLayerStore;
		
		var baseLayerCombo = new Ext.form.ComboBox({
			xtype : 'combo',
			mode : 'local',
			triggerAction: 'all',
			store : baseLayerStore,
			fieldLabel : 'Base Layer',
			forceSelection : true,
			lazyInit : false,
			displayField : 'title'
		});
		
		baseLayerCombo.on('select', function(combo, record, index) {
			this.controller.requestBaseLayer(record);
		}, this);
		
		
		
		var capabilitiesStore = config.capabilitiesStore;
		
		var layerCombo = new Ext.form.ComboBox({
			xtype : 'combo',
			mode : 'local',
			triggerAction: 'all',
			store : capabilitiesStore,
			fieldLabel : 'Layer',
			forceSelection : true,
			lazyInit : false,
			displayField : 'title'
		});
		
		layerCombo.on('select', function(combo, record, index) {
			this.controller.requestLayer(record);
		}, this);
	
		capabilitiesStore.on('load', function(capStore, records) {
			var firstIndex = 0;
			var firstRecord = capStore.getAt(firstIndex);
			layerCombo.setValue(firstRecord.get("title"));
			layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
		});
		
		
		var thresholdName = 'elevation';
		var thresholdStore = new Ext.data.ArrayStore({
			storeId : 'thresholdStore',
			idIndex: 0,
			fields: [thresholdName]
		});
		
		var thresholdCombo = new Ext.form.ComboBox({
			xtype : 'combo',
			mode : 'local',
			triggerAction: 'all',
			store : thresholdStore,
			fieldLabel : 'Tmin threshold',
			forceSelection : true,
			lazyInit : false,
			displayField : thresholdName
		});
		thresholdCombo.on('select', function(combo, record, index) {
			this.controller.requestDimension(thresholdName, record.get(thresholdName));
		}, this);
		
		config = Ext.apply({
			items : [
			baseLayerCombo,
			layerCombo,
			thresholdCombo
			]
		}, config);
		
		this.controller.on('changelayer', function() {
			var baseLayer = this.controller.getBaseLayer();
			if (baseLayer) {
				baseLayerCombo.setValue(baseLayer.getLayer().name);
			}
			
			var layer = this.controller.getLayer();
			if (layer) {
				layerCombo.setValue(layer.getLayer().name);
			}
			
			this.controller.loadDimensionStore(layer, thresholdStore, thresholdName);
			
			var threshold = this.controller.getDimension(thresholdName);
			if (threshold) {
				thresholdCombo.setValue(threshold);
			}
			
		}, this);
		
		this.controller.on('changeDimension', function() {
			var threshold = this.controller.getDimension(thresholdName);
			if (threshold) {
				thresholdCombo.setValue(threshold);
			}
		}, this);
		
		GDP.LayerChooser.superclass.constructor.call(this, config);
	}
});