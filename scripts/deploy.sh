APP_NAME=letMeKnow

REPOSITORY=/home/ubuntu/LetMeKnow
cd $REPOSITORY

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(pgrep -f $APP_NAME)

if [ -z $CURRENT_PID ]
then
  echo "> 종료할 애플리케이션이 없습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 30
fi

echo "> Deploy - $JAR_PATH "
nohup java -jar $JAR_PATH --spring.profiles.active=prod > $REPOSITORY/nohup.out 2>&1 &
