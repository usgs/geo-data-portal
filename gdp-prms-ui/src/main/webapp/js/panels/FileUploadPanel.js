Ext.ns("PRMS");

PRMS.FileUploadPanel = Ext.extend(Ext.form.FormPanel, {
    constructor : function(config) {
        if (!config) config = {};

        config = Ext.apply({
            id: 'panel-fileupload',
            fileUpload: true,
            items: [{
                xtype: 'fileuploadfield',
                id: 'form-file',
                name: 'file-path',
                emptyText: 'Select a file',
                fieldLabel: 'Upload',
                buttonText: 'Choose file'
            }],
            buttons: [{
                text: 'Upload Shapefile',
                handler: function() {
                    var fp = Ext.getCmp('panel-fileupload');
                    if(fp.getForm().isValid()) {
                        fp.getForm().submit({
                            url: 'service/upload?filename=' + fp.form.items.get('form-file').value + '&utility-wps-url=' + encodeURIComponent(CONFIG.WPS_UTILITY_URL) + '&wfs-url=' + encodeURIComponent(CONFIG.GEOSERVER_URL),
                            waitMsg: 'Please be patient while your file uploads',
                            success: function(fp, o) {
                                Ext.Msg.show({
                                    title: 'Success',
                                    msg: o.result.file + ' uploaded',
                                    minWidth: 200,
                                    modal: true,
                                    icon: Ext.Msg.INFO,
                                    buttons: Ext.Msg.OK
                                });
                            },
                            failure: function(fp, o) {
                                Ext.Msg.show({
                                    title: 'Failure',
                                    msg: o.response.responseXML.activeElement.textContent,
                                    minWidth: 200,
                                    modal: true,
                                    icon: Ext.Msg.INFO,
                                    buttons: Ext.Msg.OK
                                });
                            }
                        });
                    }
                },
                scope : this
            }]
        }, config);
    PRMS.FileUploadPanel.superclass.constructor.call(this, config);
    }
});