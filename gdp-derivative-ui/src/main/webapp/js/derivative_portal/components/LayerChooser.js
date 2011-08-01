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
		
		
		var zlayerName = 'elevation';
		var zlayerStore = new Ext.data.ArrayStore({
			storeId : 'zlayerStore',
			idIndex: 0,
			fields: [zlayerName]
		});
		
		var zlayerComboConfig = {
			mode : 'local',
			triggerAction: 'all',
			store : zlayerStore,
			forceSelection : true,
			lazyInit : false,
			displayField : zlayerName
		};
		
		var zlayerCombo = undefined;
		
		var layerOpacitySlider = new Ext.Slider({
			value : 40,
			fieldLabel: "Opacity",
			plugins: new GeoExt.LayerOpacitySliderTip({template: '<div>Opacity: {opacity}%</div>'})
		});
		
		layerOpacitySlider.on('change', function() {
			this.controller.requestOpacity(layerOpacitySlider.getValue() / 100);
		}, this, {buffer: 5});
                
		config = Ext.apply({
			items : [
			baseLayerCombo
			,layerCombo
			,layerOpacitySlider
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
			
			if (zlayerCombo) {
				this.remove(zlayerCombo)
			}
			var loaded = this.controller.loadDimensionStore(layer, zlayerStore, zlayerName);
			
			if (loaded) {
				var threshold = this.controller.getDimension(zlayerName);
				if (threshold) {
					zlayerCombo = new Ext.form.ComboBox(Ext.apply({
						fieldLabel : this.controller.getZAxisName()
					}, zlayerComboConfig));
					this.add(zlayerCombo);
					zlayerCombo.setValue(threshold);
					zlayerCombo.on('select', function(combo, record, index) {
						this.controller.requestDimension(zlayerName, record.get(zlayerName));
					}, this);
					this.doLayout();
				}
			}
			
		}, this);
		
		this.controller.on('changedimension', function() {
			var threshold = this.controller.getDimension(zlayerName);
			if (threshold & zlayerCombo) {
				zlayerCombo.setValue(threshold);
			}
		}, this);
		
		this.controller.on('changeopacity', function() {
			var opacity = this.controller.getLayerOpacity();
			
		}, this);
		
		GDP.LayerChooser.superclass.constructor.call(this, config);
	}
});