Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    legendDiv : undefined,
    height : undefined,
    legendWidth : undefined,
    controller : undefined,
    csv : undefined,
    graph : undefined,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = config.plotterDiv || 'dygraph-content';
        this.legendDiv = config.legendDiv || 'dygraph-legend';
        this.legendWidth = config.legendWidth || 250;
        this.height = config.height || 200;
        this.csv = config.csv;
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
        
        this.on("afterrender", function () {
            this.resizePlotter();
            this.graph = new Dygraph(
                Ext.get(this.plotterDiv).dom,
                this.csv,
                {
                    errorBars : true,
                    legend: 'always',
                    labelsDiv: Ext.get(this.legendDiv).dom,
                    labelsDivWidth: this.legendWidth,
                    labelsSeparateLines : true,
                    labelsDivStyles: {
                        'textAlign': 'right'
                    },
                    showRangeSelector: true
                }
                );
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
        this.csv = args.csv;
        this.graph.updateOptions({
            title : args.featureTitle,
            file : this.csv,
            dateWindow : null,
            valueRange : null
        });
    },
    resizePlotter : function() {
        var divPlotter = Ext.get(this.plotterDiv);
        var divLegend = Ext.get(this.legendDiv);
            
        divPlotter.setWidth(this.getWidth() - (this.legendWidth + 2));
        divLegend.setWidth(this.legendWidth);
        divPlotter.setHeight(this.height);
    }
    

});

