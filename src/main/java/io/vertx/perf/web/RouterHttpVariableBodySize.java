package io.vertx.perf.web;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.util.ResourceLeakDetector;
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
      String level = System.getProperty("io.netty.leakDetection.level", io.netty.util.ResourceLeakDetector.Level.DISABLED.name());
      logger.info(String.format("Started with level[%1$s]", level));
      
      ResourceLeakDetector.setLevel(io.netty.util.ResourceLeakDetector.Level.valueOf(level));

      Router router = Router.router(vertx);
      router.route().handler(BodyHandler.create());
      router.route().failureHandler(fh -> {
          logger.severe("Failure:" + fh.failure().getMessage());
          fh.response().putHeader("Content-Type", "text/html");
          fh.response().setStatusCode(500).end(String.format("barf [%1$s]",fh.failure().getMessage()));
       });
      router.post("/nonblockingform").blockingHandler(routingContext -> {
         setExceptionHandlers(routingContext);
         Buffer b = routingContext.getBody();
         String body = routingContext.getBodyAsString(UTF_8);
         int size = body.length();
         int bs = Integer.parseInt(routingContext.request().getHeader("Content-Length"));
         boolean equal = size == bs;
         assert size == bs: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, body.length());
         if (equal){
            routingContext.response().putHeader("Content-Type", TEXT_HTML);
            routingContext.response().setStatusCode(200);
            routingContext.response().end(message);
         } else {
            routingContext.fail(new Exception("Server detected the request body does not match the expected length as content-length indicates."));
            routingContext.next();
         }
         if (b.isDirectOrPooled()){
            b.close();
         }
      }, false);

      vertx.createHttpServer().requestHandler(router::accept).listen(port, host);
   }

   void setExceptionHandlers(RoutingContext ctx) {
//      ctx.request().setExpectMultipart(true);
      ctx.request().exceptionHandler(this::exceptionHandler);
      ctx.response().exceptionHandler(this::exceptionHandler);
//      ctx.next();
   }
   
   void exceptionHandler(Throwable t){
      logger.log(Level.SEVERE, "bah something went wrong: "+t.getMessage(), t);
   }
}
