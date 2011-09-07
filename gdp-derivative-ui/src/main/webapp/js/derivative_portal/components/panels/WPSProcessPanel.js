Ext.ns("GDP");

/**
 * This panel is a self-contained WPS process monitoring panel that also pops up a download link 
 * when a process has completed or informs the user if a process has failed.
 */
GDP.WPSProcessPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    process : undefined,
    runner : undefined,
    actionBar : undefined,
    pauseButton : undefined,
    continueButton : undefined,
    visualizeButton : undefined,
    downloadButton : undefined,
    cancelButton : undefined,
    infoButton : undefined,
    ownerButton : undefined,
    infoPanel : undefined,
    infoPanelCurrentContent : '',
    logTimeFormatting : 'H:i:s',
    defaultInfoPanelHeight : 250,
    timerInterval : 5000,
    currentStatus : 'process-status-stopped',
    processCancelled : false,
    downloadButtonFunction : undefined,
    constructor : function(config) {
        LOG.debug('WPSProcessPanel:constructor: Constructing self.');
        
        if (!config) config = {};
        
        this.controller = config.controller;
        this.process = config.process;
        this.ownerButton = config.ownerButton;
        this.pauseButton = new Ext.Button({
            iconCls : 'process-pause',
            tooltip : 'Pause process'
        });
        this.pauseButton.on('click', function(button) {
            this.pauseButtonOnClick(button);
        }, this);
        
        this.continueButton = new Ext.Button({
            iconCls : 'process-continue',
            tooltip : 'Continue process',
            disabled : true
        });
        this.continueButton.on('click', function(button) {
            LOG.debug('WPSProcessPanel: Continue button clicked. Continuing process.');
            this.continueButtonOnClick(button);
        }, this);
        
        this.cancelButton = new Ext.Button({
            iconCls : 'process-stop',
            tooltip : 'Stop process'
        }); 
        this.cancelButton.on('click', function() {
            LOG.debug('WPSProcessPanel: Cancel button clicked. Cancelling process.');
            this.cancelButtonOnClick();
        }, this);
        
        this.visualizeButton = new Ext.Button({
            iconCls : 'process-visualize',
            tooltip : 'Visualize',
            disabled : true
        });
        this.downloadButton = new Ext.Button({
            iconCls : 'process-download',
            tooltip : 'Download',
            disabled : true
        });
        this.downloadButton.on('click', this.downloadButtonFunction);
        
        this.infoButton = new Ext.Button({
            iconCls : 'process-info',
            tooltip : 'Information'
        });
        this.infoButton.on('click', function(){
            this.infoButtonOnClick();
        }, this)
        this.actionBar = new Ext.Toolbar({
            region : 'north',
            items : [
                this.infoButton,
                ' ', '-', ' ',
                this.pauseButton, 
                ' ', '-', ' ',
                this.continueButton,
                ' ', '-', ' ',
                this.cancelButton,
                ' ', '-', ' ',
                this.downloadButton,
                ' ', '-', ' ',
                this.visualizeButton
            ]
        });
        this.infoPanel = new Ext.Panel({
            height : 250,
            region : 'center',
            border: false,
            autoScroll : true,
            html : ''
        })
        
        config = Ext.apply({
            items : [this.actionBar, this.infoPanel],
            title : this.process.title,
            iconCls : this.currentStatus,
            autoHeight : true
        }, config);
        GDP.WPSProcessPanel.superclass.constructor.call(this, config);
        LOG.debug('WPSProcessPanel:constructor: Construction complete.');
        this.addEvents(
            "request-attention",
            "process-cancelled"
        );
        
        LOG.debug('WPSProcessPanel:constructor: Registering listeners.');
        this.on('remove', function(){
            LOG.debug('WPSProcessPanel: Observed "destroy" event.');
            LOG.debug('WPSProcessPanel: Stopping timer.');
            this.getRunner().stopAll();
            LOG.debug('WPSProcessPanel: Destroy event complete.');
        },this)
        
        this.startProcess();
        LOG.debug('WPSProcessPanel:constructor: Timer has started.');
    }, 
    startProcess : function() { // Happens when user clicks on Beign Process button
        LOG.debug('WPSPanel:startProcess: Collecting process inputs.');
        
        // Begin the process
        Ext.Ajax.request({
            url : GDP.PROXY_PREFIX + GDP.PROCESS_ENDPOINT,
            method: 'POST',
            xmlData : this.process.createWpsExecuteRequest(),
            scope : this,
            success: function (result) {
                LOG.debug('WPSPanel:createProcess:Ajax:success.');
                var xml = result.responseXML;
                var procStarted = xml.getElementsByTagName('ns:ProcessStarted')
                var runningProcessUrl;
                if (procStarted.length > 0) {
                    LOG.debug('WPSProcessPanel:constructor: Processing has started.');
                    this.setPanelIcon({status:'process-status-started'});
                    
                    runningProcessUrl = xml.getElementsByTagName('ns:ExecuteResponse')[0].getAttribute('statusLocation');
                    
                    // This fixes an issue where if we're on the mapped QA instance, runningProcessUrl comes back with proxy stuff already in there (not good)
                    runningProcessUrl = (runningProcessUrl.lastIndexOf(GDP.PROXY_PREFIX) > -1) ? runningProcessUrl.slice(runningProcessUrl.lastIndexOf('proxy/') + 6) : runningProcessUrl;
                    
                    this.process.runningProcessUrl = runningProcessUrl;
                    
                    LOG.debug('WPSProcessPanel:constructor: Creating timer process to check status every ' + (this.interval / 1000) + ' seconds.');
                    this.updateInfoPanel({msg : 'Process successfully submitted.  Will now begin checking <a href="'+runningProcessUrl+'" target="_blank">endpoint</a> for process completion.'});
                    this.runner = new Ext.util.TaskRunner();
                    this.runner.start({
                        run : this.createProcessChecker,
                        interval : this.timerInterval,
                        scope : this
                    });
                } else {
                    var errorNodes = xml.getElementsByTagName('ns:Exception');
                    var errors = errorNodes[0].getElementsByTagName('ns:ExceptionText');
                    LOG.debug('WPSPanel:createProcess:Couldn\'t start process: ' +  errors[0].textContent);
                    this.updateInfoPanel({msg : 'Could not submit process: ' + errors[0].textContent});
                    if (!this.processCancelled) this.cancelButton.fireEvent('click');
                }
            },
            failure: function ( result, request) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:failure');
                this.updateInfoPanel({msg : 'Error communicating with server while submitting process.  Task cancelled.'});
                if (!this.processCancelled) this.cancelButton.fireEvent('click');
            }
        });
    },
    createProcessChecker : function () {
        var processUrl = GDP.PROXY_PREFIX + this.process.runningProcessUrl;
        
        this.setPanelIcon({status:'process-status-started'});
        
        Ext.Ajax.request({
            url : processUrl,
            method: 'GET',
            scope : this,
            success: function (result) {
                var xml = result.responseXML;
                var procStarted = xml.getElementsByTagName('ns:ProcessStarted')
                var procSucceeded = xml.getElementsByTagName('ns:ProcessSucceeded');
                var procFailed = xml.getElementsByTagName('ns:ProcessFailed');
                if (procStarted.length > 0) {
                    this.updateInfoPanel({msg : 'Process is still running.'});
                } else if (procSucceeded.length > 0) {
                    this.runner.stopAll();
                    
                    var href = xml.getElementsByTagName('ns:Reference')[0].attributes['href'];
                    if (href == undefined) href =xml.getElementsByTagName('ns:Reference')[0].attributes[1]; // IE issue - Can't pull attribute name by value, must be index??
                    
                    this.updateInfoPanel({msg : 'This process has succeeded. <a href="'+href.value+'" target="_blank">Click here</a> or the download button to download your file.'});
                    // Make the actual link for the button
                    this.downloadButton.un('click', this.downloadButtonFunction);
                    this.downloadButtonFunction = function() {
                        try {
                            Ext.destroy(Ext.get('downloadIframe'));
                        }
                        catch(e) {}
                        Ext.DomHelper.append(document.body, {
                            tag: 'iframe',
                            id:'downloadIframe',
                            frameBorder: 0,
                            width: 0,
                            height: 0,
                            css: 'display:none;visibility:hidden;height:0px;',
                            src: href.value
                        });
                    };
                    this.downloadButton.on('click', this.downloadButtonFunction)
                    this.downloadButton.enable();
                    
                    if (!this.processCancelled) this.cancelButton.fireEvent('click');
                    
                    this.setPanelIcon({status:'process-status-completed'})
                } else if (procFailed.length > 0) {
                    this.runner.stopAll();
                    var failReason = xml.getElementsByTagName('ns1:ExceptionText')[0].textContent;
                    this.updateInfoPanel({msg : 'This process has failed.<br /><br />Reason: ' + failReason});
                    if (!this.processCancelled) this.cancelButton.fireEvent('click');
                }
            },
            failure: function ( result, request) {
                LOG.debug('WPSProcessPanel:checkProcessTask:failure: Will try again in 5 seconds.');
                this.updateInfoPanel({msg : 'Error communicating with server while checking process. Will try again.'});
            }
        });
    },
    updateInfoPanel : function(args) {
        var msg = '<b>' + new Date().format(this.logTimeFormatting) + '</b> - ' + args.msg;
        var currentContent = this.infoPanelCurrentContent;
        this.infoPanel.update(currentContent + msg + '<br /><br />');
        this.infoPanelCurrentContent =  currentContent + msg + '<br /><br />';
    },
    setPanelIcon : function(args) {
        var status = args.status;
        this.setIconClass(status);
        this.currentStatus = status;
        this.doLayout();
    },
    cancelButtonOnClick : function() {
        this.processCancelled = true;
        this.cancelButton.getEl().removeClass('process-stop');
        this.cancelButton.setIconClass('process-close');
        this.continueButton.disable();
        this.pauseButton.disable();
        this.setPanelIcon({status:'process-status-stopped'});
        this.updateInfoPanel({msg : 'Process checking stopped.  Clicking on the close button will close this panel.'});
        if (this.runner) this.runner.stopAll();
        this.fireEvent('process-cancelled', {obj : this});
        this.cancelButton.on('click', function() {
            LOG.debug('WPSProcessPanel: Close button clicked. Closing process.');
            this.ownerButton.enable();
            this.ownerCt.remove(this, true);
        }, this);
    },
    pauseButtonOnClick : function(button) {
        button.disable();
        this.continueButton.enable();
        this.runner.stopAll();
        this.setPanelIcon({status:'process-status-paused'});
    },
    continueButtonOnClick : function(button) {
        button.disable();
        this.pauseButton.enable();
        this.runner.start({
            run : this.createProcessChecker,
            interval : this.timerInterval,
            scope : this
        });
    },
    infoButtonOnClick : function() {
        if (this.infoPanel.isVisible()) {
            this.infoPanel.hide();
            this.setHeight(this.getHeight() - this.defaultInfoPanelHeight);
        } else {
            this.setHeight(this.getHeight() + this.defaultInfoPanelHeight);
            this.infoPanel.show();
        }
        this.doLayout();
    }
});