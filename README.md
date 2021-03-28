# DeltaMongo

##Introduction  
This is a Java MongoDB repository framework project which can support restore the data image by timestamp.

##Example
**Operate data via DeltaMongo repository interface**  
2020/1/1T10:00:00Z insert - {"a" : "a", "b" : "b"} -> Data ID "abc123" generated  
2020/1/2T10:00:00Z update - {"_id":"abc123", "a" : "a"} -> Remove the element "b" out of the data  
2020/1/3T10:00:00Z update - {"_id":"abc123", "a" : "a", "b" : "b", "c" : "c"} -> Add the element "b" and "c" into the data  

**Search data via DeltaMongo repository interface**  
Criteria -> {"_id" : "abc123"} - Result -> {"_id":"abc123", "a" : "a", "b" : "b", "c" : "c"}  
Criteria -> {"_id" : "abc123", "date" : "2020/1/2T12:00:00Z"} - Result -> {"_id":"abc123", "a" : "a"}  
Criteria -> {"_id" : "abc123", "date" : "2020/1/1T12:00:00Z"} - Result -> {"_id":"abc123", "a" : "a", "b" : "b"}  

##Schema
**The actual data structure in the MongoDB**  
{  
 "_id" : "abc123",  
 "current" : {  
  "a" : "a",  
  "b" : "b",  
  "c" : "c",  
  "editedTime" : "2020/1/3T10:00:00Z"  
 },  
 "2020/1/1T10:00:00Z" : {  
  "absent" : {  
   "editedTime" : "2020/1/2T10:00:00Z"  
  },  
  "present" : {  
   "b" : "b",  
   "editedTime" : "2020/1/1T10:00:00Z"  
  }  
 },  
 "2020/1/2T10:00:00Z" : {  
  "absent" : {  
   "b" : "b",  
   "c" : "c",  
   "editedTime" : "2020/1/3T10:00:00Z"  
  },  
  "present" : {  
   "editedTime" : "2020/1/2T10:00:00Z"  
  }  
 }  
}

##Thought of Design
This framework will according the input parameter "date" as key to locate the delta element within the record, once the target "date" element is located, then the program will patch the history delta data from "current" element to the target "date" element by the edited time between of them descendingly.