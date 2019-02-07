# guacamole-simple-client

Very simple guacamole client without authentication.

This project is based on https://github.com/apache/guacamole-client/tree/master/doc/guacamole-example

To run during development:

  `mvn jetty:run`
  
Complete example setup:

  `docker run --rm  --name selenium -d selenium/standalone-chrome-debug:3.4.0`
  
  `docker run --rm  --name guacd --link selenium -d guacamole/guacd`
  
  `docker run -it -p 8080:8080 --link guacd --rm bcodingde/guacamole-simple-client`
  
Then go to:

http://localhost:8080/#targetHost=selenium&guacdHost=guacd&targetPassword=secret

Optional parameters:
* targetProtocol (default vnc)
* guacdPort (default 4822)
* targetPort (default 5900)