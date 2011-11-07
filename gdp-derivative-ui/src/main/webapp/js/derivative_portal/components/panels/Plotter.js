Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    legendDiv : undefined,
    height : undefined,
    legendWidth : undefined,
    controller : undefined,
    gmlid : undefined,
    sosStore : undefined,
    dataArray : [],
    graph : undefined,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = config.plotterDiv || 'dygraph-content';
        this.legendDiv = config.legendDiv || 'dygraph-legend';
        this.legendWidth = config.legendWidth || 250;
        this.height = config.height || 200;
        this.gmlid = config.gmlid;
        //this.csv = config.csv;
            
        this.controller = config.controller;
        
        var contentPanel = new Ext.Panel({
            contentEl : this.plotterDiv,
            layout : 'fit',
            region : 'center',
            autoShow : true
        });
        var legendPanel = new Ext.Panel({
            contentEl : this.legendDiv,
            layout : 'fit', 
            region : 'east',
            autoShow : true
        });
        config = Ext.apply({
            items : [contentPanel, legendPanel],
            layout : 'border',
            autoShow : true
        }, config);
        
        GDP.Plotter.superclass.constructor.call(this, config);
        
        //this.loadSOSStore();
        
        this.on("afterrender", function () {
            this.resizePlotter();
        }, this);
        this.controller.on('updateplotter', function(args){
            this.updatePlotter(args);
        }, this),
        this.on('resize', function() {
            this.resizePlotter();
        }, this)
    },
    
    updatePlotter : function(args) {
        LOG.debug('Plotter:updatePlotter: Observed request to update plotter');
        this.gmlid = args.gmlid;
        this.loadSOSStore();
        //this.graph.updateOptions({
            //title : args.featureTitle,
            //file : this.dataArray,
            //dateWindow : null,
            //valueRange : null
        //});
    },
    resizePlotter : function() {
        var divPlotter = Ext.get(this.plotterDiv);
        var divLegend = Ext.get(this.legendDiv);
            
        divPlotter.setWidth(this.getWidth() - (this.legendWidth + 2));
        divLegend.setWidth(this.legendWidth);
        divPlotter.setHeight(this.height);
    },
    loadSOSStore : function() {
        this.sosStore = new GDP.SOSGetObservationStore({
            url : this.gmlid, // gmlid is url for now, eventually, use SOS endpoint + gmlid or whatever param
            autoLoad : true,
            opts : {
                offering: "test",
                observedProperty: "test"
            },
            listeners : {
                load : function(store) {
                    //this.dataArray = store.getAt(0).get('values');
                    var record = store.getAt(0);
                    if (this.graph) this.graph.destroy();
                    this.graph = new Dygraph(
                        Ext.get(this.plotterDiv).dom,
                        function(values) {
                            Ext.each(values, function(item, index, allItems) {
                                for(var i=0; i<item.length; i++) {
                                    var value;
                                    if (i==0) {value = new Date(item[i])}
                                    else {value = parseFloat(item[i])}
                                    allItems[index][i] = value;
                                }
                            });
                            return values;
                        }(record.get('values')),
                        {
                            //errorBars : true,
                            title : record.get('name'),
                            legend: 'always',
                            labels: function(recordArray) {
                                var columnNames = [];
                                Ext.each(recordArray, function(item) {
                                    columnNames.push(item.name + ((item.uom) ? " (" + item.uom + ")" : ""));
                                });
                                return columnNames;
                            }(record.get('dataRecord')),
                            labelsDiv: Ext.get(this.legendDiv).dom,
                            labelsDivWidth: this.legendWidth,
                            labelsSeparateLines : true,
                            labelsDivStyles: {
                                'textAlign': 'right'
                            },
                            showRangeSelector: true
                        }
                    );
                },
                scope: this
            }
        });
    }
});

