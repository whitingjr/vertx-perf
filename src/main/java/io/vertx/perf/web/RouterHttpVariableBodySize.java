package io.vertx.perf.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RouterHttpVariableBodySize extends AbstractVerticle {

   private static final Logger logger = Logger.getLogger(RouterHttpVariableBodySize.class.getName());
   private static final String message = "<html><body><h1>Thank you for the message!</h1></body></html>";
   private static final String TEXT_HTML = "text/html";
   private static final String UTF_8 = "UTF-8";

   @Override
   public void start() throws Exception {
      String host = System.getProperty("vertx.host", "f24lite");
      int port = Integer.getInteger("vertx.port", 8080);
      System.out.println("Host: " + host);
      System.out.println("Port: " + port);

      Router router = Router.router(vertx);
      router.route().handler(BodyHandler.create());
      router.post("/nonblockingform").blockingHandler(routingContext -> {
         String body = routingContext.getBodyAsString(UTF_8);
         int size = body.length();
         int bs = Integer.parseInt(routingContext.request().getHeader(HttpHeaders.CONTENT_LENGTH));
         boolean equal = size == bs;
         assert size == bs: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, body.length());
         if (equal){
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, TEXT_HTML);
            routingContext.response().setStatusCode(200).end(message);
         } else {
            routingContext.fail(new Exception("Server detected the request body does not match the expected length as content-length indicates."));
            routingContext.next();
         }
      }, false);
      router.post("/blockingform").handler (handler -> {
         Buffer b = handler.getBody();
         int size = b.length();
         int bs = Integer.parseInt(handler.request().getHeader(HttpHeaders.CONTENT_LENGTH));
         boolean equal = size == bs; 
         assert equal: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, b.length());
         if (equal){
            handler.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
            handler.response().setStatusCode(200).end("<html><body><h1>Thank you for the message!</h1></body></html>");
//            System.out.println("Blocking Success");
         } else {
            handler.fail(new Exception("Server detected the request body does not match the expected length as content-length indicates."));
            handler.next();
         }
      });

      router.route().failureHandler(fh -> {
         System.out.println("Failure" + fh.failure().getMessage());
         fh.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html");
         fh.response().setStatusCode(500).end(String.format("barf [%1$s]",fh.failure().getMessage()));
      });
      vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
   }

   void setExceptionHandlers(RoutingContext ctx) {
      ctx.request().setExpectMultipart(true);
      ctx.request().exceptionHandler(this::exceptionHandler);
      ctx.response().exceptionHandler(this::exceptionHandler);
      ctx.next();
   }
   
   void exceptionHandler(Throwable t){
      logger.log(Level.WARNING, "bah something went wrong", t);
   }

}
