# Configuration Mananger AKA Con-Man

## Configuration
conman-config.yaml check dropwizard.io configuration.

Default to H2 db, 8080 for service and 8081 for admin.

## Current

GET /config/{service}
response APPLICATION_JSON:
```
[
  {
    "service": "mylittleservice",
    "key": "myconfigurationkey",
    "value": "mylittlevalue"
  },{
    "service": "mylittleservice",
    "key": "myconfigurationkeyTwo",
    "value": "mylittlevalue"
  }
]
```
GET /config/{service}/{key}
response APPLICATION_JSON:  
```
{
  "service": "mylittleservice",
  "key": "myconfigurationkey",
  "value": "mylittlevalue"
}
```
GET /config/{service}/{key}/value 
response TEXT_PLAIN:  
`[value]`

PUT /config/{service}/{key}  
request body TEXT_PLAIN:  
`[value]`

GET /possible/{service}
response APPLICATION_JSON:
```
[
  {
    "description": "this is a possible configuratio",
    "valueRestriction": "true,false",
    "valueRestrictionType": "csv",
    "service": "mylittleservice",
    "key": "newKey"
  },{
    "description": "this is a possible configuratio",
    "valueRestriction": "true,false",
    "valueRestrictionType": "csv",
    "service": "mylittleservice",
    "key": "newKey"
  }
]
```

GET /possible/{service}/{key}
response APPLICATION_JSON:
```
{
  "description": "this is a possible configuratio",
  "valueRestriction": "true,false",
  "valueRestrictionType": "csv",
  "service": "mylittleservice",
  "key": "newKey"
}
```

PUT /possible/{service}/{key}  
request body APPLICATION_JSON:
```
{
	"description":"this is a possible configuratio",
	"valueRestriction":"true,false",
	"valueRestrictionType":"csv"
}
```

GET /possible/{service}/{key}/description  
response body TEXT_PLAIN:  
`[description]`

PUT /possible/{service}/{key}/description  
request body TEXT_PLAIN:  
`[description]`

GET /possible/{service}/{key}/valueRestriction  
response body TEXT_PLAIN:  
`[valueRestriction]`

PUT /possible/{service}/{key}/valueRestriction  
request body TEXT_PLAIN:  
`[valueRestriction]`

GET /possible/{service}/{key}/valueRestrictionType  
response body TEXT_PLAIN:  
`[valueRestrictionType]`

PUT /possible/{service}/{key}/valueRestrictionType  
request body TEXT_PLAIN:  
`[valueRestrictionType]`

## Future

GET /config/{service}/{key}?version&env&qualifier  
GET /config/{service}/{key}/value?version&env&qualifier  

PUT /config/{service}/{key}?version&env&qulifier  
DELETE /config/{service}/{key}  
DELETE /config/{service}/{key}?version&env&qualifier

GET /possible/{service}/{key}?version&env  
PUT /possible/{service}/{key}?version&env  
