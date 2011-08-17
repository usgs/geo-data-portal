Ext.ns("GDP");

/**
 * This panel is a self-contained WPS process monitoring panel that also pops up a download link 
 * when a process has completed or informs the user if a process has failed.
 */
GDP.WPSProcessPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    getController : function() {
        return this.controller;
    },
    processId : undefined,
    getProcessId : function() {
        return this.processId;
    },
    runner : undefined,
    getRunner : function() {
        return this.runner;
    },
    cancelButton : undefined,
    constructor : function(config) {
        LOG.debug('WPSProcessPanel:constructor: Constructing self.');
        
        if (!config) config = {};
        this.controller = config.controller;
        this.processId = config.processId;
        this.processLink = config.processLink;
        
        var checkProcessTask = function() {
            Ext.Ajax.request({
                url : GDP.PROXY_PREFIX + this.processLink,
                method: 'GET',
                scope : this,
                success: function ( result, request ) {
                    LOG.debug('Process Task ID ' + this.getProcessId() + ' is being checked.');
                
                    var xml = result.responseXML;
                    var procStarted = xml.getElementsByTagName('ns:ProcessStarted')
                    var procSucceeded = xml.getElementsByTagName('ns:ProcessSucceeded');
                    var procFailed = xml.getElementsByTagName('ns:ProcessFailed');
                    LOG.debug('started' + procStarted[0] + ", succeeded:")
                    if (procStarted.length > 0) {
                        var processStarted = xml.getElementsByTagName('ns:Status')[0].attributes['creationTime'];
                        LOG.debug('Process Task ID ' + this.getProcessId() + ' is still running. <br /><br />Process started ' + processStarted + '<br /><br />Last checked: ' + new Date().format('c'));
                    } else if (procSucceeded.length > 0) {
                        this.getRunner().stopAll();
                        var href = xml.getElementsByTagName('ns:Reference')[0].attributes['href'];
                        LOG.debug('Process Task ID ' + this.getProcessId() + ' succeeded.');
                        this.update("This process has succeeded. <a href='"+href.value+"'>Click</a> to download your file.");
                        var downloadButton = new Ext.Button({
                            id : 'wps-download-button',
                            text : 'Download'
                        });
                        downloadButton.on('click', function(args) {
                        
                            },this, {
                                href : href
                            });
//                        this.add(downloadButton);
                        this.getRunner().stopAll();
                        this.cancelButton.setText('Close Panel');
                        this.doLayout();
                    
                    
                    } else if (procFailed.length > 0) {
                        this.getRunner().stopAll();
                        var failReason = xml.getElementsByTagName('ns1:ExceptionText')[0].textContent;
                        LOG.debug('Process Task ID ' + this.getProcessId() + ' failed: ' + failReason);
                        this.update("This process has failed.<br /><br />Reason: " + failReason + "<br /><br />Click close to close this panel.");
                        this.cancelButton.setText('Close Panel');
                        this.doLayout();
                    }
                },
                failure: function ( result, request) {
                    LOG.debug('WPSProcessPanel:checkProcessTask:failure: Will try again in 5 seconds.');
                    
                }
            });
        };
        
        var cancelButton = new Ext.Button({
//            id : 'wps-cancel-button',
            text : 'Cancel',
            region : 'south'
        });
        cancelButton.on('click', function() {
            this.controller.processCancelled(this.getProcessId())
            this.getRunner().stopAll();
        }, this);
        this.cancelButton = cancelButton;
        
        config = Ext.apply({
            items : [cancelButton],
            html : 'While open, this panel will be checking on process ' + this.processId + '. Close panel to stop checking',
            layout : 'form',
            title : 'WPS Process: ' + this.processId,
            border : true
        }, config);
        GDP.WPSProcessPanel.superclass.constructor.call(this, config);
        
        this.runner = new Ext.util.TaskRunner();
        this.runner.start({
            run : checkProcessTask,
            interval : 5000,
            scope : this
        });
        LOG.debug('WPSProcessPanel:constructor: Timer has started.');
        
        LOG.debug('WPSProcessPanel:constructor: Construction complete.');
        this.on('remove', function(){
            LOG.debug('WPSProcessPanel: Observed "destroy" event.');
            LOG.debug('WPSProcessPanel: Stopping timer.');
            this.getRunner().stopAll();
            LOG.debug('WPSProcessPanel: Destroy event complete.');
        },this)
    }
});