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
   private static final CharSequence UTF_8 = HttpHeaders.createOptimized("UTF-8");
//   private static final CharSequence message = HttpHeaders.createOptimized("<html><body><h1>Thank you for the message!</h1></body></html>");
   private static final Buffer message = 

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
          fh.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML);
          fh.response().setStatusCode(500).end(String.format("barf [%1$s]",fh.failure().getMessage()));
       });
      router.post("/nonblockingform").blockingHandler(routingContext -> {
         setExceptionHandlers(routingContext);
         Buffer b = routingContext.getBody();
         int size = b.length();
         int bs = Integer.parseInt(routingContext.request().getHeader(HttpHeaders.CONTENT_LENGTH));
         boolean equal = size == bs;
         assert equal: String.format("Comparing the size of the body string size [%1$d] and content length [%2$d] showed a mismatch. They should be identical.",  size, bs);
         if (equal){
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaders.TEXT_HTML);
            routingContext.response().setStatusCode(200);
            routingContext.response().end();
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
