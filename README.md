## Akka HTTP Streaming example

Akka HTTP implementation of the classic reactive tweets application where streaming tweets obtained through a single 
connection are broadcasted to any number of clients through websockets.

To run, you must log in to your Twitter account and create a new application. Fill in all credentials in the 
`src/main/resources/application.conf` file.

The `index.html` file is borrowed from [this Github repo](https://github.com/andregoncalves/twitter-nodejs-websocket),
while the CSS file links to [this other Github repo](https://github.com/LiamWalshWeb/Terminal-CSS).


