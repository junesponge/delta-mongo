# DeltaMongo

**Introduction**  
* This is a Java MongoDB repository framework project which can support restore the data image by timestamp.

**Usage**  
* Reference the DeltaMongoRepository in your Service class, then `@Autowired` it and specify the `@Collection` which you need to operate the data in MongoDB.

      @Component
      public class UserService {

          @Autowired
          @Collection("test") // The MongoDB collection name
          DeltaMongoRepository deltaMongoRepository;

          public void userFunction() {
          
              // Data need to be inserted.
              this.deltaMongoRepository.insert(<JSONObject> data);
              
              // Date need to be updated (The "_id" is required).
              this.deltaMongoRepository.update(<JSONObject> data);
              
              // Find all of the records of current image.
              this.deltaMongoRepository.findAll();
              
              // Find the current image of specify record by "_id".
              this.deltaMongoRepository.findById(<String> id);
              
              // Find the past image of specify record by "_id" and timestamp.
              this.deltaMongoRepository.findByIdAndDate(<String> id, <Date> date);
              
              // Find the current image of specify record by query script.
              this.deltaMongoRepository.findByQuery(<Query> query);
              
              // Find the past image of specify record by query script and timestamp.
              this.deltaMongoRepository.findByQueryAndDate(<Query> query, <Date> date);            
          }
      }
    

**Example**  
* Operate data via DeltaMongo repository interface  
2020/1/1T10:00:00Z insert - {"a" : "a", "b" : "b"} -> Data ID "abc123" generated  
2020/1/2T10:00:00Z update - {"_id":"abc123", "a" : "a"} -> Remove the element "b" out of the data  
2020/1/3T10:00:00Z update - {"_id":"abc123", "a" : "a", "b" : "b", "c" : "c"} -> Add the element "b" and "c" into the data  

* Search data via DeltaMongo repository interface  

    Criteria -> `{"_id" : "abc123"}`  
    Result -> `{"_id":"abc123", "a" : "a", "b" : "b", "c" : "c"}` 
     
    Criteria -> `{"_id" : "abc123", "date" : "2020/1/2T12:00:00Z"}`  
    Result -> `{"_id":"abc123", "a" : "a"}`  
    
    Criteria -> `{"_id" : "abc123", "date" : "2020/1/1T12:00:00Z"}`  
    Result -> `{"_id":"abc123", "a" : "a", "b" : "b"}`  

**Schema**  
* The actual data structure store in the MongoDB  

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

**Thought of Design**  
* This framework will according the input parameter "date" as key to locate the delta element within the record, once the target "date" element is located, then the program will patch the history delta data from "current" element to the target "date" element by the edited time between of them descendingly.
* Advantage - Due to the record just stored the delta data for each time of change, it will not occupy much disk space when just had few changes within the large record.
