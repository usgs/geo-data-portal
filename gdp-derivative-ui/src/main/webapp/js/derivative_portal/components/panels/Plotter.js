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
    
    yLabels : [],
    plotterYMin : 10000000,
    plotterYMax : 0,
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
            LOG.debug('Plotter:afterrender');
            this.resizePlotter();
        }, this);
        this.controller.on('updateplotter', function(args){
            LOG.debug('Plotter:updateplotter');
            this.updatePlotter(args);
        }, this),
        this.on('resize', function() {
            LOG.debug('Plotter:resize');
            this.resizePlotter();
        }, this)
    },
    
    updatePlotter : function(args) {
        LOG.debug('Plotter:updatePlotter: Observed request to update plotter');
        
        this.gmlid = args.gmlid;
        this.plotterTitle = args.featureTitle;
        this.yLabels = [];
        // TODO this is not working, fixme
        this.sosStore.clear();
        if (this.graph) {
            this.graph.destroy()
        }
        
        this.topToolbar.removeAll(true);
        
        // Add the title
        this.topToolbar.add(
            new Ext.Toolbar.TextItem({
                id : 'title',
                html : this.plotterTitle
            }),
            new Ext.Toolbar.Fill()//,
//            new Ext.Button({
//                itemId : 'plotter-toolbar-download-button',
//                text : 'Download',
//                ref : 'plotter-toolbar-download-button'
//            })
            );
        this.topToolbar.doLayout();
        
        this.graph = new Dygraph(
            Ext.get(this.plotterDiv).dom,
            this.dataArray,
            { // http://dygraphs.com/options.html
                hideOverlayOnMouseOut : false,
                legend: 'always',
                labels: this.yLabels,
                labelsDiv: Ext.get(this.legendDiv).dom,
                labelsDivWidth: this.legendWidth,
                labelsSeparateLines : true,
                labelsDivStyles: {
                    'textAlign': 'right'
                },
                rightGap : 5,
                showRangeSelector: true,
                //ylabel: record.data.dataRecord[1].name,                            
                yAxisLabelWidth: 75,
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
                    }
                }
            }
            );
        var endpointArray = [{scenario: "a1b", gcm: "ccsm3", endpoint: "resources/states/texas.xml"}, "resources/states/alabama.xml", "resources/states/arizona.xml", "resources/states/iowa.xml"];
        this.completionArray(endpointArray.length);
        Ext.each(completionArray, function(it, ind){ this.completionArray[ind] = false}, this);
        Ext.each(endpointArray, function(endpoint, index) {
            this.loadSOSStore(endpoint, index);
        });
        
        // TODO Make sure everything is done!
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
    },
    resizePlotter : function() {
        LOG.debug('Plotter:resizePlotter()');
        var divPlotter = Ext.get(this.plotterDiv);
        var divLegend = Ext.get(this.legendDiv);
        divPlotter.setWidth(this.getWidth() - (this.legendWidth + 2));
        divLegend.setWidth(this.legendWidth);
        divPlotter.setHeight(this.height - this.toolbar.getHeight()); 
    },
    loadSOSStore : function(endpoint, indexOfThisStore) {
        this.sosStore.push(new GDP.SOSGetObservationStore({
            url : encodeURI(endpoint.endpoint + "&feature=" + this.gmlid), // gmlid is url for now, eventually, use SOS endpoint + gmlid or whatever param
            autoLoad : true,
            opts : {
                offering: "test",
                observedProperty: "test"
            },
            listeners : {
                load : function(store) {
// Ivan's IE fix
//                    var record = store.getAt(0);
//                    var yaxisUnits = undefined;
//                    
//                    if (this.graph) this.graph.destroy();
//                    
//                    this.topToolbar.removeAll(true);
//                    
//                    // Add the title
//                    this.topToolbar.add(
//                        new Ext.Toolbar.TextItem({
//                            id : 'title',
//                            html : this.plotterTitle
//                        }),
//                        new Ext.Toolbar.Fill(),
//                        new Ext.Button({
//                            itemId : 'plotter-toolbar-download-button',
//                            text : 'Download',
//                            ref : 'plotter-toolbar-download-button'
//                        })
//                        );
//                    this.topToolbar.doLayout();
//                    
//                    this.plotterValues = function(values) {
//                        Ext.each(values, function(item, index, allItems) {
//                            for(var i=0; i<item.length; i++) {
//                                var value;
//                                if (i==0) {
//                                    value = Date.parseISO8601(item[i].split('T')[0]);
//                                }
//                                else {
//                                    value = parseFloat(item[i])
//                                }
//                                allItems[index][i] = value;
//                            }
//                        });
//                        return values;
//                    }(record.get('values'))
//                    
//                    // Set up the download CSV button
//                    this.topToolbar["plotter-toolbar-download-button"].on('click', function(){
//                        var id = Ext.id();
//                        var frame = document.createElement('iframe');
//                        frame.id = id;
//                        frame.name = id;
//                        frame.className = 'x-hidden';
//                        if (Ext.isIE) {
//                            frame.src = Ext.SSL_SECURE_URL;
//                        }
//                        document.body.appendChild(frame);
//                        
//                        if (Ext.isIE) {
//                            document.frames[id].name = id;
//                        }
//                        
//                        var form = Ext.DomHelper.append(document.body, {
//                            tag:'form',
//                            method:'post',
//                            action: 'export?filename=export.csv',
//                            target:id
//                        });
//                        Ext.DomHelper.append(form, {
//                            tag:'input',
//                            name : 'data',
//                            value: function(arr) {
//                                var csv = '';
//                                Ext.each(arr, function(item) {
//                                    LOG.debug(item[0] + ',' + item[1]);
//                                    csv += item[0] + ',' + item[1] + '\n';
//                                });
//                                return encodeURI(csv);
//                            }(this.plotterValues)
//                        }); 
//                        
//                        document.body.appendChild(form);
//                        var callback = function(e) {
//                            var rstatus = (e && typeof e.type !== 'undefined'?e.type:this.dom.readyState );

                    this.dygraphUpdateOptions(store);
                    this.globalArrayUpdate(indexOfThisStore, endpoint.scenario);
                },
                scope: this
            }
            
        }));
        this.resizePlotter();
    },
    globalArrayUpdate : function(indexOfThisStore) {
        this.globalArray[indexOfThisStore] = true;
        // Do calculation on what we have so far
        // Notify user that we are still working
        var myValues = this.plotterValuesArray[indexOfThisStore];
        
        
        if (!globalArray.indexOf(false)) {
            // We are done
            // Notify user that all has been received
        }
        // updatePlotter()
    },
    dygraphUpdateOptions : function(store) {
        var record = store.getAt(0);
        var yaxisUnits = undefined;

        this.plotterValuesArray.push(function(values) {
            var context = { scenario : 'a1b'}
            Ext.each(values, function(item, index, allItems) {
                for(var i=0; i<item.length; i++) {
                    var value;
                    if (i==0) {
                        value = new Date(item[i])
                    }
                    else {
                        value = parseFloat(item[i])
                    }
                    allItems[index][i] = value;
                }
            });
            context.values = values;
            return context;
        }(record.get('values')));
// My version with for multi-plotting
        
        // TODO figure out what to do if dataRecord has more than time and mean
        yaxisUnits = record.get('dataRecord')[1].uom;
        this.yLabels.push(record.get('dataRecord')[1].name);

        this.graph.updateOptions(
            { // http://dygraphs.com/options.html
                data: this.plotterData,
                ylabel: this.graph.ylabel || record.data.dataRecord[1].name,
                valueRange: function(values){
                    var intArray = new Array();
                    Ext.each(values, function(item){
                        this.push(item[1]);
                    }, intArray)
                    this.plotterYMin = Array.min(intArray, this.plotterYMin);
                    this.plotterYMax = Array.max(intArray, this.plotterYMax);
                    return [this.plotterYMin - 10 , this.plotterYMax + 10];
                }(this.plotterValuesArray[this.plotterValuesArray.length-1]),
                axes: {
                    y: {
                        valueFormatter: function(y) {
                            return "<br />" + y + " " + yaxisUnits + " <br /><br />";
                        }
                    }
                }
            }
            );
    }
});

