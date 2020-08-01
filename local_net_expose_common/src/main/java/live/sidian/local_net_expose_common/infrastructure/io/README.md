# 通道输入输出流
`ChannelOutputStream`使用`writeOp`方法写入命令, `ChannelInputStream`的`read(byte[])`方法读取到命令时, 会返回`-2`. 然后使用`readOp()`方法读取命令