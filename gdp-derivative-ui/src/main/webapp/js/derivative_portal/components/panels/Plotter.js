Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    legendDiv : undefined,
    height : undefined,
    legendWidth : undefined,
    controller : undefined,
    gmlid : undefined,
    plotterTitle : undefined,
    sosStore : undefined,
    dataArray : [],
    graph : undefined,
    toolbar : undefined,
    plotterValues : undefined,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = config.plotterDiv || 'dygraph-content';
        this.legendDiv = config.legendDiv || 'dygraph-legend';
        this.legendWidth = config.legendWidth || 250;
        this.height = config.height || 200;
        this.gmlid = config.gmlid;
        this.plotterTitle = config.title || '';
        this.controller = config.controller;
        
        this.toolbar = new Ext.Toolbar({
            itemId : 'plotterToolbar',
            ref : '../plotterToolbar',
            items : '&nbsp; '
        })
        var contentPanel = new Ext.Panel({
            contentEl : this.plotterDiv,
            itemId : 'contentPanel',
            ref : '../contentPanel',
            layout : 'fit',
            region : 'center',
            autoShow : true
        });
        var legendPanel = new Ext.Panel({
            itemId : 'legendPanel',
            ref : '../legendPanel',
            contentEl : this.legendDiv,
            layout : 'fit', 
            region : 'east',
            autoShow : true
        });
        config = Ext.apply({
            items : [contentPanel, legendPanel],
            layout : 'border',
            autoShow : true,
            tbar : this.toolbar
        }, config);
        
        GDP.Plotter.superclass.constructor.call(this, config);
        
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
        this.plotterTitle = args.featureTitle;
        this.loadSOSStore();
    },
    resizePlotter : function() {
        var divPlotter = Ext.get(this.plotterDiv);
        var divLegend = Ext.get(this.legendDiv);
        divPlotter.setWidth(this.getWidth() - (this.legendWidth + 2));
        divLegend.setWidth(this.legendWidth);
        divPlotter.setHeight(this.height - this.toolbar.getHeight());
        
        
    },
    loadSOSStore : function() {
        this.sosStore = new GDP.SOSGetObservationStore({
            url : encodeURI(this.gmlid), // gmlid is url for now, eventually, use SOS endpoint + gmlid or whatever param
            autoLoad : true,
            opts : {
                offering: "test",
                observedProperty: "test"
            },
            listeners : {
                load : function(store) {
                    var record = store.getAt(0);
                    var yaxisUnits = undefined;
                    
                    if (this.graph) this.graph.destroy();
                    
                    this.topToolbar.removeAll(true);
                    
                    // Add the title
                    this.topToolbar.add(
                        new Ext.Toolbar.TextItem({
                            id : 'title',
                            html : this.plotterTitle
                        }),
                        new Ext.Toolbar.Fill(),
                        new Ext.Button({
                            itemId : 'plotter-toolbar-download-button',
                            text : 'Download',
                            ref : 'plotter-toolbar-download-button'
                        })
                        );
                    this.topToolbar.doLayout();
                    
                    this.plotterValues = function(values) {
                        Ext.each(values, function(item, index, allItems) {
                            for(var i=0; i<item.length; i++) {
                                var value;
                                if (i==0) {
                                    value = Date.parseISO8601(item[i].split('T')[0]);
                                }
                                else {
                                    value = parseFloat(item[i])
                                }
                                allItems[index][i] = value;
                            }
                        });
                        return values;
                    }(record.get('values'))
                    
                    // Set up the download CSV button
                    this.topToolbar["plotter-toolbar-download-button"].on('click', function(){
                        var id = Ext.id();
                        var frame = document.createElement('iframe');
                        frame.id = id;
                        frame.name = id;
                        frame.className = 'x-hidden';
                        if (Ext.isIE) {
                            frame.src = Ext.SSL_SECURE_URL;
                        }
                        document.body.appendChild(frame);
                        
                        if (Ext.isIE) {
                            document.frames[id].name = id;
                        }
                        
                        var form = Ext.DomHelper.append(document.body, {
                            tag:'form',
                            method:'post',
                            action: 'export?filename=export.csv',
                            target:id
                        });
                        Ext.DomHelper.append(form, {
                            tag:'input',
                            name : 'data',
                            value: function(arr) {
                                var csv = '';
                                Ext.each(arr, function(item) {
                                    LOG.debug(item[0] + ',' + item[1]);
                                    csv += item[0] + ',' + item[1] + '\n';
                                });
                                return encodeURI(csv);
                            }(this.plotterValues)
                        }); 
                        
                        document.body.appendChild(form);
                        var callback = function(e) {
                            var rstatus = (e && typeof e.type !== 'undefined'?e.type:this.dom.readyState );
        
                            switch(rstatus){
                                case 'loading':  //IE  has several readystate transitions
                                case 'interactive': //IE

                                    break;
           
                                case 'load': //Gecko, Opera
                                case 'complete': //IE
                                    if(Ext.isIE){
                                        this.dom.src = "javascript:false"; //cleanup
                                    }
                                    break;
                                default:
                            }
                        };
                
                        Ext.EventManager.on(frame, Ext.isIE?'readystatechange':'load', callback);
                        form.submit();
                    }, this)
                    
                    this.graph = new Dygraph(
                        Ext.get(this.plotterDiv).dom,
                        this.plotterValues,
                        { // http://dygraphs.com/options.html
                            hideOverlayOnMouseOut : false,
                            legend: 'always',
                            labels: function(recordArray) {
                                var columnNames = [];
                                Ext.each(recordArray, function(item) {
                                    columnNames.push(item.name); 
                                    yaxisUnits = item.uom;
                                });
                                return columnNames;
                            }(record.get('dataRecord')),
                            labelsDiv: Ext.get(this.legendDiv).dom,
                            labelsDivWidth: this.legendWidth,
                            labelsSeparateLines : true,
                            labelsDivStyles: {
                                'textAlign': 'right'
                            },
                            rightGap : 5,
                            showRangeSelector: true,
                            ylabel: record.data.dataRecord[1].name,                            
                            yAxisLabelWidth: 75,
                            valueRange: function(values){
                                var intArray = new Array();
                                Ext.each(values, function(item){
                                    this.push(item[1]);
                                }, intArray)
                                var min = Array.min(intArray);
                                var max = Array.max(intArray);
                                return [min - 10 , max + 10];
                            }(this.plotterValues),
                            axes: {
                                x: {
                                    valueFormatter: function(ms) {
                                        return '<span style="font-weight: bold; text-size: big">' +
                                        new Date(ms).strftime('%Y') +
                                        '</span>';
                                    },
                                    axisLabelFormatter: function(d) {
                                        return d.strftime('%Y');
                                    }
                                },
                                y: {
                                    valueFormatter: function(y) {
                                        return "<br />" + y + " " + yaxisUnits + " <br /><br />";
                                    }
                                }
                            }
                        }
                        );
                }
                ,
                scope: this
            }
            
        });
        this.resizePlotter();
    }
});

