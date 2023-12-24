REPOSITORY=/home/ubuntu/LetMeKnow

APP_NAME=letMeKnow
JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
echo ">>> build 파일명: $JAR_NAME" >> /home/ubuntu/LetMeKnow/deploy.log

JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

echo ">>> 현재 실행중인 애플리케이션 pid 확인" >> /home/ubuntu/LetMeKnow/deploy.log
CURRENT_PID=$(pgrep -f $APP_NAME)

if [ -z $CURRENT_PID ]
then
  echo ">>> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/LetMeKnow/deploy.log
else
  echo ">>> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo ">>> JAR 배포"    >> /home/ubuntu/LetMeKnow/deploy.log
# Java 환경변수
source REPOSITORY/scripts/.bashrc

# profile prod
nohup java -jar $JAR_PATH --spring.profiles.active=prod > /home/ubuntu/LetMeKnow/nohup.out 2>&1 &


