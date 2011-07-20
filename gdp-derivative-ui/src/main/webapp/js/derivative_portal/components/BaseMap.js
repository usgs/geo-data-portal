Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerOpacity : 0.4,
	legendWindow : undefined,
	realignLegend : function() {
		var DEFAULT_LEGEND_X = 110;
		var DEFAULT_LEGEND_Y = 264;
		this.legendWindow.alignTo(this.getEl(), "br-br", [-DEFAULT_LEGEND_X,-DEFAULT_LEGEND_Y]); 
	},
	constructor : function(config) {
		if (!config) config = {};
		
		var map = new OpenLayers.Map();
    
		var baseLayer = new OpenLayers.Layer.WMS(
			"Global Imagery",
			"http://maps.opengeo.org/geowebcache/service/wms",
			{
				layers: "bluemarble"
			}
			);

		map.addLayers([baseLayer]);
		
		config = Ext.apply({
			map: map,
			center: new OpenLayers.LonLat(-96, 38),
			zoom : 4
		}, config);
		
		GDP.BaseMap.superclass.constructor.call(this, config);
		
		var legendPanel = new GeoExt.LegendPanel({
			defaults: {
				style: 'padding: 0 5px 5px 5px'
			},
			border : false,
			layerStore: this.layers,
			filter : function(record) {
				return !(record.getLayer().isBaseLayer);
			},
			autoScroll: true
		});
    
		this.legendWindow = new Ext.Window({
			border : false,
			frame : false,
			closable: false,
			resizable : false,
			items : [legendPanel]
		});
		
		this.on('afterlayout', function() {
			LOG.debug('mapPanel afterlayout hit');
			this.legendWindow.show(null, function() {
				LOG.info('Legend window show callback hit');
				this.realignLegend();
			}, this);
		}, this);
	},
	zoomToExtent : function(record) {
		this.map.zoomToExtent(
			OpenLayers.Bounds.fromArray(record.get("llbbox"))
			);
	},
	replaceLayer : function(record, params) {
		var copy = record.clone();
		
		if (!params) {
			params = {};
		}
		
		params = Ext.apply({
			format: "image/png",
			transparent : true
		}, params);
		
		copy.get('layer').mergeNewParams(params);
		copy.get('layer')['opacity'] = this.layerOpacity;
		
		this.layers.removeAt(1);
		this.layers.add(copy);
		this.zoomToExtent(copy);
		this.realignLegend();
	}
});