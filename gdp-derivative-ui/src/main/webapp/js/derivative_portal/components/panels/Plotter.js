Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    height : undefined,
    controller : undefined,
    csv : undefined,
    graph : undefined,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = Ext.get(config.contentEl || 'dygraph-content');
        this.height = config.height || 200;
        this.csv = config.csv;
        this.controller = config.controller;
        GDP.Plotter.superclass.constructor.call(this, config);
        
        this.on("afterrender", function () {
            this.plotterDiv.setWidth(this.getWidth());
            this.plotterDiv.setHeight(this.height);
            this.graph = new Dygraph(
                this.plotterDiv.dom,
                this.csv,
                {
                    errorBars : true,
                    legend: 'always',
                    labelsDivStyles: {
                        'textAlign': 'right'
                    },
                    showRangeSelector: true
                }
                );
        }, this);
        this.controller.on('updateplotter', function(args){
            this.updatePlotter(args);
        })
    },
    updatePlotter : function(args) {
        LOG.debug('Plotter:updatePlotter: Observed request to update plotter');
        this.csv = args.csv;
//        var graph = new Dygraph(
//                this.plotterDiv.dom,
//                this.csv,
//                {
//                    errorBars : true,
//                    legend: 'always',
//                    labelsDivStyles: {
//                        'textAlign': 'right'
//                    },
//                    showRangeSelector: true
//                }
//            );
        this.graph.updateOptions({file : this.csv});
    }
    

});

