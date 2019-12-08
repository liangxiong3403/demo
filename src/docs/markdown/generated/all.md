# demo


<a name="overview"></a>
## Overview
swagger for demo


### Version information
*Version* : 1.0.0.RELEASE


### Contact information
*Contact* : liangxiong  
*Contact Email* : 1071608617@qq.com


### License information
*License* : Apache License, Version 2.0  
*License URL* : https://www.apache.org/licenses/LICENSE-2.0.html  
*Terms of service* : https://github.com/dyc87112/spring-boot-starter-swagger


### URI scheme
*Host* : localhost  
*BasePath* : /


### Tags

* 首页 : Index Controller




<a name="paths"></a>
## Paths

<a name="hellousingpost"></a>
### 读取用户数据
```
POST /hello
```


#### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Header**|**name one**  <br>*required*|some description one|string|
|**Query**|**houseId**  <br>*optional*||string|
|**Query**|**location**  <br>*optional*||string|
|**Query**|**polygon**  <br>*optional*||string|
|**Body**|**name two**  <br>*optional*|some description two|string|


#### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|OK|[House](#house)|
|**500**|500post|[ERROR](#error)|


#### Consumes

* `application/json`


#### Produces

* `\*/*`


#### Tags

* 首页


#### Security

|Type|Name|Scopes|
|---|---|---|
|**apiKey**|**[Authorization](#authorization)**|global|


<a name="indexusingget"></a>
### 访问首页
```
GET /index
```


#### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Header**|**name one**  <br>*required*|some description one|string||
|**Query**|**username**  <br>*required*|用户名称|string|`"李白"`|
|**Body**|**name two**  <br>*optional*|some description two|string||


#### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|OK|string|
|**401**|401get|No Content|
|**500**|500get|[ERROR](#error)|


#### Produces

* `\*/*`


#### Tags

* 首页


#### Security

|Type|Name|Scopes|
|---|---|---|
|**apiKey**|**[Authorization](#authorization)**|global|




<a name="definitions"></a>
## Definitions

<a name="house"></a>
### House

|Name|Schema|
|---|---|
|**houseId**  <br>*optional*|string|
|**location**  <br>*optional*|string|
|**polygon**  <br>*optional*|string|




<a name="securityscheme"></a>
## Security

<a name="authorization"></a>
### Authorization
*Type* : apiKey  
*Name* : TOKEN  
*In* : HEADER



