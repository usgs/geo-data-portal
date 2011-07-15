<%-- ########### MODAL DIALOGS #####################--%>
<div id="algorithm-documentation" class="hidden"></div>

<div id="algorithm-dynamic-container-content" class="hidden"></div>

<div id="missing-input-summary" class="hidden"></div>

<div id="submit-dialog" class="hidden">
    <div id="algorithm-configuration-summary" class="element_wrapper"></div>
    <div class="element-wrapper">
        <div id="submit-dialog-info-text"></div>

        <div id="submit-dialog-email-entry">
            <label for="user-email">E-Mail: </label><input type="text" name="user-email" id="user-email" />
        </div>
    </div>

</div>
<%-- ###############################################--%>

<div id="dataset">
    <div class="step-header hidden">Configure / Submit</div>
    <div class="step-content hidden centered">

        
        <div id="algorithm-config-div" >
            <span id="algorithm-dropdown-container">
                <select id="algorithm-dropdown"></select>
            </span>
            <span id="algorithm-buttons">
                <span id="algorithm-documentation-link" class="button"></span>
                <span id="configure-algorithm-link" class="hidden button"></span>
            </span>
        </div>

        <div id="csw-client-div"  class="hidden">
            <table id="dataset-table">
                <%-- Search Query--%>
                <tr class="csw-dialog-tablerow" id="csw-url-input-row">
                    <td>
                        <a class="tooltip" id="csw-server-tooltip" title="Enter a CSW server">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td>
                        CSW Server:
                    </td>
                    <td>
                        <input type="text" id="csw-url-input-box" size="62" />
                        <span id="csw-host-set-button" class="button" value="Set"></span>
                    </td>
                </tr>
                <tr class="csw-dialog-tablerow">
                    <td>
                        <a class="tooltip" title="Enter a search query to limit
                            the datasets listed. This search will query the
                            metadata record associated with the dataset
                            displayed.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td id="csw-wrapper" colspan="2"></td>
                </tr>
                <tr id="dataset-url-input-row" class="csw-dialog-tablerow">
                    <%-- Dataset URL --%>
                    <td>
                        <a class="tooltip" title="Resources in the GDP catalog
                            are associated with a GDP compatible data transport
                            standard for analysis and potentially a web map service
                            (WMS) for visualization. Upon selection of a dataset,
                            the dataset URL will be populated and the sever will be
                            queried for available dataset IDs. Alternatively, you
                            can enter any OPeNDAP (dods://url/servicePath) url or a
                            WCS endpoint (http://url/servicePath?).">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td>
                        <span>Dataset URL:</span>
                    </td>
                    <td>
                        <input type="text" id="dataset-url-input-box" size="62" />
                        <span id="select-dataset-button" class="button"></span>
                    </td>
                </tr>

                <tr id="dataset-selected-title-row">
                    <td colspan="4">
                        <div id="dataset-selected-title"></div>
                    </td>
                </tr>

                <%-- Dataset ID --%>
                <tr id="dataset-id-row">
                    <td>
                        <a id="dataset-id-tooltip" class="tooltip hidden" title="
                           Select the data type of interest from the available list.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td>
                        <span id="dataset-id-label" class="hidden">Select Datatype:</span>
                    </td>
                    <td>
                        <div id="datasetId" class="hidden">
                            <select id="dataset-id-selectbox" multiple size="5" ></select>
                        </div>
                    </td>
                </tr>

                <tr id="wms-id-row">
                    <%-- WMS ID --%>
                    <td>
                        <a id="wms-tooltip" class="tooltip hidden" title="The GDP
                           will attempt to match the dataset ID and the WMS layer
                           name. <br /><br />If a match is found the WMS will
                           display.<br /><br />If no match is found, you will be
                           able to choose a WMS layer from the available list. ">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td>
                        <span id="wms-label" class="hidden">WMS:</span>
                    </td>
                    <td>
                        <select id="wms-layer-selectbox" class="hidden"></select>
                    </td>
                </tr>

                <tr id="date-pickers-table" class="hidden">
                    <td>
                        <a id="date-picker-tooltip" class="tooltip hidden" 
                           title="Enter the date range of interest in the format shown or
                           use the calendar to select your starting and ending dates of interest.">
                            <img src="images/question-mark.png" alt="informational question mark"/>
                        </a>
                    </td>
                    <td>
                        Select Date Range:
                    </td>
                    <td>
                        From: <input id="date-range-from-inputbox" type="text"/>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        To: <input id="date-range-to-inputbox" type="text"/>
                    </td>
                </tr>
            </table>
        </div>

        <div>            
            <span id="submit-button-div"><span id="submit-for-processing-link" class="hidden button"></span></span>            
            <span id="process-buttons">
                <span id="retrieve-output-button" class="hidden button"></span>
                <span id="retrieve-process-info-button" class="hidden button"></span>
            </span>
        </div>
    </div>
</div>