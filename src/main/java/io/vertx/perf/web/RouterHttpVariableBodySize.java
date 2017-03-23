package io.vertx.perf.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;

public class RouterHttpVariableBodySize extends AbstractVerticle {

   
   @Override
   public void start() throws Exception {
      String host = System.getProperty("vertx.host", "f24lite");
      int port = Integer.getInteger("vertx.port", 8080);
      System.out.println("Host: " + host);
      System.out.println("Port: " + port);

      Router router = Router.router(vertx);
      
      router.post("/").handler(req -> {
         req.response().putHeader("content-type", "text/html").setStatusCode(200).end("you used the wrong path, try /form instead");
      });
      router.post("/form").blockingHandler(routingContext -> {
         int size = routingContext.getBody().length();
         int bs = Integer.parseInt(routingContext.request().getHeader("content-length"));
         assert size == bs;
         routingContext.response().setStatusCode(200).end("<html><body><h1>Thank you for the message!</h1></body></html>");
         routingContext.next();
      }, false);
      router.route().handler(routingContext -> {
          routingContext.response().putHeader("content-type", "text/html").end("<html><head></head><body>This catch all handler is an indication you used the wrong method. Try POST with the /form URI.</body></html>");;
       });
      vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
   }
}
