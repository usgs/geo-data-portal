Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    height : undefined,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = Ext.get(config.contentEl || 'dygraph-content');
        this.height = config.height || 200;
        GDP.Plotter.superclass.constructor.call(this, config);
        
        this.on("afterrender", function () {
            this.plotterDiv.setWidth(this.getWidth());
            this.plotterDiv.setHeight(this.height);
            var graph = new Dygraph(
                this.plotterDiv.dom,
                'resources/test.csv',
//                [
//                    [0,1,2],
//                    [1,4,5],
//                    [2,68,44]
//                ],
                {
                    //labels : ['x', 'Num', 'Awesome'],
                    //customBars: true,
                    errorBars : true,
                    //title: 'Test Flight',
                    //ylabel: 'Temperature (F)',
                    legend: 'always',
                    labelsDivStyles: {'textAlign': 'right'},
                    showRangeSelector: true
                }
            );
        }, this);
    }

});

