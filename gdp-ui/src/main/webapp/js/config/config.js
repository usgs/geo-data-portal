var logger;
var incomingEndpoints = {};
$(document).ready(function() {
    // Set up the logger
    logger = log4javascript.getLogger();
    var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
    var appender = new log4javascript.BrowserConsoleAppender();
    appender.setLayout(layout);
    logger.addAppender(appender);
    
    Constant = Constant();
    Constant.init();
    var equalsCell = $('<td></td>').html('=');
    var nameInputCell = $('<td></td>').append($('<input></input>').attr('type','text'));
    var propertyInputCell = $('<td></td>').append($('<input></input>').attr('type','text').attr('size', '80'));
    var newRow = $('<tr></tr>').append(nameInputCell).append(equalsCell).append(propertyInputCell);
    // Perform XSLT
    logger.debug('Performing XSLT transformation...');
    $('#webform').xslt({xml: $.xslt.xmlToText(Constant.xml), xslUrl: 'js/xslt/properties.xsl', callback: function(data) {
        logger.debug('...done.');
        $('#addrow_button').live('click',function() {
            $(newRow.clone()).insertAfter('tbody tr:nth-last-child(2)');
        });

        $('#submit_button').live('click',function() {
            logger.debug('User is submitting a change to configuration.');
            var formVariables = {};
            $('tr:has("input")').each(function(rowIndex, rowElement){
                var property = $(this).find('.key').attr('value');
                var value = $(this).find('.value').val();
                var del = $(this).find('.delete').is(':checked');

                formVariables[property] = value + ';' + del;
            })
            formVariables.command = 'setprops';

            $.ajax({
                url : Constant.endpoint.properties,
                data : formVariables,
                success : function(data, textStatus, XMLHttpRequest) {
                    logger.debug('Configuration changes have been successfully committed.');
                    window.location.reload();
                },
                error : function() {
                    logger.error('There was a problem committing configuration changes.  Changes were not saved.');
                }
            });

        });
    }});
});
