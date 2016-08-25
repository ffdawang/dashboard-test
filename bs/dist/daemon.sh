#! /bin/sh

command='java -Xms256m -Xmx512m -jar board-bs-1.0.0-jar-with-dependencies.jar'
#start
start() {
       exec $command &
       echo "board-bs is up and running!"
}
#stop
stop(){
        ps -ef | grep "$command" | awk '{print $2}' | while read pid
        do
			kill -9 $pid
        done
}

case "$1" in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  stop
  start
  ;;
*)
  printf 'Usage: %s {start|stop|restart}\n' "$prog"
  exit 1
  ;;
esac