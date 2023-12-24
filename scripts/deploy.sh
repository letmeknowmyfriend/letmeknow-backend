REPOSITORY=/home/ubuntu/LetMeKnow

APP_NAME=letMeKnow
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)

JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(pgrep -f $APP_NAME)

if [ -z $CURRENT_PID ]
then
    echo ">>> No $APP_NAME is running"
else
  echo ">>> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

# profile prod
nohup java -jar $JAR_PATH --spring.profiles.active=prod > /dev/null 2> /dev/null < /dev/null &
echo ">>> Deploy Success!!"

