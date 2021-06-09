# spring-integrate-mock
a mock util in Spring context and dependency on junit4

# 使用步骤

## 0、前置依赖
* JDK8
* Junit4
* Spring4以及以上
* 对象比较 https://github.com/yamikaze0/obj-compare

## 1、依赖引用
* install
```text
git clone https://github.com/yamikaze0/spring-integrate-mock.git
cd spring-integrate-mock && mvn install -DskipTests=true
```

* 引入依赖
```xml
<dependency>
    <groupId>org.yamikaze</groupId>
    <artifactId>spring-integrate-junit4-mock</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2、配置
* 在Spring的context配置文件中引入以下配置
```xml
<bean id="mockRegistry" class="com.yt.unit.test.mock.MockSpringRegistry">
   <property name="ignoreBean">
       <list>
           <!--  需要过滤代理哪些bean       -->
           <value>org.springframework.*.**</value>
       </list>
   </property>
</bean>
```
* 代码中增加配置需要代理哪些bean
```java

    static {
        // 通过匹配bean名称
        GlobalConfig.addBeanNamePattern("*Service*");
        GlobalConfig.addBeanNamePattern("*Adapter*");
        GlobalConfig.addBeanNamePattern("*AO*");
        // 通过Class对象
        GlobalConfig.addMockClass(XXXAdapter.class);    
    }
```

* 在测试类上使用指定的Runner
```java
@RunWith(SpringJunitMockRunner.class)
public class BaseTest extends AbstractJUnit4SpringContextTests {
    
}

```

## 3、使用

### 3.1、mock某个bean方法1
```java

@AutoWired
private XXXAdapter adapter;

@Test
public void testMockBeanMethod() {
    XXXAdapter mockAdapter = MockFactory.create(XXXAdapter.class);
    YamiMock.when(mockAdapter.someMethod(), true) // 匹配参数，false不匹配
        .thenReturn(1);
    
    // 正常编写方法
    int value = adapter.someMethod();    
    Assert.assertTrue(value == 1);
}

```

###3.2、mock某个bean方法2
```java

@AutoWired
private XXXService service;

@Test
public void testMockBeanMethod() {
    // 假设service的方法调用了某个adapter，想要mock adapter方法
    XXXAdapter mockAdapter = MockFactory.create(XXXAdapter.class);
    YamiMock.when(mockAdapter.someMethod(), true) // 匹配参数，false不匹配
        .thenReturn(1);
    
    // 正常编写方法，不需要改变service对象的adapter属性
    int value = service.someMethod();    
    Assert.assertTrue(value == 1);
}

```

###3.3、mock返回值为void的方法
```java

@AutoWired
private XXXService service;

@Test
public void testMockBeanMethod() {
    XXXAdapter mockAdapter = MockFactory.create(XXXAdapter.class);
    YamiMock.when(mockAdapter).doNothing();
    
    // 正常编写方法，不需要改变service对象的adapter属性
    int value = service.someMethod();    
    Assert.assertTrue(value == 1);
}

```

###3.4、mock静态方法与final方法
* 在要测试的类上标注
```java
@MockEnhance({StringUtils.class, RandomUtils.class}) // 例如需要mock apache common包的StringUtils和当前工程的工具类
public class SomeTest {
    
}

```

* 录制
由于上述的录制都是基于代理的，static方法和final方法不能被代理，所以录制过程稍微麻烦一点
```java

// 假设录制 StringUtils.isBlank无论什么情况都返回false
YamiMock.mock(StringUtils.class)
        .mockMethod("isBlank")
        //.noParam() //指定方法无参数
        .types(String.class) // 指定方法只有一个参数，类型为String
        //.param("123") // 指定参数，只有当参数与指定参数相等时才执行mock，不指定则所有调用都会被mock
        .result(false, false, true); // 第一次返回false，第二次返回false，第三次返回true，如果无返回值 调用doNothing

// 假设录制 RandomUtils.randomNumber 第一次返回123，第二次返回456
YamiMock.mock(RandomUtils.class).mockMethod("randomNumber").types(int.class).result("123", "456");

```

### 3.5、支持参数化测试
支持junit5类似的参数化测试

```java
// 如果当前单测环境不需要跟Spring结合，可以使用此Runner
@RunWith(JunitParameterizedRunner.class)
public class ParameterizedTest {

    @Test
    @ParameterizedSource("parameterized/params.txt") // 使用注解指定单测用例的文件
    public void testWithParams(String val, Integer value, Date date) {
        System.out.println("name is " + val + " and value is " + value + " and date is " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
    }

    @Test
    @ParameterizedSource("parameterized/params.xls")
    public void testWithParamsXls(String val, Integer value, @Converter(DateConverter.class /* 支持自定义参数转换*/ ) Date date) {
        System.out.println("name is " + val + " and value is " + value + " and date is " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
    }

    @Test
    @ParameterizedSource("parameterized/params.xlsx") // 用例文件支持txt和xlsx、xls
    public void testWithParamsXlsx(String val, Integer value, @Converter(DateConverter.class) Date date) {
        System.out.println("name is " + val + " and value is " + value + " and date is " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date));
    }
}

```

单测用例文件内容(txt)
姓名  |   值   |  时间
张三  |  123   | 2020-12-21 12:21:23
李四  |  124   | 2020-12-21 12:21:23
王武  | 125 | 2020-12-21 12:21:32
yamikaze | 1 |  2020-12-21 11:22:23
zhouyu | 2 | 2020-12-21 12:21:33
wang wu | 3  | 2020-12-21 12:21:32
