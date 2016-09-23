/*
 This code implements the XDM API for use within item preview app.
 */

(function (XDM, CM) {

    // we load one page in advance, but we don't want that to cause a cascade of page show/load
    Blackbox.getConfig().preventShowOnLoad = false;
    //Adding this onto TDS for now so it is available in the dictionary handler.
    var irisUrl = location.href;
    var buttonsLoaded = false;
    CM.accessibilityEnabled = true;
    // Functions that are used by toolbar buttons

    //Calculator
    var calculatorBtn = function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage) {
            Calculator.toggle();
        }
    };

    //Global Notes
    var globalNotesBtn = function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage && TDS.Notes) {
            TDS.Notes.open();
        }
    };

    //Masking
    var showMask = function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage) {
            Masking.toggle();
        }
    };

    var dictionaryBtn = function(ev) {
        var currentPage = ContentManager.getCurrentPage();
        if (currentPage) {
            Dictionary.toggle();
        }
    };


    // setup cross domain api
    XDM.init(window);



    function getItemId(item) {
        return "I-" + item.bankKey + "-" + item.itemKey;
    }

    function getItemMap(requestedItems) {
        var distinctItemCount = 0;

        var itemMap = requestedItems.reduce(function (map, item) {
            ++distinctItemCount;
            map[getItemId(item)] = item;
            return map;
        }, {});

        if (requestedItems.length !== distinctItemCount) {
            throw new Error('One or more of the requested items appears multiple times in this request.');
        }

        return itemMap;
    }

    function getExistingPage(requestedItems) {

        var requestedItemCount = Object.keys(requestedItems).length,
            partialMatches = false,
            matchedPage = null,
            matchedItems = null;

        // go through each page to try matching items
        CM.getPages().forEach(function (page) {
            var items = page.getItems(),
                matches = [];

            // check this page for items which are in the current content request
            items.forEach(function (item) {
                var itemId = getItemId(item),
                    matchedItem = requestedItems[itemId];

                if (matchedItem) {
                    matches.push({
                        loaded: item,
                        requested: matchedItem
                    });
                }
            });

            if (matches.length === items.length && items.length === requestedItemCount) {
                // exact match, save the page and items
                matchedPage = page;
                matchedItems = matches;
            } else if (matches.length) {
                // only some items matched
                partialMatches = true;
            }
        });

        if (partialMatches) {
            throw new Error('One or more of the items requested have already been loaded. Make sure the content request is the same as the orginal (e.g. it can\'t contain different response or label values).');
        }

        return {
            page: matchedPage,
            itemPairs: matchedItems
        };
    }

    function loadContent(xmlDoc) {
        if (typeof xmlDoc == 'string') {
            xmlDoc = Util.Xml.parseFromString(xmlDoc);
        }

        // create array of content json from the xml
        var deferred = $.Deferred();
        var contents = CM.Xml.create(xmlDoc);
        var content = contents[0];

        var itemMap = getItemMap(content.items);
        var result = getExistingPage(itemMap);

        //if the page is already loaded we want to force a reload because the accommodations may have changed.
        if (result.page) {
            // show the page
            TDS.Dialog.hideProgress();
            ContentManager.removePage(result.page);
            // If there is a word list loaded clear the cached words because they may have changed.
            if(window.WordListPanel){
                window.WordListPanel.clearCache();
            }
        }

        page = CM.createPage(content);

        page.render();
        page.once('loaded', function () {
            TDS.Dialog.hideProgress();
            page.show();
            CM.accessibilityEnabled = false;
            deferred.resolve();
        });

        if(!buttonsLoaded) {
            Blackbox.showButton('btnMask', showMask, true);
            Blackbox.showButton('btnCalculator', calculatorBtn, true);
            Blackbox.showButton('btnGlobalNotes', globalNotesBtn, true);
            buttonsLoaded = true;
        }
        if (TDS.getAccommodationProperties().getDictionary()) {
            Blackbox.showButton('btnDictionary', dictionaryBtn, true);
        }

        var printSize = CM.getAccProps().getPrintSize();
        if(printSize) {
            CM.getZoom().setLevel(printSize, true);
        } else {
            CM.getZoom().setLevel(0, true);
        }

        return deferred.promise();
    }

    //function that is passed to Blackbox.changeAccommodations to modify the accommodations
    //in our case we just want to clear out any accommodations that are set.
    function clearAccommodations(accoms) {
        accoms.clear()
    }

    //parses any accommodations from the token, and sets them on the Blackbox.
    function setAccommodations(token) {
        var parsed = JSON.parse(token);
        //Call changeAccommodations once to reset all accommodations to their default values
        Blackbox.changeAccommodations(clearAccommodations);
        if(parsed.hasOwnProperty('accommodations')) {
            Blackbox.setAccommodations(parsed['accommodations']);
            //Call changeAccommodations a second time to apply the new accommodations that were set
            //by setAccommodations
            Blackbox.changeAccommodations(function(accoms){})
        }
    }

    function loadToken(vendorId, token) {
        Messages.set('TDS.WordList.illustration', 'Illustration', 'ENU');
        TDS.Dialog.showProgress();
        var url = irisUrl + '/Pages/API/content/load?id=' + vendorId;
        setAccommodations(token);
        return $.post(url, token, null, 'text').then(function (data) {
            return loadContent(data);
        }, function (data) {
            window.alert("Unable to load item.\n" +
                "Please make sure you entered the correct bank and item numbers.")

        });
    }

    XDM.addListener('IRiS:loadToken', loadToken);

    Blackbox.events.on('ready', function () {
        XDM(window.parent).post('IRiS:ready');
    });

})(window.Util.XDM, window.ContentManager);