package io.vertx.perf.web;

import io.vertx.core.AbstractVerticle;

public class SimpleHttpVariableBodySize extends AbstractVerticle{

  public void start() {

    String host = System.getProperty("vertx.host", "f24lite");
    int port = Integer.getInteger("vertx.port", 8080);
    System.out.println("Host: " + host);
    System.out.println("Port: " + port);

    vertx.createHttpServer().requestHandler(req -> {
      if (req.uri().equals("/")) {
        req.response().setStatusCode(200).end("you used the wrong path, try /form instead");
      } else if (req.uri().startsWith("/form")) {
        req.setExpectMultipart(true);
        int cl = Integer.parseInt(req.getHeader("content-length"));
        req.bodyHandler(body -> {
           assert (cl == body.length() );
        });
      } else {
        System.out.println("page not found");
        req.response().setStatusCode(404).end();
      }
      req.response().putHeader("content-type", "text/html").end("<html><body><h1>Thank you for the message!</h1></body></html>");
    }).listen(port, host);

    System.out.println("Server is started");
  }

}
