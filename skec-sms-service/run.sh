#!/bin/sh
JAR_PATH=/opt/skec-sms-service
if [ ${JVM_PROFILE} = "prod" ]; then
  JVM_OPTS="-Dlogfile.path=prod -Dfile.encoding=${JVM_ENCODING} -Xms${JVM_MINIMUM_MEMORY} -Xmx${JVM_MAXIMUM_MEMORY}"
elif [ ${JVM_PROFILE} = "test" ]; then
  JVM_OPTS="-Dlogfile.path=test -Dfile.encoding=${JVM_ENCODING} -Xms${JVM_MINIMUM_MEMORY} -Xmx${JVM_MAXIMUM_MEMORY}"
elif [ ${JVM_PROFILE} = "dev" ]; then
  JVM_OPTS="-Dorg.eclipse.jetty.LEVEL=DEBUG -Dlogfile.path=development -Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory -Dfile.encoding=${JVM_ENCODING} -Xms${JVM_MINIMUM_MEMORY} -Xmx${JVM_MAXIMUM_MEMORY}"
elif [ ${JVM_PROFILE} = "test-server" ]; then
  JVM_OPTS="-Dorg.eclipse.jetty.LEVEL=DEBUG -Dlogfile.path=development -Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory -Dfile.encoding=${JVM_ENCODING} -Xms${JVM_MINIMUM_MEMORY} -Xmx${JVM_MAXIMUM_MEMORY}"
fi

exec java $JVM_OPTS -jar $JAR_PATH/app.jar