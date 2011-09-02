/*
 *
 * The original source was contributed by:
 * 
 *
* File : CSWClient.js
* Author : Rob van Swol
* Organisation: National Aerospace Laboratory NLR
* Country : The Netherlands
* email : vanswol@nlr.nl
* Description: Simple AJAX based CSW client 
* Tested on : FireFox 3, Safari, IE 7
* Last Change : 2008-10-22
 * 
 * The original tool can be found at:
 * http://gdsc.nlr.nl/gdsc/en/tools/excat/excat_download_and_installation
 * 
*/

Ext.ns("GDP");

GDP.CSWClient = function() {
	
	function loadDocument(uri) {

        var xml = Sarissa.getDomDocument();
        var xmlhttp = new XMLHttpRequest();
        xml.async = false;
        xmlhttp.open("GET", uri, false);
        xmlhttp.send('');
        xml = xmlhttp.responseXML;
        return xml;
    }
	
	function setXpathValue(_a,_b,_c) {

        var _e=_a.selectSingleNode(_b);
		if (typeof _c === 'string') {
			if(_e){
				if(_e.firstChild){
					_e.firstChild.nodeValue=_c;
				}else{
					var dom=Sarissa.getDomDocument();
					var v=dom.createTextNode(_c);
					_e.appendChild(v);
				}
				return true;
			}else{
				return false;
			}
		} else if (_c.firstChild) {
			Sarissa.copyChildNodes(_c.firstChild, _e);
		}
        return false;
    }
	
	function parseXml(xmlString) {
		var result;
		if (window.ActiveXObject) {
			// IE
			result = new ActiveXObject('Msxml2.DOMDocument.6.0');
			result.loadXML(xmlString);
		} else {
			result = (new DOMParser()).parseFromString(xmlString, "text/xml");
		}
		return result;
	}
	
	function sendCSWRequest(config)	{
		config = config || {};
		
		if (config.host && config.request) {
			var host = config.host;
			var params = config.request;
			
			var xml = Sarissa.getDomDocument();
			xml.async = false;
			var xmlhttp = new XMLHttpRequest();

			xmlhttp.open("POST", host, false);
			xmlhttp.setRequestHeader("Content-type", "application/xml");
			xmlhttp.setRequestHeader("Content-length", params.length);
			xmlhttp.setRequestHeader("Connection", "close");
			xmlhttp.send(params); // POST

			xml = xmlhttp.responseXML;
			return xml;
		} else {
			return undefined;
		}
    }
	
	function handleCSWResponse(config) {
		config = config || {};
		
		if (config.response) {
			var xml = config.response;
			var xslt = config.stylesheet;
			
			var processor = new XSLTProcessor();
			processor.importStylesheet(xslt);

			var XmlDom = processor.transformToDocument(xml);
			var serializer = new XMLSerializer();
			var output = serializer.serializeToString(XmlDom.documentElement);

			return output;
		} else {
			return undefined;
		}
    }
	
	return function(config) {
		config = config || {};
		
		var getRecordsReq = config.getRecordsRequestXsl || "js/excat/xsl/getrecords.xsl";
		var getRecordsResp = config.getRecordsResponseXsl || "js/excat/xsl/csw-results.xsl";
		
		var getRecordByIdReq = config.getRecordByIdRequestXsl || "js/excat/xsl/getrecordbyid.xsl";
		var getRecordsByIdResp = config.getRecordByIdResponseXsl || "js/excat/xsl/csw-metadata.xsl";
		
		var prettyXmlResp = config.XmlResponseXsl || "js/excat/xsl/prettyxml.xsl";
		
		var defaultsPath = config.defaultsXml || "js/excat/xml/defaults.xml";
		var cswhost = config.cswhost || "geonetwork";
		
		this.getrecords_xsl = loadDocument(getRecordsReq);
		this.getrecordbyid_xsl = loadDocument(getRecordByIdReq);
		this.defaults_xml = loadDocument(defaultsPath);
		this.getRecordsStylesheet = loadDocument(getRecordsResp);
		this.getRecordsByIdStylesheet = loadDocument(getRecordsByIdResp);
		this.xmlStylesheet = loadDocument(prettyXmlResp);
		this.defaultschema = this.defaults_xml.selectSingleNode("/defaults/outputschema/text()").nodeValue;
		
		this.host = cswhost;
		
		this.getRecordById = function(config) {
			config = config || {};
			
			if (config.id) {
				var id = config.id;
				var outputDivId = config.outputId || "metadata";

				var schema = config.schema || this.defaultschema;

				setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
				setXpathValue(this.defaults_xml, "/defaults/id", id + '');

				var processor = new XSLTProcessor();
				processor.importStylesheet(this.getrecordbyid_xsl);

				var request_xml = processor.transformToDocument(this.defaults_xml);
				var request = new XMLSerializer().serializeToString(request_xml);

				var csw_response = sendCSWRequest({host : this.host, request: request});
				var output = handleCSWResponse({response : csw_response, stylesheet : this.getRecordsByIdStylesheet});
				var outputDiv = document.getElementById(outputDivId);
				outputDiv.innerHTML = output;
			}
			
		};
		
		this.getRecords = function(config) {
			config = config || {};
			
			var outputDivId = config.outputId || "csw-output";
			var start = config.start || 1;
			var sortby = config.sortBy || "title";

			var property = "keyword";
            /*because geonetwork doen not follow the specs*/
            if(cswhost.indexOf('geonetwork') !=-1 & property == "anytext") {
				property = "any";
			}

            var operator = config.operator || "contains";
            var query = '';
			var literals = config.query || ["National"];
            for (var i = 0; i < literals.length; i++) {
				var literal = literals[i];
				if (literal && literal !== '') {
					if (operator == "contains" & literal != "") {
						literal = "%" + literal + "%";
					}
					query += '<constraint><propertyname>';
					query += property;
					query += '</propertyname><literal>';
					query += literal;
					query += '</literal></constraint>';
				}
			}
			
//			if (query !== '') {
				query = '<query>' + query + '</query>';
				query = parseXml(query);
				setXpathValue(this.defaults_xml, "/defaults/query", query);
//			}

            var schema = config.schema || this.defaultschema; 
            setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
            setXpathValue(this.defaults_xml, "/defaults/startposition", start + '');
            setXpathValue(this.defaults_xml, "/defaults/sortby", sortby + '');

            var processor = new XSLTProcessor();
            processor.importStylesheet(this.getrecords_xsl);

            var request_xml = processor.transformToDocument(this.defaults_xml);
            var request = new XMLSerializer().serializeToString(request_xml);

            var csw_response = sendCSWRequest({host : this.host, request : request});
            var results = "<results><request start=\"" + start + "\"";
            results += " maxrecords=\"";
            results += this.defaults_xml.selectSingleNode("/defaults/maxrecords/text()").nodeValue;
            results += "\"/></results>";

            var results_xml = parseXml(results);

            var importNode = results_xml.importNode(csw_response.documentElement, true);
            results_xml.documentElement.appendChild(importNode);
			
			var output = handleCSWResponse({response: results_xml, stylesheet : this.getRecordsStylesheet});
			var outputDiv = document.getElementById(outputDivId);
			outputDiv.innerHTML = output;
        }
	}
}();