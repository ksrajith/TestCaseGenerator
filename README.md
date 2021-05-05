# TestCaseGenerator
Can generate test resutls for configed data

Simple clone the project to your machine.
Goto config file (configs.json):
src/main/java/com/api/con/configs.json

If the request has dependancy with pre-request, then user has to enable
_isPreRequest_ config as true.
If you do not have that dependacy then simply make it as false and no need to worry about 
_preSendJson_ configs. (This only reads if the _isPreRequest_ is true)
And fill the request related info

"preSendJson":{
    "jsonPre": <JSON Body>,
    "preRequestType":<POST or PUT>,
    "preContentType": <Content type>,
    "preRequestUrl": <Request Url>,
    "preHeaders": <Header array header name and value> 
  }
  

 "preSendJson":{
    "jsonPre":["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday"],
    "preRequestType":"POST",
    "preContentType": "JSON",
    "preRequestUrl":"http://localhost:3030/api/test",
    "preHeaders":[ {"header": "Abc", "value": "test"}, {"header": "PQR", "value": "43534"}]
  }
  
  
  Now you need to add data on your main request JSON
  
  Add the JSON
  "jsonMain": {"test":"value", "age":12, "address":"Sri Lanka"}
  "mainRequestType":<POST or PUT>,
  "mainContentType":<Content type>,
  "mainRequestUrl": <Request Url>,
  "mainHeaders": <Header array header name and value>
  "setFromPreResponse": <If this request need to add data from pre-request mapping should mention in here> 
  
  Example:
  [{"pathOrName": "RESPONSEHED","isFromPreHeader": true, "updateKeyPath": "RESPONSEHEDTMP", "isToMainHeader": true}]
  pathOrName - request header name or json path
  isFromPreHeader - Is mentioned pathOrName should take from pre-request Response header
  updateKeyPath - main request header name or json path which value need to be update
  isToMainHeader - Is mentioned updateKeyPath is in header or JSON Body
  
  
  "constructJsonForEachField": <If request need to send for eanch field mention in JSON body> 
  if user need to build JSON variations with assigning null value to each field put parameter "NULL_CHECK"
  if user need to build JSON variations with assigning empty ("") value to each field put parameter "EMPTY_CHECK"
  if user need to build JSON variations with removing each field put parameter "REMOVE_FIELD"

If the user need to build the JSONs to all above combinations:
"constructJsonForEachField": ["REMOVE_FIELD", "EMPTY_CHECK", "NULL_CHECK"],

Also user can build JSONs only for configed fields under _modifyJson_
Example:
If the user need to send a JSON assigning NULL values to "age", "address"
user need to enable the config _isIgnore_ as false (Then config will read else this is ignored)
 "modifyType": "NULL_CHECK" (Since ucer need to assigine NULL value)
 "statusCode": expected response code if available
 "responsePayLoad": expected response payload if available
 "isStrictCompare": if need to compare payload as one to one mapping make this as true,
                    if the user need to verify given values in the payload then this should mark as false

{
      "fields":[
        "age", "address"
      ],
      "isIgnore": false,
      "modifyType": "NULL_CHECK",
      "statusCode":400,
      "responsePayLoad":"{\n    \"task\": \"Task 51\",\n}",
      "isStrictCompare":false
    },


Under modify payload has "ONLY_USE" option which can use to test mandatory (Required) fields.
(JSON bulds using mentioned fields only)

User can add the testcases with length check parameters and value update paramenters.
lengthCheck, valueUpdate

For lengthCheck: generate values for below options
numNChars: Numbers and Chars
charOnly: chars only
numOnly: numbers only

To enable this config user need to make the _isIgnore_ as false

In valueupdate JSON path values will update with the given value


**Limitations**

This version supports to POST and PUT Methods
