java -agentpath:/home/julien/tools/jprofiler9/bin/linux-x64/libjprofilerti.so="address=10.39.168.2,port=8849" -Dvertx.disableContextTimings=true -Dvertx.disableTCCL=true -Dvertx.threadChecks=false -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.rmi.port=1098 -Dcom.sun.management.jmxremote.local.only=false -Djava.rmi.server.hostname=10.39.168.2 -Dcom.sun.management.jmxremote.host=10.39.168.2 -Dvertx.host=192.168.123.25 -Dvertx.port=8080 -jar target/vertx-perf-3.1.0-SNAPSHOT-fat.jar -instances 32
