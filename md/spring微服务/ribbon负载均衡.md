源码分析http://blog.didispace.com/springcloud-sourcecode-ribbon/：
我们已经可以大致理清Spring Cloud中使用Ribbon实现客户端负载均衡的基本脉络。
了解了它是如何通过LoadBalancerInterceptor拦截器对RestTemplate的请求进行拦截，
并利用Spring Cloud的负载均衡器LoadBalancerClient将以逻辑服务名为host的URI转换成具体的服务实例的过程。
同时通过分析LoadBalancerClient的Ribbon实现RibbonLoadBalancerClient，
可以知道在使用Ribbon实现负载均衡器的时候，实际使用的还是Ribbon中定义的ILoadBalancer接口的实现，
自动化配置会采用ZoneAwareLoadBalancer的实例来进行客户端负载均衡实现。

AbstractLoadBalancer是ILoadBalancer
1、定义了获取LoadBalancerStats对象的方法，LoadBalancerStats对象被用来存储负载均衡器中各个服务实例当前的属性和统计信息，这些信息非常有用

BaseLoadBalancer类是Ribbon负载均衡器的基础实现类，在该类中定义很多关于均衡负载器相关的基础内容：
1、定义并维护了两个存储服务实例Server对象的列表。一个用于存储所有服务实例的清单，一个用于存储正常服务的实例清单。
2、定义了负载均衡的处理规则IRule对象，从BaseLoadBalancer中chooseServer(Object key)的实现源码，我们可以知道负载均衡器实际进行服务实例选择任务是委托给了IRule实例中的choose函数来实现。而在这里，默认初始化了RoundRobinRule为IRule的实现对象。RoundRobinRule实现了最基本且常用的线性负载均衡规则。
3、启动ping任务：在BaseLoadBalancer的默认构造函数中，会直接启动一个用于定时检查Server是否健康的任务。该任务默认的执行间隔为：10秒

DynamicServerListLoadBalancer类继承于BaseLoadBalancer类，它是对基础负载均衡器的扩展。在该负载均衡器中，实现了服务实例清单的在运行期的动态更新能力；同时，它还具备了对服务实例清单的过滤功能，也就是说我们可以通过过滤器来选择性的获取一批服务实例清单。下面我们具体来看看在该类中增加了一些什么内容：
1、从DynamicServerListLoadBalancer的成员定义中，我们马上可以发现新增了一个关于服务列表的操作对象：ServerList<T> serverListImpl。其中泛型T从类名中对于T的限定
2、ServerList默认配置到底使用了哪个具体实现呢？既然在该负载均衡器中需要实现服务实例的动态更新，那么势必需要ribbon具备访问eureka来获取服务实例的能力，所以我们从Spring Cloud整合ribbon与eureka的包org.springframework.cloud.netflix.ribbon.eureka下探索，可以找到配置类EurekaRibbonClientConfiguration，在该类中可以找到看到下面创建ServerList实例的内容
3、在DiscoveryEnabledNIWSServerList中通过EurekaClient从服务注册中心获取到最新的服务实例清单后，返回的List到了DomainExtractingServerList类中，将继续通过setZones函数进行处理，而这里的处理具体内容如下，主要完成将DiscoveryEnabledNIWSServerList返回的List列表中的元素，转换成内部定义的DiscoveryEnabledServer的子类对象DomainExtractingServer，在该对象的构造函数中将为服务实例对象设置一些必要的属性，比如id、zone、isAliveFlag、readyToServe等信息
4、可以看到，这里终于用到了我们之前提到的ServerList的getUpdatedListOfServers，通过之前的介绍我们已经可以知道这一步实现了从Eureka Server中获取服务可用实例的列表。在获得了服务实例列表之后，这里又将引入一个新的对象filter，追朔该对象的定义，我们可以找到它是ServerListFilter定义的。
5、AbstractServerListFilter：这是一个抽象过滤器，在这里定义了过滤时需要的一个重要依据对象LoadBalancerStats，我们在之前介绍过的，该对象存储了关于负载均衡器的一些属性和统计信息等。
6、ZoneAffinityServerListFilter：该过滤器基于“区域感知（Zone Affinity）”的方式实现服务实例的过滤，也就是说它会根据提供服务的实例所处区域（Zone）与消费者自身的所处区域（Zone）进行比较，过滤掉那些不是同处一个区域的实例。


默认负载均衡器：ZoneAwareLoadBalancer