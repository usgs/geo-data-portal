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
            button.disable();
            this.continueButton.enable();
            this.runner.stopAll();
        }, this);
        
        this.continueButton = new Ext.Button({
            iconCls : 'process-continue',
            tooltip : 'Continue process',
            disabled : true
        });
        this.continueButton.on('click', function(button) {
            button.disable();
            this.pauseButton.enable();
            this.runner.start(this.createProcessChecker);
        }, this);
        
        this.cancelButton = new Ext.Button({
            iconCls : 'process-stop',
            tooltip : 'Stop process'
        }); 
        this.cancelButton.on('click', function(button) {
            LOG.debug('WPSProcessPanel: Cancel button clicked. Cancelling process.');
            this.processCancelled = true;
            this.cancelButton.getEl().removeClass('process-stop');
            this.cancelButton.setIconClass('process-close');
            this.continueButton.disable();
            this.pauseButton.disable();
            this.setIconClass('process-status-stopped');
            this.currentStatus = 'process-status-stopped';
            this.updateInfoPanel({ msg : 'Process now cancelled.  Clicking on the close button will close this panel.'});
            if (this.runner) this.runner.stopAll();
            this.fireEvent('process-cancelled', {obj : this});
            this.cancelButton.on('click', function() {
                LOG.debug('WPSProcessPanel: Close button clicked. Closing process.');
                this.ownerButton.enable();
                this.ownerCt.remove(this, true);
            }, this);
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
        this.infoButton = new Ext.Button({
            iconCls : 'process-info',
            tooltip : 'Information'
        });
        this.infoButton.on('click', function(){
            if (this.infoPanel.isVisible()) {
                this.infoPanel.hide();
                this.setHeight(this.getHeight() - this.defaultInfoPanelHeight);
            } else {
                this.setHeight(this.getHeight() + this.defaultInfoPanelHeight);
                this.infoPanel.show();
            }
            this.doLayout();
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
            iconCls : this.currentStatus
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
            xmlData : (new XMLSerializer()).serializeToString(this.process.createWpsExecuteRequest()),
            scope : this,
            success: function (result) {
                LOG.debug('WPSPanel:createProcess:Ajax:success.');
                var xml = result.responseXML;
                var procStarted = xml.getElementsByTagName('ns:ProcessStarted')
                var runningProcessUrl;
                if (procStarted.length > 0) {
                    LOG.debug('WPSProcessPanel:constructor: Processing has started.');
                    this.setIconClass('process-status-started');
                    this.currentStatus = 'process-status-started';

                    runningProcessUrl = xml.getElementsByTagName('ns:ExecuteResponse')[0].getAttribute('statusLocation');
                    this.process.runningProcessUrl = runningProcessUrl;
                    
                    LOG.debug('WPSProcessPanel:constructor: Creating timer process to check status every ' + (this.interval / 1000) + ' seconds.');
                    this.updateInfoPanel({msg : 'Process successfully submitted.  Will now begin checking ' + runningProcessUrl + ' for process completion.'});
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
        
        return; // stop here for now
    },
    createProcessChecker : function () {
        var processUrl = GDP.PROXY_PREFIX + this.process.runningProcessUrl;
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
                    LOG.debug('Process Task ID ' + this.getProcessId() + ' succeeded.');
                    this.updateInfoPanel({msg : 'This process has succeeded. <a href="'+href.value+'">Click</a> to download your file.'});
                    if (!this.processCancelled) this.cancelButton.fireEvent('click');
                    this.setIconClass('process-status-completed');
                    this.currentStatus = 'process-status-completed';
                    this.doLayout();
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
        var msg = new Date().format(this.logTimeFormatting) + ' - ' + args.msg;
        var currentContent = this.infoPanelCurrentContent;
        this.infoPanel.update(currentContent + msg + '<br /><br />');
        this.infoPanelCurrentContent =  currentContent + msg + '<br /><br />';
    }
});