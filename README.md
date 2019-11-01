# Java SDK for Genesys Chat APIv2

This is Java SDK library for Genesys Chat API v2 with CometD. See more in [Genesys Document](https://docs.genesys.com/Documentation/GMS/8.5.2/API/ChatAPIv2CometD)

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.


### Installing

A step by step series of examples that tell you how to get a development env running


```
mvn package 
```

And add library to your project

Maven:
```
<dependency>
   <groupId>net.balkonsky</groupId>
   <artifactId>genesys-chat-apiv2-sdk</artifactId>
   <version>0.9.2-SNAPSHOT</version>
</dependency>
```

Gradle:

```
compile "net.balkonsky:genesys-chat-apiv2-sdk:0.9.2-SNAPSHOT"
```

## Code Example

__Create event manager object and subscribe to events:__
```
EventManager eventManager = new EventManager();
eventManager.subscribe(new ChatEventListener());
```

__Crate HTTP transport:__

```
HttpTransportClient httpTransportClient = new HttpTransportClientImpl(
                    "{hostname}",
                    {Socket Timeout},
                    {Connection Request Timeout},
                    {Connect Timeout}
            );
```

__Create Genesys Chat API v2 Client object and set Event Manager object, Http Transport object:__

```
ChatAPIv2Client chatAPIv2Client = new ChatAPIv2ClientImpl(
                                                      {Event Manager Object},
                                                      "{GMS hostname}",
                                                      "{GMS Comet Channel}",
                                                      {Cometd ConnectTimeout},
                                                      {Cometd Transport Enum},
                                                      {Http Transport Object}
                                              );
```

__Create listener class and implement Event Listener interface:__

```
class ChatEventListener implements EventListener {
        @Override
        public void onEvent(ChatEvent chatEvent) {
        
         }
        
        @Override
        public void onState(ChatState chatState) {
   
        }
}
```

## Built With

* [CometD Client](https://docs.cometd.org/current/reference/) - Highly Scalable Clustered Web Messaging
* [Apache Http Client](https://hc.apache.org/) - Http Client
* [Maven](https://maven.apache.org/) - Dependency Management


## Authors

* **Maksim Avramenko** - [Balkonsky](https://github.com/balkonsky/)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


