/*    
Submit = function() {
    var divString = '<div></div>';
    var inputString = '<input></input>';
    var selectString = '<select></select>';
    var optionString = '<option></option>';
    var spanString = '<span></span>'
    var abstractText;
    var retrieveOutputURL;
    var configured = false;
    var _htmlID = 'submit';



 

    function submitForProcessingCallback(xml) {
        if (!WPS.checkWpsResponse(xml, 'Error submitting request for processing.')) {
            $('#submitForProcessingButton').removeAttr('disabled');
            return false;
        }

        var statusLocation = $(xml).find('ns|ExecuteResponse').attr('statusLocation');
        var email = $('#emailInputBox').val().trim();

        var wpsAlgorithm = 'gov.usgs.cida.gdp.wps.algorithm.communication.EmailWhenFinishedAlgorithm';
        var wpsInputs = {
            'wps-checkpoint': [statusLocation],
            'email': [email]
        };

        var wpsOutput = ['result'];

        if (email) {
            WPS.sendWpsExecuteRequest(Constant.endpoint.proxy + Constant.endpoint.utilitywps, wpsAlgorithm, wpsInputs, wpsOutput, false, emailCallback);
        }

        var statusID = (statusLocation.split('?')[1]).split('id=')[1];

        var intervalID = window.setInterval(function() {
            $.ajax({
                url : Constant.endpoint.proxy + Constant.endpoint.statuswps,
                data : {
                    'id': statusID
                },
                success : function(data, textStatus, XMLHttpRequest) {
                    statusCallback(XMLHttpRequest.responseText, intervalID, statusID);
                },
                error : function() {
                    $('#submitForProcessingButton').removeAttr('disabled');
                    window.clearInterval(intervalID);
                }
            });
        }, 5000);

        showNotification('Processing your request.');
    }

    function statusCallback(xmlText, intervalID, statusID) {
        ////////////////////////////////////////////////////////////////////////
        // Workaround and extra logging for bug where empty xml is returned. 
        // Ignore it and keep rechecking.
        if (!xmlText || xmlText == '') {
            logger.warn('GDP: RetrieveResultServlet returned empty response. Retrying.');
            showErrorNotification('RetrieveResultServlet returned empty response. Retrying.');
            return;
        }
        
        var xml = $.xmlDOM(xmlText);
        
        if (xml.length == 0) {
            logger.warn('GDP: RetrieveResultServlet response is not valid xml. Retrying.');
            showErrorNotification('RetrieveResultServlet response is not valid xml. Retrying.');
            logger.warn('GDP: invalid xml = \'' + xmlText + '\'');
            return;
        } else {
            xml = xml[0];
        }
        ////////////////////////////////////////////////////////////////////////
        
        if (!WPS.checkWpsResponse(xml, 'Error checking status of submission.')) {
            $(_submitForProcessingLink).fadeIn(Constant.ui.fadeSpeed);
            logger.warn('GDP: error xml = \'' + xmlText + '\'');
            window.clearInterval(intervalID);
            return;
        }

        if ($(xml).find('ns|ProcessStarted').length > 0) {
            //console.log('process started');
            
        } else if ($(xml).find('ns|ProcessSucceeded').length > 0) {
            window.clearInterval(intervalID);
            showNotification('Request successfully completed.', true);

            retrieveOutputURL = $(xml).find('ns|Output').find('ns|Reference').attr('href');

            $('#retrieveOutputButton').removeAttr('disabled');
            bindRetrieveProcInfoButton(statusID);
            $('#retrieveProcessInfoButtonButton').removeAttr('disabled');
            
        } else if ($(xml).find('ns|ProcessFailed').length > 0) {
            $('#submitForProcessingButton').removeAttr('disabled');
            window.clearInterval(intervalID);
            showErrorNotification('Process failed: ' + $(xml).find('ns|ProcessFailed ns1|ExceptionText').text());
        } else {
            showErrorNotification('Unknown RetrieveResultServlet wps response');
            logger.warning('unknown wps response = ' + '\'' + xmlText + '\'');
        }
    }

    /**
     * This sets up a configuration of the application where we only have one
     * algorithm to display to the user.
     */
    function setupSingleAlgorithmView(algorithm) {
        // TODO - Currently if we have a single algorithm with nothing to configure, the configure link shows up anyway.
        // When a user clicks it, it goes away and nothing happens. We need a way to know before this view shows up whether or
        // not we can configure the algorithm.  If not, don't show the link or remove it at this point.
        var singleAlgorithm = Algorithm.algorithms[algorithm]
            
        // Protect ourselves from front-end garbage -- This means config is configured for a nonexistent algorithm -- default to the dropdown
        if (!singleAlgorithm) {
            populateAlgorithmDropdownView();
        } else {
            var abstractText = singleAlgorithm.abstrakt;
            var title = singleAlgorithm.title;

            // Append the current algorithm we're displaying to the html body
            $('#algorithm_dropdown_container').append($('<span></span>').attr('id', 'algorithm_display_span').append('Algorithm: ', title));
            $('#algorithm_dropdown_container').append('&nbsp;&nbsp;<a href="#" id="configure_algorithm_link"> Configure</a>');
            if (abstractText) {
                $('#algorithm_dropdown_container').append('&nbsp;&nbsp;<a href="#" id="algorithm_documentation_link"> Documentation</a>');
                configureDocumentationLink(singleAlgorithm.title, abstractText);
            }
            
            // Wire up the configure link
            $('#configure_algorithm_link').click(function() { 
                populateDynamicContent(singleAlgorithm.xml); 
            });
        }
    }
//
//    function configureDocumentationLink(title, abstractText) {
//        $('#algorithmDocumentation').remove();
//        
//        $('body').append(
//            $('<div></div>').addClass('hidden').attr('id', 'algorithmDocumentation').html(abstractText)
//            );
//            
//        $('#algorithm_documentation_link').click(function() {
//            $( "#algorithmDocumentation" ).dialog({
//                buttons: {
//                    'OK' : function() {
//                        $(this).dialog("close");
//                    }
//                },
//                title: title + ' Description',
//                width: 'auto',
//                height: 'auto',
//                modal: true,
//                zIndex: 9999
//            });
//        })
//    }

    /**
     * Creates a 'GetCapabilities' call to the WPS server and retrieves the list of algorithms available.
     * Using those algorithms, a dropdown list is populated on the submit page.
     
    function populateAlgorithmDropdownView() {
        logger.debug("GDP: Populating algorithm dropdown.");
        // Create a new dropdown with a blank entry first
        var select = $(selectString).attr('id','algorithm_dropdown').append($(optionString).attr('value', ''));
            
        // For each algorithm we use the 'ows:Identifier' long name as the option value
        // We use the title as the display value for the dropdown
        $.each(Algorithm.algorithms, function(i,v) {
            var identifier = i;
            var title = v.title;
            var option = $(optionString).attr('value', identifier).html(title);
            $(select).append(option);
        });

        // Append the label and dropdown to the html body
        $('#algorithm_dropdown_container').append('<label></label>').attr('for', 'algorithm_dropdown').html('Choose an algorithm: ');
        $('#algorithm_dropdown_container').append(select);

        bindAlgorithmDropDown();
        $('#algorithm_dropdown_container').append('&nbsp;&nbsp;<a href="#" id="algorithm_documentation_link"> Documentation</a>&nbsp;&nbsp;');
        configureDocumentationLink('All Algorithms', 
            function() {
                var aText = ''; 
                $.each(Algorithm.algorithms, function(i,v) {
                    aText += '<h3>' + v.title + '</h3>';
                    aText += v.abstrakt + '<br /><br />';
                })
                return aText;
            }()
            );
    }

    function bindAlgorithmDropDown() {
        // Bind the dropdown list
        $('#algorithm_dropdown').change(function() {
            var selectedAlgorithm = $('#algorithm_dropdown option:selected').val();
                
            // Did the user select the blank option? 
            if (!selectedAlgorithm) {
                // Yes, they did. Disable the submit button
                $('#submitForProcessingButton').attr('disabled', 'disabled');
                // There's nothing to configure
                $('#configure_algorithm_link').remove();
                // Show the user the description for all the algorithms
                configureDocumentationLink('All Algorithms', 
                    function() {
                        var aText = ''; 
                        $.each(Algorithm.algorithms, function(i,v) {
                            aText += '<h3>' + v.title + '</h3>';
                            aText += v.abstrakt + '<br /><br />';
                        })
                        return aText;
                    }()
                    );
            } else {
                // No, the user selected a valid algorithm. Handle that.
                $('#algorithm_dynamic_container_content').html(''); // Clear out what's currently in the dynamic container
                populateDynamicContent(Algorithm.algorithms[selectedAlgorithm].xml);
            }
        });
    }
    
    /**
     * Goes through the WPS DescribeProcess response and uses each <input> field
     * to create a dynamic form
     
    function populateDynamicContent(xml) {
        // First we clear out the dynamic content that may or may not have been there
        $('#algorithm_dynamic_container_content').html('');
        var algorithm = $(xml).find('ns1|Identifier').first().text();
        abstractText = ($(xml).find('ns1|Abstract')).first() ? $(xml).find('ns1|Abstract').first().text() : '';
        
        // If we find that this algorithm doesn't take TIME_START and we HAVE a TIME_START,
        // that means we don't have the right dataset for this algorithm
        if (Dataset.getFromDate() && !$(xml).find('DataInputs > Input > ns1|Identifier:contains(TIME_START)').length) {
            // show error
            showErrorNotification("The dataset you chose is not compatible with this algorithm.");
            $('#submitForProcessingButton').attr('disabled', '');
            return;
        }
        WPS.processDescriptions[algorithm] = xml;
        // Add the algorithm full name to the container in a hidden field
        $('#algorithm_dynamic_container_content').append(
            $(divString).addClass('hidden').append(
                $(inputString).
                attr('id', 'di_algorithm_identifier').
                attr('name', 'di_algorithm_identifier').
                attr('type', 'hidden').
                attr('value', algorithm)
                )
            );

        // For each input field we add the HTML element for this type of field
        $(xml).find('DataInputs > Input').each(function(index, element){
            $('#algorithm_dynamic_container_content').append(
                createHTMLInputField(element)
            );
            $('.di_identifier').hide();
        });
        initializeTips();
        
        $('#current_input_index').attr('value', '0');
        bindDynamicInputElements();
        
        $('#algorithm_documentation_link').remove();
        if (abstractText) {
            $('#algorithm_dropdown_container').append('&nbsp;&nbsp;<a href="#" id="algorithm_documentation_link"> Documentation</a>&nbsp;&nbsp;');
            configureDocumentationLink(Algorithm.algorithms[algorithm].title, abstractText);
        }
        // Check if we have anything to actually configure
        if ($('#algorithm_dynamic_container').find('.di_element_wrapper').length > 0) {
            $( "#algorithm_dynamic_container" ).dialog({
                buttons: {
                    'OK' : function() {
                        $(this).dialog("close");
                        configured = true;
                        populateConfigurationSummary();
                        // Create the Configure link if one does not already exist
                        if ($.find('#configure_algorithm_link').length == 0) {
                            $('#algorithm_dropdown_container').append('<a href="#" id="configure_algorithm_link"> Configure</a>');
                            // Bind the configure link
                            $('#configure_algorithm_link').click(function() {
                                $('#algorithm_dropdown').trigger('change');
                            });
                        }
                    }
                },
                title: 'Configure ' + $('#di_algorithm_identifier').val().split('.').pop(),
                width: 'auto',
                height: 'auto',
                modal: true,
                zIndex: 9999
            });
        } else {
            // We don't have anything to configure, so just enable the submit button
            $('#submitForProcessingButton').attr('disabled', '');
            $('#algorithm_configuration_summary').html('');
            $('#configure_algorithm_link').remove();
        }
    }

    /**
     * For each WPS DescribeProcess input element, create a representative HTML input field
     
    function createHTMLInputField(xml) {
        var minOccurs = parseInt($(xml).attr('minOccurs'));
        var maxOccurs = parseInt($(xml).attr('maxOccurs'));
        var lastIndexId = $('#algorithm_dynamic_container_content > div:last-child').attr('id');
        var lastIndex = parseInt(lastIndexId.substring(lastIndexId.length - 1));
        var index = (isNaN(lastIndex)) ? '0' : lastIndex + 1;  // This will be used for labeling the input container
        var identifierText = $(xml).find('ns1|Identifier').first().text();
        var titleText = $(xml).find('ns1|Title').first().text();
        var abstractText = $(xml).find('ns1|Abstract').first().text();

        // We ignore inputs with these titles since they're already plugged in by the user in previous steps
        if ($.inArray(identifierText, ['FEATURE_COLLECTION','FEATURE_ATTRIBUTE_NAME','DATASET_URI','DATASET_ID', 'TIME_START', 'TIME_END']) > -1) return '';

        // Outer wrapper per input
        var containerDiv = $(divString).attr('id', 'di_element_wrapper_' + index).addClass('di_element_wrapper');

        // Create the title, identifier and abstract text tooltip
        var identifierContainer = $(divString).addClass('di_identifier').html(identifierText);
        $(containerDiv).append(identifierContainer);
        if (abstractText) $(containerDiv).append(
            $('<a></a>').addClass('toolTip').attr('title', abstractText).append(
                $('<img></img>').attr({
                    'src' : 'images/question-mark.png',
                    'alt' : 'informational question mark'
                })
            )
        )
        $(containerDiv).append($(spanString).addClass('di_algorithm_input_title').html(titleText));
        if (!minOccurs) $(containerDiv).append($(spanString).html(' (Optional)'));

        // Create the input element
        var inputContainer = $(divString).addClass('di_field'); // wrapper

        if ($(xml).find('ComplexData').length) { // We found a complex type. Typically this will be a textbox to put a URL into.
            // First we add the mimetype, datatype and schema as a hidden field
            var complexMimeType = $(inputString).attr('type', 'hidden').attr('name', 'di_field_mimetype').attr('value', $(xml).find('Default>Format>MimeType').text());
            var complexData = $(inputString).attr('type', 'hidden').attr('name', 'di_field_datatype').attr('value', 'complex');
            var complexSchema = $(inputString).attr('type', 'hidden').attr('name', 'di_field_schema').attr('value', $(xml).find('Default>Format>Schema').text());
            // Add the input box
            var complexInputBox = $(inputString).attr('type', 'text').attr('size', '40').addClass('di_field_input');

            $(inputContainer).append(complexData);
            $(inputContainer).append(complexMimeType);
            $(inputContainer).append(complexSchema);
            $(inputContainer).append(complexInputBox);
        } else if ($(xml).find('LiteralData').length) { 
            // Find out what sort of datatype we have 
            var dataType = $(xml).find('ns1|DataType').first().attr('ns1:reference').split(':')[1];
            var defaultValue = $(xml).find('DefaultValue').text();
            if (dataType == 'boolean') {
                var literalCheckbox = $(inputString).
                    attr({
                        'type' : 'checkbox',
                        'name' : identifierText
                    }).
                    addClass('di_field_input');
                $(inputContainer).append(literalCheckbox);
                if (defaultValue.toLowerCase() == 'true') $(literalCheckbox).attr('checked','checked');
                else $(literalCheckbox).removeAttr('checked');
            } else if ($(xml).find('LiteralData').find('ns1|AnyValue').length) { // We have an input box
                var literalInputBox = $(inputString).attr('type', 'text').attr('size', '40').addClass(dataType).addClass('di_field_input');
                $(inputContainer).append(literalInputBox);
            } else  if ($(xml).find('LiteralData').find('ns1|AllowedValues').length) { // We have a listbox
                var literalListbox = $(selectString).addClass('di_field_input');

                $(xml).find('LiteralData').find('ns1|AllowedValues').find('ns1|Value').each(function(i,e){
                    $(literalListbox).append($(optionString).attr('value', $(e).text()).html($(e).text()));
                });

                if (maxOccurs > 1) literalListbox.attr('multiple', 'multiple');

                $(inputContainer).append(literalListbox);
            }
        } else if ($(xml).find('BoundingBoxData').length) {
        //TODO- We've got nothing - not sure how to work these yet
        }

        // Append the input container we just created to the main container div
        $(containerDiv).append(inputContainer);
        
        return containerDiv;
    }

    function createMiscOptionsJSON() {
        var optionsJSON = {};
        $('.di_element_wrapper').each(function(i,e){
            var inputId = $(e).find('.di_identifier').html();

            // For every select box...
            $(e).find('.di_field:has("select")').each(function(ind, ele){
                $(ele).find("option:selected").each(function (inex, element) {
                    var selectedOption = $(this).text();
                    if (optionsJSON[inputId]) optionsJSON[inputId].push(selectedOption);
                    else optionsJSON[inputId] = new Array(selectedOption);
                });
            });

            // For every text box
            $(e).find('.di_field:has("input[type="text"]")').each(function(ind, ele){
                var enteredText = $(ele).find('input[type="text"]').val();
                if (optionsJSON[inputId]) optionsJSON[inputId].push(enteredText);
                else optionsJSON[inputId] = new Array(enteredText);
            });
            
            // For every checkbox
            $(e).find('.di_field:has("input[type="checkbox"]")').each(function(ind, ele){
                var checked = $(ele).find('input[type="checkbox"]').is(':checked');
                if (optionsJSON[inputId]) optionsJSON[inputId].push(checked);
                else optionsJSON[inputId] = new Array(checked);
            });
        });
        return optionsJSON;
    }

    function bindDynamicInputElements() {
        $('.di_element_wrapper').each(function(i,e){
            
            // Bind all time fields as a calendar
            $(e).find('.di_field:has("input")').each(function(){
                if ($(this).find('.di_field_input').hasClass('dateTime')){
                    $(this).find('.di_field_input').datepicker({
                        'autoSize' : true,
                        'changeMonth' : true,
                        'changeYear' : true,
                        'defaultDate' : new Date(),
                        'duration' : 500,
                        'hideIfNoPrevNext': true
                    });
                }
            });
            
        });
    }

    function populateConfigurationSummary() {
        var configurationMap = {};
        
        // Populate the JSON object
        $('.di_element_wrapper').each(function(wrapperIndex, wrapperElement){
            
            // Create the key for each of our inputs
            var identifier = $(wrapperElement).find('.di_identifier').html();
            var idMapElement = new Array();
            

            // For each input element within each wrapper, add value to map
            $(wrapperElement).find('input:not([type="hidden"])').each(function(inputIndex, inputElement){
                if (inputElement.type == 'checkbox') idMapElement.push((inputElement.checked) ? 'true' : 'false');
                else idMapElement.push($(inputElement).val());
            })

            // For each selectbox
            $(wrapperElement).find('select:not([type="hidden"]) option:selected').each(function(selectedIndex, selectedElement){
                idMapElement.push($(selectedElement).text());
            })

            // Put the key element into the JSON object
            configurationMap[identifier] = idMapElement;
        })

        // Use the populated JSON object to create a configuration div
        var wrapperDiv = $('#algorithm_configuration_summary');
        $(wrapperDiv).html('');
        var table = $('<table></table>');
        $.each(configurationMap, function(key, value) {
            var row = $('<tr></tr>');
            $(row).append($('<td></td>').html(key + ":"))
            $(value).each(function(arrayIndex, arrayValue) {
                var td = $('<td></td>').html(arrayValue);
                $(row).append(td);
            })
            $(table).append(row);
        })
        $(wrapperDiv).append(table);

        $('#algorithm_configuration_summary').css('overflow', 'auto').css('height', $('#stepsContent').outerHeight(true) - $('#config_table').outerHeight(true) - 20);
        $('#submitForProcessingButton').attr('disabled', '');
    }

    function bindSubmitForProcessingButton() {
        $('#submitForProcessingButton').click(function() {
            var result = isReadyForSubmit();
            if (!result.ready) {
                $('#missing_input_summary').append($('<table></table>').attr('id', 'input_summary_table'));

                $.each(result, function(k,v){
                    if (k === 'ready') return true; // (this is the same as 'continue')
                    if (v.complete) {
                        $('#input_summary_table').append(
                            $('<tr></tr>').
                                append($('<td></td>').text(v.txt)).
                                append($('<td></td>').addClass('greenO').text('&#x2714;'.htmlDecode()))
                            )
                    } else {
                        $('#input_summary_table').append(
                            $('<tr></tr>').
                                append($('<td></td>').text(v.txt)).
                                append($('<td></td>').addClass('redX').text('X'))
                            )
                    }
                });

                $('#missing_input_summary').removeClass("hidden");
                $('#missing_input_summary').dialog({
                    modal: true,
                    title: 'Some inputs are missing.',
                    model: true,
                    buttons: {
                        Ok: function() {
                            $( this ).dialog( "close" );
                        }
                    },
                    close: function(event,ui) {
                        $('#missing_input_summary').html('');
                        $('#missing_input_summary').addClass('hidden');
                    }
                });
                return false;
            };
            
            $('#retrieveOutputButton').attr('disabled', 'disabled');
            $('#retrieveProcessInfoButtonButton').attr('disabled', 'disabled');
            $('#submitForProcessingButton').attr('disabled', 'disabled');

            var submitWpsAlgorithm = $('[name=di_algorithm_identifier]').attr('value');

            var featureType = AOI.getSelectedFeatureType();
            var attribute = AOI.getSelectedAttribute();
            var features = AOI.getSelectedFeatures();

            var datasetURL = Dataset.getSelectedDatasetURL();
            var datasetID = Dataset.getSelectedDatasetID();
            var fromDate = Dataset.getFromDate();
            var toDate = Dataset.getToDate();

            var miscOptions = createMiscOptionsJSON();

            var submitWpsStringInputs = {
                'FEATURE_ATTRIBUTE_NAME': attribute ? [attribute] : [],
                'DATASET_URI': datasetURL ? [datasetURL] : [],
                'DATASET_ID': datasetID ? datasetID : [],
                'TIME_START': fromDate ? [fromDate] : [],
                'TIME_END': toDate ? [toDate] : []

            };

            $.extend(submitWpsStringInputs, miscOptions, submitWpsStringInputs);

            // http://internal.cida.usgs.gov/jira/browse/GDP-172
            // Now we remove any inputs not found in the algorithm
            // First get all of the inputs from the process
            var processDescriptionInputsObject = new Object();
            $(WPS.processDescriptions[$('[name=di_algorithm_identifier]').attr('value')]).find("DataInputs Input ns1|Identifier").each(function(){
                processDescriptionInputsObject[$(this).text()] = {
                    'minOccurs' : $(this).parent().attr("minOccurs"), 
                    'maxOccurs' : $(this).parent().attr("maxOccurs")
                }
            });
            if ($.isEmptyObject(processDescriptionInputsObject)) {
                showErrorNotification('You have not completed the configuration for this algorithm.  Please review your configuration inputs and try again.');
                return false;
            }
            
            // Test each element of our input strings against what the process has. if it's not in there, wipe it
            $.each(submitWpsStringInputs, function(k,v){
                if (!processDescriptionInputsObject[k]) delete submitWpsStringInputs[k]
            });

            var wfsXML = createGetFeatureXML(featureType, attribute, features);
            var wfsWpsXML = WPS.createWfsWpsReference(Constant.endpoint.geoserver + "/wfs", wfsXML);
            var submitWpsXmlInputs = {'FEATURE_COLLECTION': [wfsWpsXML]}
            var submitWpsOutput = ['OUTPUT'];
            var processingWPS = Constant.endpoint.proxy + Constant.endpoint.processwps;

            // http://internal.cida.usgs.gov/jira/browse/GDP-178
            var missing = [];
            var present = [];

            $.each(processDescriptionInputsObject, function(k,v){ //
                var stringInput = (submitWpsStringInputs[k] != undefined && submitWpsStringInputs[k].length > 0);
                var xmlInput = (submitWpsXmlInputs[k] != undefined && submitWpsXmlInputs[k].length > 0);
                if (parseInt(v.minOccurs) > 0) {
                    if (stringInput || xmlInput) present.push(k);
                    else missing.push(k);
                }
            });

            if (missing.length > 0) {
                // Create the dialog
                $('#missing_input_summary').append(
                    $('<table></table>').attr('id', 'input_summary_table')
                    );
                $.each(present, function(i,v){
                    $('#input_summary_table').append(
                        $('<tr></tr>').
                        append($('<td></td>').text(v)).
                        append($('<td></td>').addClass('greenO').text('&#x2714;'.htmlDecode()))
                        )
                });
                $.each(missing, function(i,v){
                    $('#input_summary_table').append(
                        $('<tr></tr>').
                        append($('<td></td>').text(v)).
                        append($('<td></td>').addClass('redX').text('X'))
                        )
                });
                $('#missing_input_summary').removeClass("hidden");
                $('#missing_input_summary').dialog({
                    modal: true,
                    title: 'Some inputs are missing.',
                    model: true,
                    buttons: {
                        Ok: function() {
                            $( this ).dialog( "close" );
                        }
                    },
                    close: function(event,ui) {
                        $('#missing_input_summary').html('');
                        $('#missing_input_summary').addClass('hidden');
                    }
                });
                return false;
            }

            WPS.sendWpsExecuteRequest(processingWPS, submitWpsAlgorithm, submitWpsStringInputs, submitWpsOutput, true, submitForProcessingCallback, submitWpsXmlInputs);
        });
    }
    
    function bindRetrieveOutputButton() {
        $('#retrieveOutputButton').click(function() {
            $('#submitForProcessingButton').removeAttr('disabled');
            var urlAndData = retrieveOutputURL.split('?');
            //$.download(Constant.endpoint.proxy + urlAndData[0], urlAndData[1], 'get');
			$.download(urlAndData[0], urlAndData[1], 'get');
        });
    }

    function bindRetrieveProcInfoButton(statusID) {
        $('#retrieveProcessInfoButtonButton').click(function(){
            var urlAndData = retrieveOutputURL.split('?');
            $.download(Constant.endpoint.proxy + urlAndData[0],'id=' + statusID + '&attachment=true', 'get');
        });
    }
    
    return {
        htmlID: _htmlID,
        init: function() {
//            logger.debug("GDP: Initializing Submit.");
//            bindSubmitForProcessingButton();
//
//            bindRetrieveOutputButton();
//
//            initializeEmailVerification();
//            
//            // Set up the type of algorithm configuration view the user will see based on whats in config.jsp
//            // See http://internal.cida.usgs.gov/jira/browse/GDP-194
//            var singleAlgorithm = Constant.ui.view_algorithm_single;
//            if (singleAlgorithm.length > 0) setupSingleAlgorithmView(singleAlgorithm);
//            else populateAlgorithmDropdownView();
        },
        // A hook for when a step is appearing on the page
        stepLoading: function() {
            // If we have a single algorithm view and there's configuration to be had,
            // pop the config up automatically for the user when we load the submit page
            // but only the first time the Submit section appears
            if (Constant.ui.view_algorithm_single.length > 0 
                && $('#configure_algorithm_link') 
                && !configured) {
                // TODO- We really need a better way to check 
                // whether or not an algorithm needs config. 
                // Nail down the method we use to figure out to 
                // see whether a method is configurable and slap
                // that onto the Algorithm object
                $('#configure_algorithm_link').click();
            }
            return true;
        }
    };
};
*/