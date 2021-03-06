磁盘IO性能

性能评价指标：
1.IOPS（input/output per second）即每秒的输入输出量（或读写次数）
2.吞吐量（throughput）单位时间可以成功传输的数据数量

随机读写频繁的应用IOPS是关键指标，如OLTP（online transaction processing）
VOD（video on demand） 则关注吞吐量指标

一次IO的流程
1.寻址（seek）（磁盘磁头移动到要操作的初始数据块所在的磁道的正上方）
2.旋转延时（盘片旋转到可操作的扇区的过程）
3.传送时间（接着盘片的旋转，磁头不断的读/写相应的数据块，直到完成这次IO所需要的全部数据）

硬盘厂商宣传的三个参数：平均寻址时间，盘片旋转速度及最大传送速度

计算公式
IO时间= 寻址时间+盘片旋转速度/2 + 数据块/传输速度

如15000转/分钟转速的磁盘计算，寻址时间5ms，旋转延时(60s/15000) * (1/2)=2ms,传输速度40MB，

4K
IO time = 5ms + 2ms +4k/40MB = 5+2+0.1=7.1ms
IOPS = 1s/7.1ms = 140 

64K
IO time = 5ms + 2ms +64k/40MB = 5+2+1.6=8.6ms
IOPS = 1s/8.6ms = 116

极端情况：比如读取一个很大的存储连续分布在磁盘的文件，磁头在完成一个读IO操作之后，不需要从新寻址，也不需要旋转延时
4K
IO time = 0 + 0 +4k/40MB = 0.1ms
IOPS = 1s/0.1ms = 10000

连续 / 随机 I/O
连续 I/O 指的是本次 I/O 给出的初始扇区地址和上一次 I/O 的结束扇区地址是完全连续或者相隔不多的。反之，如果相差很大，则算作一次随机 I/O

连续 I/O 比随机 I/O 效率高的原因是：在做连续 I/O 的时候，磁头几乎不用换道，或者换道的时间很短；而对于随机 I/O，如果这个 I/O 很多的话，会导致磁头不停地换道，造成效率的极大降低