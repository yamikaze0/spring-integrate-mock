# spring-integrate-mock
a mock util in Spring context and dependency on junit4/junit5

# 功能列表
- [X] 支持junit4/junit5
- [X] junit4支持参数化测试
- [X] 支持普通方法mock
- [X] 支持私有、静态方法mock
- [X] 支持DUBBO调用mock
- [X] 支持Mybatis Mapper调用mock
- [X] 支持调用路径打印（需要代理调用路径上的所有bean）
- [X] 支持单测类/单测方法命名检查（类以Test开头/结尾，方法以test开头）
- [X] 支持真实调用文件采集式mock

# 使用步骤

## 前置依赖
* JDK8
* Junit4/junit5
* Spring4以及以上
* 对象比较 https://github.com/yamikaze0/obj-compare

## 1、依赖引用
* install
```text
git clone https://github.com/yamikaze0/spring-integrate-mock.git
cd spring-integrate-mock && mvn install -DskipTests=true
```

### 1.1、junit4
```xml
<dependency>
    <groupId>org.yamikaze</groupId>
    <artifactId>spring-integrate-junit4</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 1.2、junit5
```xml
<dependency>
    <groupId>org.yamikaze</groupId>
    <artifactId>spring-integrate-junit5</artifactId>
    <version>1.0.1</version>
</dependency>
```

## 2、Spring Bean配置(必须)
```xml
<bean id="mockRegistry" class="org.yamikaze.unit.test.mock.MockSpringRegistry">
   <property name="ignoreBeans">
       <list>
           <!--  需要过滤代理哪些bean       -->
           <value>org.springframework.*.**</value>
       </list>
   </property>
</bean>
```

## 3、使用配置

### 3.1、junit4配置
如果使用Spring自带的runner，还需要在测试类中加入配置
```java
@org.junit.Rule
public org.yamikaze.unittest.junit4.ExtensionRule rule; // 必须声明为public
```

如果不需要上述配置可以使用
```java
@RunWith(org.yamikaze.unittest.junit4.SpringJunitMockRunner.class)
public class XXXTest {}
```
`SpringJunitMockRunner`会自动将`rule`生效

### 3.2、junit5配置
junit配置比较简单，只需要在单测类上加入如下注解配置即可
```java
@ExtendWith(Junit5MethodEachCallback.class)
public class XXXTest {}
```

### 3.3、配置代理的bean列表（必须）
经过上述配置之后，还需要配置需要代理那些bean，支持多维度的bean配置，我们需要在Spring容器初始化前配置，所以
一般在junit4中的`@BeforeClass`或者junit5中的`@BeforeAll`注解修饰的方法中进行配置，默认情况下，自动代理DUBBO reference bean
，以下为配置示例

* Ant通配符形势的bean名称配置
```java
GlobalConfig.addBeanNamePattern("*Adapter*");
```
* 不对DUBBO进行mock
```java
GlobalConfig.noMockDubbo();
```
* 某class需要进行配置
```java
GlobalConfig.addMockClass(UserService.class);
```

* 某class必须使用jdk代理
```java
GlobalConfig.addMustJdkMock(UserService.class);
```

* 以classname作为通配符进行匹配
```java
GlobalConfig.addMockPattern("com.xxx.xxx.xxx.*.**");
```

### 3.4、其他配置

* 开启/关闭mock
```java
GlobalConfig.setMockEnabled(true);
```

* 打印真实调用日志
* 打印使用日志
* 设置代理方式 cglib/java-native
* 启用/禁用agent


## 4、使用示例
### 4.1、使用注解形式

```java
// step1 使用@Mock注解声明方法的key
@Mock(clz = UserService.class, method = "getUser", key = "mock-getUser")
@Test
public void testUser() {
    // step2: 使用DataCodeFactory注册key对应的返回值，注入的值可以是异常对象
    User expect = new User();    
    DataCodeFactory.register("mock-getUser", expect);
    
    // step3、userApi调用userService
    User u = userApi.getUser(1L);
    assert u == expect;
}
```

### 4.2、mock-record形式
使用类似easymock和mockit的形式
```java
@Test
public void testGetUser() {
    // step1: 使用MockUtils获取代理对象，并进行mock-record
    UserMapper mock = MockUtils.createMock(UserMapper.class);
    User expect = JSON.parseObject("{\"password\":\"123456\",\"salt\":\"hel\",\"phone\":\"13467830023\",\"id\":1,\"username\":\"4088586803\"}\n", User.class);
    // when(T, boolean)的重载表示是否进行参数匹配，如果false或者默认方法，不会进行参数匹配
    MockUtils.when(mock.selectById(1L), true).matchBean("xxxService").thenReturn(expect);
    
    // step2: 调用userApi
    User actual = userApi.getUser(1L);
    assert actual == expect;
}

```

### 4.3、文件采集形式
step1: 文件采集形式需要引入pom
```xml
<dependency>
    <groupId>org.yamikaze</groupId>
    <artifactId>spring-integrate-mock-file</artifactId>
    <version>1.0.1</version>
</dependency>
```

step2: 指定哪些接口需要采集调用结果
```java
@Test
public void testGetUser() {
    // 指定哪些接口需要采集，ant通配符形式
    // mock(true)表示如果之前有采集的文件，会走mock的形式
    Mockit.mock(true).mock(Arrays.asList(UserService.class.getName() + "*"));

    User actual = userApi.getUser(1L);

    System.out.println(actual.getId());
}

```

Tip: 文件采集的方式第一次运行会进行真实调用，如果真实调用的接口和方法能够匹配上指定的列表，会将调用
的参数以及返回值以json文件的形式保存下来，默认保存在当前工作目录/mock下，文件路径为当前测试类的全称限定名+方法名，
你也可以使用`GlobalConfig.setSaveResourceLocation(dir)`进行重新设置

Tip2: 默认情况下，采集到的参数在进行mock时，会进行参数匹配，如果想要关闭匹配，可以使用`Mockit.mock(true).matchParams(false)`关闭方法全局参数匹配，或者进入到采集的文件，给json
加上 matchParams = false的键值对

### 4.4、静态方法mock
step1: 在单测方法上指定需要对哪些方法进行增强
```java
@MockEnhance(RandomUtils.class)
public class XXXTest{}
```

step2: 在单测方法内完成录制
```java
// 表示对RandomUtils#randomString进行mock，方法参数为int，值为10，返回987654321
MockUtils.mock(RandomUtils.class).mockMethod("randomString")
        .types(int.class).param(10).result("987654321");
```

## 5、其他功能

### 5.1、调用路径打印
默认情况下，会对所有代理的bean以及静态mock的方法调用进行采集，然后打印，效果如下：
```text
2022-06-15 15:45:50.585 INFO  InvokeTree [InvokeTree.java:109] - Invoke Tree Dump:
org.yamikaze.spring.mock.example.ApplicationTest#testGetUser time = 189ms, mode = mix
|---UserApi#getUser time = 37ms, mode = mix
    |---UserService#getUser time = 22ms, mode = mock
    |   |---UserMapper#selectById time = 17ms, mode = mock
    |       |---org.yamikaze.spring.mock.example.RandomUtils#randomString time = 0ms, mode = mock
    |---UserLogService#log time = 4ms, mode = real-invoke
```
