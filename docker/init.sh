#! /bin/bash

if [ -n "${service_port}" ];then
  sed -i "s/5005/${service_port}/g" /dist/conf/application.yml
fi

if [ -n "${db_ip}" ];then
  sed -i "s/127.0.0.1/${db_ip}/g" /dist/conf/application.yml
fi

if [ -n "${db_port}" ];then
  sed -i "s/3306/${db_port}/g" /dist/conf/application.yml
fi

if [ -n "${db_name}" ];then
  sed -i "s/webasechainmanager/${db_name}/g" /dist/conf/application.yml
fi

if [ -n "${db_account}" ];then
  sed -i "s/defaultAccount/${db_account}/g" /dist/conf/application.yml
fi

if [ -n "${db_password}" ];then
  sed -i "s/defaultPassword/${db_password}/g" /dist/conf/application.yml
fi

if [ ${db_init}=="yes" ];then
  if [ -d /dist/script ] && [ -n "${db_ip}" ] && [ -n "${db_port}" ] && [ -n "${db_name}" ] && [ -n "${db_account}" ] && [ -n "${db_password}" ];then
    echo "CREATE DATABASE IF NOT EXISTS ${db_name} DEFAULT CHARSET utf8 COLLATE utf8_general_ci;" > /dist/script/createdb.sql
    mysql -h $db_ip -u $db_account -p$db_password mysql < /dist/script/createdb.sql
    cd /dist/script/
    sed -i "s/webasechainmanager/${db_name}/g" webase.sh
    sed -i "s/defaultAccount/${db_account}/g" webase.sh
    sed -i "s/defaultPassword/${db_password}/g" webase.sh
    res=`bash webase.sh ${db_ip} ${db_port} | grep ERROR`
    if [ -z "${res}" ];then
      mv /dist/script /dist/script.bk
    else
      echo "${res}"
    fi
  fi
fi

