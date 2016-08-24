## IRiS Accommodations within the Item Viewer Service. 

#### Zoom and color contrast.
- [ ] The font size starts at the maximum size.
- [ ] Clicking the Zoom Out button decreases the zoom.
- [ ] Clicking the Zoom In button increases the zoom.
- [ ] The text is yellow.
- [ ] The background is blue.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-856?isaap=TDS_CCYellowB;TDS_PS_L4


#### Word List Glossary and Illustration Glossary.
- [ ] Clicking the word spinner opens up the glossary panel.
- [ ] There is a picture of the spinner in the glossary panel.
- [ ] There is a tab with the Korean definition in the glossary panel.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-1881?isaap=TDS_WL_KoreanGloss;TDS_WL_Illustration;TDS_ILG1


#### Spanish translation for items and the tools menu.
- [ ] There is a Spanish translation of the item.
- [ ] The tools menu accessed by right clicking or with the button is in Spanish.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-1844?isaap=TDS_ITM1;TDS_Highlight1;TDS_ST1;ESN


#### Streamlined mode.
- [ ] The item layout uses the streamlined mode which puts everything in one column.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-2576?isaap=TDS_SLM1


#### Item tools menu, highlighting, strikethrough, and masking.
- [ ] Right clicking brings up the item tools menu.
- [ ] Clicking the tools menu button brings up the tools menu.
- [ ] Highlighting text then right clicking or clicking the tools menu button brings up the option to highlight the selection.
- [ ] Right clicking again or clicking the tools menu button brings up the option to reset highlighting.
- [ ] Right clicking or clicking the tools menu button provides the option for strikethrough.
- [ ] Clicking on one of the answers with strikethrough mode on puts a line across the answer.
- [ ] Clicking on the tools menu or in the text area turns strikethrough mode off.
- [ ] While strikethrough mode is enabled, clicking on an answer that has the strikethrough line on it removes the line.
- [ ] Clicking the Masking button turns on the masking tool.
- [ ] While the masking tool is on clicking and dragging creates a mask.
- [ ] Clicking the Masking button again turns off the masking tool.
- [ ] Clicking the (x) on a mask closes the mask.
- [ ] Clicking the American Sign Language option in the tools menu opens the American Sign Language video.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-1844?isaap=TDS_ITM1;TDS_Highlight1;TDS_ST1;TDS_Masking1;TDS_ASL1


#### Expandable Passages
- [ ] Clicking the expand button in the top right corner of the passage expands it.
- [ ] Clicking the expand button when the passage is expanded shrinks it back to normal size.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-1437?isaap=TDS_ExpandablePassages1


#### Calculator, Notes, Dictionary, and Font Type
- [ ] The font used is Verdana. This is a sans serif font.
- [ ] Clicking the calculator button opens the calculator.
- [ ] The calculator can be used to make calculations.
- [ ] Clicking the (x) in the top right corner of the calculator pane closes it.
- [ ] Clicking the notes button opens the notes panel.
- [ ] Typing in the notes panel creates text.
- [ ] Clicking Save and Close saves the text and closes the pane.
- [ ] Clicking cancel closes the panel without saving any text that was entered or deleted.
- [ ] Clicking the dictionary button opens the dictionary pane.
- [ ] If the dictionary is set up then an interactive dictionary is displayed.
- [ ] If the dictionary is not set up a 404 page is displayed.
- [ ] Clicking the (x) in the top right corner of the dictionary pane closes it.

Go to: http://itemviewerservice.cass.oregonstate.edu/item/187-2576?isaap=TDS_CalcSciInv;TDS_GN1;TDS_Dict_SD3;TDS_FT_Verdana


## Status Levels
Status levels are cumlative. Tests for level zero should also pass on level one and so forth.

#### Level Zero
 - [ ] Timestamp in RFC 3339 format https://www.ietf.org/rfc/rfc3339.txt
 - [ ] unit label is set
 - [ ] level is 0
 - [ ] status rating is set
 - [ ] status text is set

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=0


#### Level One
 - [ ] System element exists
 - [ ] System element has status rating and status text
 - [ ] Memory element has total memory, available memory, swap total, and swap used
 - [ ] Filesystems element exists
 - [ ] File system information elements
 - [ ] File system elements list free space, mount point, precent free space, total space and file system type

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=1

#### Level Two
 - [ ] Configuration element exists
 - [ ] Configuration element has status rating and status text
 - [ ] Element listing the S3 content bucket exists, or an error element warning that it is not configured
 - [ ] Element listign the AWS region exists, or an error element warning that it is not configured

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=2



#### Level Three
 - [ ] Database element exists
 - [ ] Database element has status rating and status text
 - [ ] Boolean element lists if the db exists
 - [ ] Boolean element lists if the db is writeable
 - [ ] If either of the abolve boolean elements is false a warning element exists

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=3



#### Level Four
 - [ ] Element listing the results of attempting to write a new file to the content database exists.
 - [ ] Element listing the results of attempting to remove a file from the content database exists.
 - [ ] If there was an error writing a file to the content database an error element appears
 - [ ] If there was an error removing a file from the content database an error element appears

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=4



#### Level Five
 - [ ] Providers element exists
 - [ ] Providers element has status rating and status text
 - [ ] Element listing amazon S3 connection status exists, or an error if the connection could not be made.
 - [ ] Element listing the item viewer service http status exists
 - [ ] Element listing the blackbox http status exists
 - [ ] Element listing the word list handler http status exists

Go to: http://itemviewerservice.cass.oregonstate.edu/status?level=5
