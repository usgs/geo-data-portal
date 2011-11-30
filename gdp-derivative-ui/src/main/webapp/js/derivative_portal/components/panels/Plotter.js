Ext.ns("GDP");

GDP.Plotter = Ext.extend(Ext.Panel, {
    plotterDiv : undefined,
    legendDiv : undefined,
    height : undefined,
    legendWidth : undefined,
    controller : undefined,
    plotterTitle : undefined,
    sosStore : [],
    plotterData : [],
    graph : undefined,
    toolbar : undefined,
    scenarioGcmJSON : {},
    
    yLabels : [],
    plotterYMin : 10000000,
    plotterYMax : 0,
    constructor : function(config) {
        config = config || {};
        this.plotterDiv = config.plotterDiv || 'dygraph-content';
        this.legendDiv = config.legendDiv || 'dygraph-legend';
        this.legendWidth = config.legendWidth || 250;
        this.height = config.height || 200;
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
        }, this);
        this.on('resize', function() {
            LOG.debug('Plotter:resize');
            this.resizePlotter();
        }, this);
        this.controller.on('loaded-catstore', function(args) {
            LOG.debug('Plotter:onLoadedCatstore');
            this.onLoadedCatstore(args);
        }, this);
    },
    
    updatePlotter : function(args) {
        LOG.debug('Plotter:updatePlotter: Observed request to update plotter');
        
        var endpoint = args.url;
        var offering = args.offering;
        this.plotterTitle = args.featureTitle;
        this.yLabels = [];
        // TODO this is not working, fixme
        if (this.sosStore) {
            this.sosStore= new Array();
        }
        if (this.graph) {
            this.graph.destroy();
        }
        
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
        
        Ext.iterate(this.scenarioGcmJSON, function(scenario, object) {
            Ext.iterate(object, function(gcm, valueArray) {
                var meta = {};
                var url = endpoint.replace('{gcm}', gcm);
                url = url.replace('{scenario}', scenario);
                url = url.replace('{threshold}', this.controller.getThreshold());
                meta.url = url;
                meta.scenario = scenario;
                meta.gcm = gcm;
                this.loadSOSStore(meta, offering);
            }, this);
        }, this);
        

    },
    resizePlotter : function() {
        LOG.debug('Plotter:resizePlotter()');
        var divPlotter = Ext.get(this.plotterDiv);
        var divLegend = Ext.get(this.legendDiv);
        divPlotter.setWidth(this.getWidth() - (this.legendWidth + 2));
        divLegend.setWidth(this.legendWidth);
        divPlotter.setHeight(this.height - this.toolbar.getHeight()); 
    },
    onLoadedCatstore : function(args) {
        Ext.each(args.record.get("scenarios"), function(scenario) {
            var scenarioKey = this.cleanUpIdentifiers(scenario[0]);
            this.scenarioGcmJSON[scenarioKey] = {};
            Ext.each(args.record.get("gcms"), function(gcm) {
                var gcmKey = this.cleanUpIdentifiers(gcm[0]);
                this.scenarioGcmJSON[scenarioKey][gcmKey] = [];
            }, this);
        }, this);
        Ext.DomHelper.append(Ext.DomQuery.selectNode("div[id='dygraph-content']"), {
            tag : 'div', 
            id : 'plotter-prefill-text',
            html : args.record.get('helptext')['plotWindowIntroText']
        });
    },
    loadSOSStore : function(meta, offering) {
        var url = "proxy/" + meta.url + "?service=SOS&request=GetObservation&version=1.0.0&offering=" +
        encodeURI(offering) + "&observedProperty=mean";
        
        this.sosStore.push(new GDP.SOSGetObservationStore({
            url : url, // gmlid is url for now, eventually, use SOS endpoint + gmlid or whatever param
            autoLoad : true,
            //            opts : {
            //                offering: offering,
            //                observedProperties: ["mean"]
            //            },
            proxy : new Ext.data.HttpProxy({
                url: url, 
                disableCaching: false, 
                method: "GET"
            }),
            baseParams : {},
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
                    //                        var rstatus = (e && typeof e.type !== 'undefined'?e.type:this.dom.readyState );

                    //this.dygraphUpdateOptions(store);
                    this.globalArrayUpdate(store, meta);
                },
                scope: this
            }
            
        }));
    },
    globalArrayUpdate : function(store, meta) {
        var record = store.getAt(0);
        this.scenarioGcmJSON[meta.scenario][meta.gcm] = function(values) {
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
        }(record.get('values'));

        var isComplete = true;

        Ext.iterate(this.scenarioGcmJSON, function(key, value, object) {
            Ext.iterate(value, function(key, value, object) {
                if (value.length == 0) {
                    isComplete = false;
                }
            }, this);
        }, this);
        if (isComplete) {
            // calculate this.plotterData;
            this.plotterData = [];
            var observationsLength;
            var scenarios = [];
            var gcms = [];
            Ext.iterate(this.scenarioGcmJSON, function(scenario, value) {
                scenarios.push(scenario);
                Ext.iterate(value, function(gcm, value) {
                    if(gcms.indexOf(gcm) == -1) {
                        gcms.push(gcm);
                    }
                    if (!observationsLength) {
                        observationsLength = value.length;
                    }
                });
            });
            
            this.yLabels = scenarios;
            
            this.plotterYMin = 1000000;
            this.plotterYMax = 0;
            
            for (var i=0; i<observationsLength; i++) {
                this.plotterData.push(new Array());
                this.plotterData[i][0] = this.scenarioGcmJSON[scenarios[0]][gcms[0]][i][0];

                Ext.each(scenarios, function(scenario) {
                    var scenarioArray = [];
                    Ext.each(gcms, function(gcm) {
                        scenarioArray.push(this.scenarioGcmJSON[scenario][gcm][i][1]);
                    }, this);
                    var min = Array.min(scenarioArray);
                    var mean = Array.mean(scenarioArray);
                    var max = Array.max(scenarioArray);
                    this.plotterData[i].push([min, mean, max]);
                    if (min < this.plotterYMin) {
                        this.plotterYMin = min
                        }
                    if (max > this.plotterYMax) {
                        this.plotterYMax = max
                        }
                }, this);
            }
            this.dygraphUpdateOptions(store);
            
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
                    value: function(dataJSON) {
                        var observationsLength;
                        var scenarios = [];
                        var gcms = [];
                        Ext.iterate(dataJSON, function(scenario, value) {
                            scenarios.push(scenario);
                            Ext.iterate(value, function(gcm, value) {
                                if(gcms.indexOf(gcm) == -1) {
                                    gcms.push(gcm);
                                }
                                if (!observationsLength) {
                                    observationsLength = value.length;
                                }
                            });
                        });
                    
                        var csv = '';
                        for (var i=0; i<observationsLength; i++) {
                            var line = '';
                            line += dataJSON[scenarios[0]][gcms[0]][i][0] + ",";

                            Ext.each(scenarios, function(scenario) {
                                Ext.each(gcms, function(gcm) {
                                    line += dataJSON[scenario][gcm][i][1] + ",";
                                }, this);
                            }, this);
                            csv += line.substr(0, line.length - 1) + "\n";
                        }

                        return encodeURI(csv);
                    }(this.scenarioGcmJSON)
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
        }
    },
    dygraphUpdateOptions : function(store) {
        var record = store.getAt(0);
        // this is mean for us, probably figure this out better?
        var yaxisUnits = record.get('dataRecord')[1].uom;
        //var valueRangeMax = this.plotterYMax + (this.plotterYMax / 10);
        //var valueRangeMin

        // TODO figure out what to do if dataRecord has more than time and mean
        //this.yLabels.push(record.get('dataRecord')[1].name);
        //this.yLabels = this.scenarioGcmJSON.keys
        var plotterDiv = Ext.get(this.plotterDiv).dom;
        this.graph = new Dygraph(
            Ext.get(this.plotterDiv).dom,
            this.plotterData,
            { // http://dygraphs.com/options.html
                hideOverlayOnMouseOut : false,
                legend: 'always',
                customBars: true,
                errorBars: true,
                labels: ["Date"].concat(this.yLabels),
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
                ylabel: this.controller.getDerivative().get('derivative') + " (" +
                this.controller.getThreshold() + this.controller.getUnits() + ")",
                valueRange: [this.plotterYMin - (this.plotterYMin / 10) , this.plotterYMax + (this.plotterYMax / 10)],
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
                            return Math.round(y) + " " + yaxisUnits + "<br />";
                        }
                    }
                }
            }
            );
    },
    // These are some business rules for how our scenario or gcms appear in urls
    cleanUpIdentifiers : function(str) {
        str = str.toLowerCase();
        str = str.replace(' ', '_');
        str = str.replace('.', '-');
        return str;
    }
});

