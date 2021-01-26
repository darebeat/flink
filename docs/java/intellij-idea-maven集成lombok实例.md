# lombok

lombok 通过简单注解方式简化java代码。（如消除实体对象的get/setter方法、日志对象声明等...）

## IDEA安装步骤：

+ 1、选择支持注解处理：Settings-->Build-->Annotation Processors-->勾选上Enable annotation processing

+ 2、安装插件：Settings-->Plugins-->单击Browse repositories
查找lombok插件并进行安装：

+ 3、新增lombok maven jar包依赖：

```xml
<!--日志封装类-->
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.4</version>
</dependency>
```

+ 4、新增lombok maven插件支持：

```xml
<plugin>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.4</version>
</plugin>
```

## Lombok注解的使用

### 1. POJO类常用注解

```java
import lombok.*;

@ToString(of = {"name","age"},exclue = {"password"})
@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@NoArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Test {
	@NonNull private id int;
	private String name;
	private int age ; 
	private String address; 
	private String password;
	@Getter @Setter private String email;
}
```

+ 1. @Getter/@Setter: 

作用类上，生成所有成员变量的getter/setter方法；作用于成员变量上，生成该成员变量的getter/setter方法。可以设定访问权限及是否懒加载等。

+ 2. @ToString：

作用于类，覆盖默认的toString()方法，可以通过of属性限定显示某些字段，通过exclude属性排除某些字段



+ 3. @EqualsAndHashCode：

作用于类，覆盖默认的equals和hashCode


+ 4. @NonNull：

主要作用于成员变量和参数中，标识不能为空，否则抛出空指针异常。

+ 5. @NoArgsConstructor, @RequiredArgsConstructor, @AllArgsConstructor：

作用于类上，用于生成构造函数。有staticName、access等属性。

> @NoArgsConstructor：生成无参构造器；
> @RequiredArgsConstructor：生成包含final和@NonNull注解的成员变量的构造器；
> @AllArgsConstructor：生成全参构造器

+ 6. @Data：

作用于类上，是以下注解的集合：@ToString @EqualsAndHashCode @Getter @Setter @RequiredArgsConstructor

+ 7. @Builder：

作用于类上，将类转变为建造者模式

+ 8. @Log：

作用于类上，生成日志变量。针对不同的日志实现产品，有不同的注解：

### 2. 其他重要注解

+ 1. @Cleanup：

自动关闭资源，针对实现了java.io.Closeable接口的对象有效，如：典型的IO流对象

```java
public static void main(String[] args) throws Exception {
	File file = new File("/home/test/test.txt");
	@Cleanup InputStream in = new FileInputStream(file);
	byte[] bs = new byte[1024];
	int len;
	while ((len = in.read(bs)) != -1) {
		System.out.println("content: " + new String(bs,0,len));
	}
}

// 等价于
public static void main(String[] args) throws Exception {
	File file = new File("/home/test/test.txt");
	InputStream in = new FileInputStream(file);
	
	try {
		byte[] bs = new byte[1024];
		int len;
		while ((len = in.read(bs)) != -1) {
			System.out.println("content: " + new String(bs,0,len));
		}
	} finally {
		if(Collections.singletonList(in).get(0) != null) {
			in.close();
		}
	}
	
}
```

+ 2. @SneakyThrows：

可以对受检异常进行捕捉并抛出，可以改写上述的main方法如下：

```java
@SneakyThrows
public static void main(String[] args) {
	File file = new File("/home/test/test.txt");
	@Cleanup InputStream in = new FileInputStream(file);
	byte[] bs = new byte[1024];
	int len;
	while ((len = in.read(bs)) != -1) {
		System.out.println("content: " + new String(bs,0,len));
	}
}
```

+ 3. @Synchronized：作用于方法级别，可以替换synchronize关键字或lock锁，用处不大


