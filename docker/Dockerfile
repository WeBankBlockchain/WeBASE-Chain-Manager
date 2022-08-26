FROM openjdk:8-stretch

RUN sed -i 's/deb.debian.org/mirrors.ustc.edu.cn/g' /etc/apt/sources.list
RUN sed -i 's|security.debian.org/debian-security|mirrors.ustc.edu.cn/debian-security|g' /etc/apt/sources.list
RUN apt-get update  &&  apt-get -y install mysql-client  &&  rm -rf /var/lib/apt/lists/*

COPY dist /dist
RUN  mv /dist/conf_template /dist/conf
COPY docker/init.sh /dist
RUN  chmod -R +x /dist/*.sh

WORKDIR /dist
EXPOSE 5005

ENTRYPOINT bash init.sh && bash start.sh && sleep 3 && tail -F log/WeBASE-Chain-Manager-error.log
