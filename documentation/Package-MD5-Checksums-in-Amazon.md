Amazon does not provide the MD5 checksum for files stored on S3. The checksum is required to verify content packages downloaded from S3 are not corrupted. Amazon's recommendation is to add an MD5 checksum field to the item's metadata. This requires whoever uploads the package to set the metadata. https://aws.amazon.com/premiumsupport/knowledge-center/data-integrity-s3/

The Smarter Balanced Item Viewer calculates the MD5 checksum for the content pack zip file after it downloads it from Amazon S3 and compares it to the value stored in the "md5" field of the custom Amazon metadata for the file. The value in the Amazon metadata field needs to be the 32 character hexadecimal representation of the MD5 checksum or the comparison will fail. This is the default format provided by [Microsoft's FCIV tool](https://support.microsoft.com/en-us/kb/841290) and the [md5sum command](http://linux.die.net/man/1/md5sum) in Linux.

To add the required header field to a content pack loaded into S3.
1. Select the content package zip file in S3  
2. From the "Actions" drop down menu select "Properties"  
3. Expand the "Metadata" area  
4. Click "Add more metadata"  
5. In the key field enter "x-amz-meta-md5" without quotes  
6. In the value field enter the 32 character hexadecimal representation of the file's MD5 hash  
7. Save the new key value pair  