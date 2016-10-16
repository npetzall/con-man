[![Build Status](https://travis-ci.org/npetzall/con-man.svg?branch=master)](https://travis-ci.org/npetzall/con-man)
# Configuration Manager AKA Con-Man

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
    "env": "default",
    "value": "mylittlevalue"
  },{
    "service": "mylittleservice",
    "key": "myconfigurationkeyTwo",
    "env": "default",
    "value": "mylittlevalue"
  }
]
```

GET /config/{service}?env=custom
response APPLICATION_JSON:
```
[
  {
    "service": "mylittleservice",
    "key": "myconfigurationkey",
    "env": "custom",
    "value": "mylittlevalue"
  },{
    "service": "mylittleservice",
    "key": "myconfigurationkeyTwo",
    "env": "custom",
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
  "env": "default",
  "value": "mylittlevalue"
}
```

GET /config/{service}/{key}?env=custom
response APPLICATION_JSON:
```
{
  "service": "mylittleservice",
  "key": "myconfigurationkey",
  "env": "custom",
  "value": "mylittlevalue"
}
```

GET /config/{service}/{key}/value 
response TEXT_PLAIN:  
`[env default value]`

GET /config/{service}/{key}/value?env=custom
response TEXT_PLAIN:
`[env custom value]`

PUT /config/{service}/{key}  
request body TEXT_PLAIN:  
`[env default value]`

PUT /config/{service}/{key}?env=custom
request body TEXT_PLAIN:
`[env custom value]`

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

GET /config/{service}/{key}?qualifier
GET /config/{service}/{key}?env&qualifier

GET /config/{service}/{key}/value?qualifier
GET /config/{service}/{key}/value?env&qualifier

PUT /config/{service}/{key}?qulifier
PUT /config/{service}/{key}?env&qulifier


DELETE /config/{service}/{key}
DELETE /config/{service}/{key}?env&qualifier

GET /possible/{service}/{key}
PUT /possible/{service}/{key}
