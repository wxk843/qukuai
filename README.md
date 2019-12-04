# qukuai
基于以太坊的应用开发实例

环境安装：（windows 10 家庭版）
1、安装Docker 
2、安装成功后，启动Docker Quickstart Terminal
docker run -d --name ethereum -p 8545:8545 -p 30303:30303 ethereum/client-go --rpc --rpcaddr "0.0.0.0" --rpcapi="db,eth,net,web3,personal" --rpccorsdomain "*" --dev

运行以下命令，可以对ethereum进行操作
docker exec -it ethereum geth attach ipc:/tmp/geth.ipc

如创建帐号：personal.newAccount("123456")

启动ethereum：
docker start ethereum
