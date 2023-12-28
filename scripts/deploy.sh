APP_NAME=letMeKnow

REPOSITORY=/home/ubuntu/LetMeKnow
cd $REPOSITORY

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

# 현재 시간
echo "CURRENT_TIME: $(date '+%Y%m%d %H:%M:%S')" >> $REPOSITORY/deploy.log
echo "REPOSITORY: $REPOSITORY" >> $REPOSITORY/deploy.log

CURRENT_PID=$(pgrep -f $APP_NAME)

echo "CURRENT_PID: $CURRENT_PID" >> $REPOSITORY/deploy.log

if [ -z $CURRENT_PID ]
then
  echo "> 종료할 애플리케이션이 없습니다." >> $REPOSITORY/deploy.log
else
  echo "> kill -15 $CURRENT_PID" >> $REPOSITORY/deploy.log
  kill -15 $CURRENT_PID
fi

nohup java -jar $JAR_PATH --spring.profiles.active=prod > $REPOSITORY/nohup.out 2>&1 &
echo "> Deploy - $JAR_PATH" >> $REPOSITORY/deploy.log
echo " " >> $REPOSITORY/deploy.log
