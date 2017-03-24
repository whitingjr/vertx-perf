package io.vertx.perf.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RouterHttpVariableBodySize extends AbstractVerticle {

   private static final Logger logger = Logger.getLogger(RouterHttpVariableBodySize.class.getName());
   private boolean blocking = false;
   private String body = null;

   @Override
   public void start() throws Exception {
      String host = System.getProperty("vertx.host", "f24lite");
      int port = Integer.getInteger("vertx.port", 8080);
      System.out.println("Host: " + host);
      System.out.println("Port: " + port);

      Router router = Router.router(vertx);
//      router.post("/form").handler(BodyHandler.create());
      router.post("/form").handler(this::setExceptionHandlers);
      
      router.post("/form").handler(this::isBlocking);
//      router.post("/").handler(req -> {
//         req.response().setStatusCode(200).end("you used the wrong path, try /form instead");
//      });
      if (blocking){
         router.post("/form").blockingHandler(routingContext -> {
            String body = routingContext.getBodyAsString();
            int size = body.length();
            int bs = Integer.parseInt(routingContext.request().getHeader("content-length"));
            assert size == bs: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, body.length());
            routingContext.response().setStatusCode(200).end("<html><body><h1>Thank you for the message!</h1></body></html>");
            routingContext.next();
         }, false);
      } else {
         router.post("/form").handler (handler -> {
            handler.request().bodyHandler(bh -> {
               body = bh.toString();
            });
            int size = body.length();
            int bs = Integer.parseInt(handler.request().getHeader("content-length"));
            assert size == bs: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, body.length());
            handler.response().setStatusCode(200).end("<html><body><h1>Thank you for the message!</h1></body></html>");
            handler.next();
         });
      }
      router.route().handler(routingContext -> {
         routingContext.response().putHeader("content-type", "text/html");
         routingContext.next();
      });
      vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
   }

   void setExceptionHandlers(RoutingContext ctx) {
      ctx.request().setExpectMultipart(true);
      ctx.request().exceptionHandler(this::exceptionHandler);
      ctx.response().exceptionHandler(this::exceptionHandler);
      ctx.next();
   }
   
   void isBlocking(RoutingContext ctx) {
      String value = ctx.request().getParam("blocking");
      if (value != null && !"".equals(value)){
        blocking = Boolean.parseBoolean(value);
      }
      ctx.next();
   }

   void exceptionHandler(Throwable t){
      logger.log(Level.WARNING, "bah something went wrong", t);
   }

}
