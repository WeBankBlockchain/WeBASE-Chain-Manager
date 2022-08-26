
# 命令返回非0时则退出
set -o errexit
# 管道命令中任何一个失败，就退出
set -o pipefail
# 遇到不存在的变量就会报错并停止执行
set -o nounset
# 在执行每一个命令之前把经过变量展开之后的命令打印出来，调试时很有用
# set -o xtrace

# 退出时，执行的命令，做一些收尾工作
trap 'echo -e "Aborted, error $? in command: $BASH_COMMAND"; trap ERR; exit 1' ERR


# 脚本所在的目录
s_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 脚本的全路径，包含脚本文件名
s_filename="${s_dir}/$(basename "${BASH_SOURCE[0]}")"
# 主目录
__root="$(cd "$(dirname "${s_dir}")" && pwd)"


# 编译项目
cd "${__root}" && chmod +x ./gradlew && ./gradlew clean build -x test


# 添加替换配置的命令
#cd "${s_dir}"
#sed -i '2a \\n' "${__root}"/dist/start.sh
#sed -i '3r sed.sh' "${__root}"/dist/start.sh


# 构建docker镜像
cd "${__root}"
docker build -t webasepro/webase-chain-manager:latest -f "${__root}"/docker/Dockerfile .


